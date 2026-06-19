package co.com.disspace.app.presentacion

import android.graphics.Color
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.app.AlertDialog
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import co.com.disspace.app.MainActivity
import co.com.disspace.app.data.datasource.firstString
import co.com.disspace.app.data.datasource.optAny
import co.com.disspace.app.presentacion.common.DropdownOption
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val ESTADOS_COTIZACION = listOf("En proceso", "Para fabricación", "Finalizada")

private data class ZonaOption(
    val id: String,
    val description: String,
    val data: JSONObject
) {
    override fun toString(): String = description.ifBlank { "Zona" }
}

internal fun MainActivity.showCotizaciones(filters: Map<String, String> = emptyMap()) {
    val page = appPage("Cotizaciones", "Gestion comercial por sucursal.")
    val filterFields = listOf(
        "numero" to "Numero",
        "cliente" to "Cliente",
        "vendedor" to "Vendedor",
        "estado" to "Estado",
        "fechaInicio" to "Fecha inicio (YYYY-MM-DD)",
        "fechaFin" to "Fecha fin (YYYY-MM-DD)"
    )
    page.addView(topActions {
        addView(primaryButton("Nueva") { showCotizacionForm(null, null) })
        addView(filterButton(filters.size) {
            showFiltersDialog("Filtros de cotizaciones", filterFields, filters) { showCotizaciones(it) }
        })
    })

    val holder = section("Cargando...")
    page.addView(holder)
    apiCall({ api.get("/cotizaciones", filters) }) { json ->
        holder.removeAllViews()
        val rows = json.optJSONArray("cotizaciones") ?: JSONArray()
        if (rows.length() == 0) holder.addView(emptyText("No hay cotizaciones."))
        for (i in 0 until rows.length()) {
            val item = rows.optJSONObject(i) ?: continue
            val id = item.optAny("COTIZACIONID")
            val tienePedido = item.optAny("PEDIDOID").isNotBlank()
            holder.addView(card {
                addView(itemTitle(item, listOf("NUMERO", "CLIENTE")))
                addView(jsonPreview(item, listOf("FECHA", "ESTADO", "VENDEDOR"), hideDatabaseIds = true))
                addView(topActions {
                    addView(primaryButton("Detalle") { showCotizacionDetail(id) })
                    if (!tienePedido) {
                        addView(secondaryButton("Copiar") { postCotizacionAction(id, "/copiar") })
                        addView(secondaryButton("Enviar pedido") { postCotizacionAction(id, "/enviar-pedido") })
                        addView(dangerButton("Eliminar") {
                            confirm("Eliminar cotizacion", "Tambien se eliminaran sus detalles.") {
                                apiCall({ api.delete("/cotizaciones/$id") }) { showCotizaciones(filters) }
                            }
                        })
                    } else {
                        addView(secondaryButton("Ver pedido") {
                            showPedidoDetail(item.optAny("PEDIDOID"))
                        })
                    }
                })
            })
        }
    }
}

internal fun MainActivity.postCotizacionAction(id: String, suffix: String) {
    apiCall({ api.post("/cotizaciones/$id$suffix", JSONObject()) }) {
        toast(it.optString("message", "Operacion realizada"))
        when (suffix) {
            "/enviar-pedido" -> {
                val pedidoId = it.optAny("pedidoId")
                if (pedidoId.isNotBlank()) showPedidoDetail(pedidoId) else showCotizaciones()
            }
            "/copiar" -> {
                val newId = it.optAny("cotizacionId")
                if (newId.isNotBlank()) showCotizacionDetail(newId) else showCotizaciones()
            }
            else -> showCotizaciones()
        }
    }
}

internal fun MainActivity.showCotizacionForm(id: String?, existing: JSONObject?) {
    val page = appPage(if (id == null) "Nueva cotizacion" else "Editar cotizacion", "Encabezado")

    val fillForm: (JSONObject) -> Unit = data@{ data ->
        page.removeAllViews()
        val cot = data.optJSONObject("cotizacion") ?: existing ?: JSONObject()
        val vendedores = data.optJSONArray("vendedores") ?: JSONArray()
        val clientes = data.optJSONArray("clientes") ?: JSONArray()
        val suggested = data.optAny("numeroSugerido")
        val pedidoId = data.optAny("pedidoId")

        val numero = input("Numero", if (id == null) suggested else cot.optAny("NUMERO")).apply {
            inputType = InputType.TYPE_CLASS_NUMBER
        }
        val fecha = input("Fecha (YYYY-MM-DD)", if (id == null) today() else cot.firstString(listOf("FECHA"))).apply {
            inputType = InputType.TYPE_CLASS_DATETIME
        }
        page.addView(fieldLabel("Numero *"))
        page.addView(numero)
        page.addView(fieldLabel("Fecha *"))
        page.addView(fecha)

        var selectedClienteId = cot.optAny("CLIENTEID")
        var selectedVendedorId = cot.optAny("VENDEDORID")

        val clienteOptions = jsonArrayToOptions(clientes, "TERCEROID", "NOMBRE")
        val vendedorOptions = jsonArrayToOptions(vendedores, "TERCEROID", "NOMBRE")

        page.addView(labeledSpinner("Cliente *", clienteOptions, "Seleccionar cliente", selectedClienteId) { opt ->
            selectedClienteId = opt.id
        })
        page.addView(topActions {
            addView(secondaryButton("+ Nuevo cliente") {
                showQuickCreateTercero("2") { _, _ -> showCotizacionForm(id, existing) }
            })
        })

        page.addView(labeledSpinner("Vendedor *", vendedorOptions, "Seleccionar vendedor", selectedVendedorId) { opt ->
            selectedVendedorId = opt.id
        })
        page.addView(topActions {
            addView(secondaryButton("+ Nuevo vendedor") {
                showQuickCreateTercero("1") { _, _ -> showCotizacionForm(id, existing) }
            })
        })

        val estadoOptions = ESTADOS_COTIZACION.map { DropdownOption(it, it) }
        val selectedEstado = if (id == null) "En proceso" else cot.optAny("ESTADO")
        var selectedEstadoValue = selectedEstado
        page.addView(labeledSpinner("Estado", estadoOptions, "Seleccionar estado", selectedEstado) { opt ->
            selectedEstadoValue = opt.id
        })

        val descuento = input("Descuento %", cot.optAny("DESCUENTO").ifBlank { "0" }).apply {
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        }
        val iva = input("IVA %", cot.optAny("IVA").ifBlank { "0" }).apply {
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        }
        val observaciones = multiLineInput("Observaciones", cot.optAny("OBSERVACIONES"))

        page.addView(fieldLabel("Descuento %"))
        page.addView(descuento)
        page.addView(fieldLabel("IVA %"))
        page.addView(iva)
        page.addView(fieldLabel("Observaciones"))
        page.addView(observaciones)

        if (pedidoId.isNotBlank()) {
            page.addView(infoBanner("Esta cotizacion ya tiene pedido #$pedidoId. No se puede modificar."))
            page.addView(topActions {
                addView(secondaryButton("Atras") { showCotizaciones() })
                addView(primaryButton("Ver pedido") { showPedidoDetail(pedidoId) })
            })
        } else {
            page.addView(topActions {
                addView(secondaryButton("Cancelar") { showCotizaciones() })
                addView(primaryButton("Guardar") {
                    val numVal = numero.text.toString().trim()
                    val fechaVal = fecha.text.toString().trim()
                    if (numVal.isBlank() || fechaVal.isBlank()) {
                        showError("Numero y fecha son obligatorios")
                        return@primaryButton
                    }
                    if (selectedClienteId.isBlank() || selectedVendedorId.isBlank()) {
                        showError("Cliente y vendedor son obligatorios")
                        return@primaryButton
                    }
                    val body = JSONObject()
                        .put("numero", numVal)
                        .put("fecha", fechaVal)
                        .put("cliente", selectedClienteId)
                        .put("vendedor", selectedVendedorId)
                        .put("estado", selectedEstadoValue)
                        .put("descuento", descuento.text.toString().trim().ifBlank { "0" })
                        .put("iva", iva.text.toString().trim().ifBlank { "0" })
                        .put("observaciones", observaciones.text.toString().trim())
                    val call = if (id == null) { { api.post("/cotizaciones", body) } } else { { api.put("/cotizaciones/$id", body) } }
                    apiCall(call) {
                        toast(it.optString("message", "Cotizacion guardada"))
                        val newId = it.optAny("cotizacionId").ifBlank { id ?: "" }
                        if (newId.isNotBlank()) showCotizacionDetail(newId) else showCotizaciones()
                    }
                })
            })
        }
    }

    if (id == null) {
        apiCall({ api.get("/cotizaciones/form-data") }) { fillForm(it) }
    } else {
        apiCall({ api.get("/cotizaciones/$id") }) { fillForm(it) }
    }
}

internal fun MainActivity.showQuickCreateTercero(tipoTerceroId: String, onCreated: (String, String) -> Unit) {
    val content = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(18), dp(16), dp(18), dp(6))
        background = rounded(Color.rgb(33, 37, 41), dp(8), Color.rgb(63, 68, 74))
    }
    val nombre = input("Nombre *")
    val apellido = input("Apellido *")
    val documento = input("Documento")
    val correo = input("Correo")
    val telefono = input("Telefono")
    content.addView(fieldLabel("Nombre *"))
    content.addView(nombre)
    content.addView(fieldLabel("Apellido *"))
    content.addView(apellido)
    content.addView(fieldLabel("Documento"))
    content.addView(documento)
    content.addView(fieldLabel("Correo"))
    content.addView(correo)
    content.addView(fieldLabel("Telefono"))
    content.addView(telefono)

    val dialog = AlertDialog.Builder(this)
        .setView(content)
        .setNegativeButton("Cancelar", null)
        .setPositiveButton("Crear") { _, _ ->
            val nom = nombre.text.toString().trim()
            val ape = apellido.text.toString().trim()
            if (nom.isBlank() || ape.isBlank()) {
                showError("Nombre y apellido son obligatorios")
                return@setPositiveButton
            }
            val body = JSONObject()
                .put("nombre", nom)
                .put("apellido", ape)
                .put("tipoterceroid", tipoTerceroId)
                .put("nrodocumento", documento.text.toString().trim())
                .put("correo", correo.text.toString().trim())
                .put("telefono", telefono.text.toString().trim())
            apiCall({ api.post("/terceros", body) }) { json ->
                val newId = json.optAny("terceroId").ifBlank { json.optJSONObject("tercero")?.optAny("TERCEROID").orEmpty() }
                val fullName = "$nom $ape"
                if (newId.isNotBlank()) {
                    toast("Tercero creado")
                    onCreated(newId, fullName)
                }
            }
        }
        .show()
    dialog.window?.setBackgroundDrawable(rounded(Color.rgb(33, 37, 41), dp(8), Color.TRANSPARENT))
}

internal fun MainActivity.showCotizacionDetail(id: String) {
    val page = appPage("Detalle de cotizacion", "Zonas, detalles y acciones.")
    page.addView(topActions {
        addView(secondaryButton("Atras") { showCotizaciones() })
        addView(secondaryButton("Editar") { showCotizacionForm(id, null) })
        addView(primaryButton("Nueva zona") { showZonaForm(id, null, null) })
        addView(primaryButton("Nuevo detalle") { showDetalleForm(id, null, null) })
    })
    val holder = section("Cargando...")
    page.addView(holder)
    apiCall({ api.get("/cotizaciones/$id") }) { cot ->
        holder.removeAllViews()
        val cotizacion = cot.optJSONObject("cotizacion") ?: JSONObject()
        val pedidoId = cot.optAny("pedidoId")
        holder.addView(jsonPreview(cotizacion, listOf("NUMERO", "FECHA", "ESTADO", "CLIENTE", "VENDEDOR", "DESCUENTO", "IVA", "OBSERVACIONES"), hideDatabaseIds = true))

        if (pedidoId.isNotBlank()) {
            holder.addView(infoBanner("Cotizacion enviada a pedido #$pedidoId. Las zonas y detalles no se pueden modificar."))
            holder.addView(topActions {
                addView(primaryButton("Ver pedido") { showPedidoDetail(pedidoId) })
            })
        }

        apiCall({ api.get("/cotizaciones/$id/zonas") }) { zonasJson ->
            apiCall({ api.get("/cotizaciones/$id/detalles") }) { detJson ->
                val detalles = detJson.optJSONArray("detalles") ?: JSONArray()
                addZoneSelector(
                    holder = holder,
                    cotizacionId = id,
                    zonas = zonasJson.optJSONArray("zonas") ?: JSONArray(),
                    detalles = detalles,
                    readOnly = pedidoId.isNotBlank()
                )
                addCotizacionTotals(holder, detalles, cotizacion)
            }
        }
    }
}

private fun MainActivity.addCotizacionTotals(holder: LinearLayout, detalles: JSONArray, cotizacion: JSONObject) {
    var subtotal = 0.0
    for (i in 0 until detalles.length()) {
        val d = detalles.optJSONObject(i) ?: continue
        subtotal += calculateDetalleTotal(
            tipoProdId = d.optAny("TIPOPRODID").toIntOrNull() ?: 0,
            cantidad = d.optAny("CANTIDAD").toDoubleOrNull() ?: 0.0,
            ancho = d.optAny("ANCHO").toDoubleOrNull() ?: 0.0,
            alto = d.optAny("ALTO").toDoubleOrNull() ?: 0.0,
            precio = d.optAny("PRECIO").toDoubleOrNull() ?: 0.0,
            precioInstalacion = d.optAny("PRECIO_INSTALACION").toDoubleOrNull() ?: 0.0,
            instalacionPorAncho = d.optAny("INSTALACION_POR_ANCHO") == "1" || d.optAny("INSTALACION_POR_ANCHO").equals("true", true),
            instalacionPorAlto = d.optAny("INSTALACION_POR_ALTO") == "1" || d.optAny("INSTALACION_POR_ALTO").equals("true", true)
        )
    }
    val descuentoPct = cotizacion.optAny("DESCUENTO").toDoubleOrNull() ?: 0.0
    val ivaPct = cotizacion.optAny("IVA").toDoubleOrNull() ?: 0.0
    val descuentoMonto = subtotal * descuentoPct / 100.0
    val trasDescuento = subtotal - descuentoMonto
    val ivaMonto = trasDescuento * ivaPct / 100.0
    val total = trasDescuento + ivaMonto

    holder.addView(sectionTitle("Resumen"))
    holder.addView(card {
        addView(dataRow("Subtotal", formatMoney(subtotal)))
        if (descuentoPct > 0) addView(dataRow("Descuento ($descuentoPct%)", "- ${formatMoney(descuentoMonto)}"))
        if (ivaPct > 0) addView(dataRow("IVA ($ivaPct%)", formatMoney(ivaMonto)))
        addView(dataDivider())
        addView(dataRow("Total", formatMoney(total)))
    })
}

private fun MainActivity.dataRow(label: String, value: String): LinearLayout {
    return LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = android.view.Gravity.CENTER_VERTICAL
        setPadding(0, dp(6), 0, dp(6))
        addView(TextView(context).apply {
            text = label
            textSize = 14f
            setTextColor(Color.rgb(185, 189, 194))
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 0.5f)
        })
        addView(TextView(context).apply {
            text = value
            textSize = 15f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            setTextColor(Color.rgb(244, 245, 246))
            gravity = android.view.Gravity.END
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 0.5f)
        })
    }
}

private fun MainActivity.addZoneSelector(
    holder: LinearLayout,
    cotizacionId: String,
    zonas: JSONArray,
    detalles: JSONArray,
    readOnly: Boolean
) {
    holder.addView(sectionTitle("Zona"))
    val options = mutableListOf<ZonaOption>()
    for (i in 0 until zonas.length()) {
        val zona = zonas.optJSONObject(i) ?: continue
        val description = zona.optString("DESCRIPCION").ifBlank { "Zona ${i + 1}" }
        options.add(ZonaOption(zona.optAny("ZONAID"), description, zona))
    }

    if (options.isEmpty()) {
        holder.addView(emptyText("Sin zonas. Crea una zona para agregar detalles."))
        if (!readOnly) {
            holder.addView(topActions {
                addView(primaryButton("Crear zona") { showZonaForm(cotizacionId, null, null) })
            })
        }
        return
    }

    holder.addView(fieldLabel("Selecciona una zona"))
    val selectedZoneActions = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
    val detailHolder = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
    val adapter = object : ArrayAdapter<ZonaOption>(
        this, android.R.layout.simple_spinner_item, options
    ) {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            return (super.getView(position, convertView, parent) as TextView).apply {
                textSize = 15f
                setTextColor(Color.WHITE)
                setBackgroundColor(Color.rgb(35, 38, 43))
                setPadding(dp(12), 0, dp(12), 0)
            }
        }

        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
            return (super.getDropDownView(position, convertView, parent) as TextView).apply {
                textSize = 15f
                setTextColor(Color.WHITE)
                setBackgroundColor(Color.rgb(25, 26, 31))
                setPadding(dp(12), dp(12), dp(12), dp(12))
            }
        }
    }.apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

    val spinner = Spinner(this).apply {
        this.adapter = adapter
        setBackgroundColor(Color.rgb(35, 38, 43))
        layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(54)).apply {
            setMargins(0, dp(6), 0, dp(10))
        }
        onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, itemId: Long) {
                val selected = options[position]
                renderZoneActions(selectedZoneActions, cotizacionId, selected, readOnly)
                renderZoneDetails(detailHolder, cotizacionId, selected, detalles, readOnly)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }
    }

    holder.addView(spinner)
    holder.addView(selectedZoneActions)
    holder.addView(sectionTitle("Detalles de la zona"))
    holder.addView(detailHolder)
}

private fun MainActivity.renderZoneActions(
    container: LinearLayout,
    cotizacionId: String,
    zone: ZonaOption,
    readOnly: Boolean
) {
    container.removeAllViews()
    if (readOnly) return
    container.addView(topActions {
        addView(secondaryButton("Editar zona") { showZonaForm(cotizacionId, zone.id, zone.data) })
        addView(dangerButton("Eliminar zona") {
            confirm("Eliminar zona", "Solo se puede eliminar si no tiene detalles.") {
                apiCall({ api.delete("/cotizaciones/$cotizacionId/zonas/${zone.id}") }) { showCotizacionDetail(cotizacionId) }
            }
        })
    })
}

private fun MainActivity.renderZoneDetails(
    container: LinearLayout,
    cotizacionId: String,
    zone: ZonaOption,
    detalles: JSONArray,
    readOnly: Boolean
) {
    container.removeAllViews()
    val selectedDetails = mutableListOf<JSONObject>()
    for (i in 0 until detalles.length()) {
        val detail = detalles.optJSONObject(i) ?: continue
        val sameZoneId = detail.optAny("ZONAID").isNotBlank() && detail.optAny("ZONAID") == zone.id
        val sameZoneName = detail.optString("ZONA_DESCRIPCION").isNotBlank() &&
            detail.optString("ZONA_DESCRIPCION").equals(zone.description, ignoreCase = true)
        if (sameZoneId || sameZoneName) selectedDetails.add(detail)
    }

    if (selectedDetails.isEmpty()) {
        container.addView(emptyText("Sin detalles para esta zona."))
        return
    }

    selectedDetails.forEach { detail ->
        val total = calculateDetalleTotal(
            tipoProdId = detail.optAny("TIPOPRODID").toIntOrNull() ?: 0,
            cantidad = detail.optAny("CANTIDAD").toDoubleOrNull() ?: 0.0,
            ancho = detail.optAny("ANCHO").toDoubleOrNull() ?: 0.0,
            alto = detail.optAny("ALTO").toDoubleOrNull() ?: 0.0,
            precio = detail.optAny("PRECIO").toDoubleOrNull() ?: 0.0,
            precioInstalacion = detail.optAny("PRECIO_INSTALACION").toDoubleOrNull() ?: 0.0,
            instalacionPorAncho = detail.optAny("INSTALACION_POR_ANCHO") == "1" || detail.optAny("INSTALACION_POR_ANCHO").equals("true", true),
            instalacionPorAlto = detail.optAny("INSTALACION_POR_ALTO") == "1" || detail.optAny("INSTALACION_POR_ALTO").equals("true", true)
        )
        container.addView(card {
            addView(itemTitle(detail, listOf("ARTICULO", "PROVEEDOR")))
            addView(jsonPreview(detail, listOf("CANTIDAD", "ALTO", "ANCHO", "PRECIO", "PRECIO_INSTALACION"), hideDatabaseIds = true))
            addView(dataRow("Total detalle", formatMoney(total)))
            if (!readOnly) {
                addView(topActions {
                    addView(secondaryButton("Editar") { showDetalleForm(cotizacionId, detail.optAny("DETALLEID"), detail) })
                    addView(secondaryButton("Copiar") { copyDetalle(cotizacionId, detail) })
                    addView(dangerButton("Eliminar") {
                        confirm("Eliminar detalle", "Se quitara el producto de la cotizacion.") {
                            apiCall({ api.delete("/cotizaciones/$cotizacionId/detalles/${detail.optAny("DETALLEID")}") }) {
                                showCotizacionDetail(cotizacionId)
                            }
                        }
                    })
                })
            }
        })
    }
}

internal fun MainActivity.copyDetalle(cotizacionId: String, detalle: JSONObject) {
    val body = JSONObject()
        .put("articuloid", detalle.optAny("ARTICULOID"))
        .put("cantidad", detalle.optAny("CANTIDAD"))
        .put("alto", detalle.optAny("ALTO"))
        .put("ancho", detalle.optAny("ANCHO"))
        .put("listaprecioid", detalle.optAny("LISTAPRECIOID"))
        .put("precio", detalle.optAny("PRECIO"))
        .put("precioInstalacion", detalle.optAny("PRECIO_INSTALACION"))
        .put("zonaid", detalle.optAny("ZONAID"))
    apiCall({ api.post("/cotizaciones/$cotizacionId/detalles", body) }) {
        toast("Detalle copiado")
        showCotizacionDetail(cotizacionId)
    }
}

internal fun MainActivity.showZonaForm(cotizacionId: String, zonaId: String?, zona: JSONObject?) {
    val page = appPage(if (zonaId == null) "Nueva zona" else "Editar zona", "Cotizacion")
    val descripcion = input("Descripcion", zona?.optString("DESCRIPCION").orEmpty())
    page.addView(labeledInput("Descripcion", descripcion))
    page.addView(topActions {
        addView(secondaryButton("Cancelar") { showCotizacionDetail(cotizacionId) })
        addView(primaryButton("Guardar") {
            val body = JSONObject().put("descripcion", descripcion.text.toString().trim())
            val call = if (zonaId == null) {
                { api.post("/cotizaciones/$cotizacionId/zonas", body) }
            } else {
                { api.put("/cotizaciones/$cotizacionId/zonas/$zonaId", body) }
            }
            apiCall(call) { showCotizacionDetail(cotizacionId) }
        })
    })
}

internal fun MainActivity.showDetalleForm(cotizacionId: String, detalleId: String?, detalle: JSONObject?) {
    val page = appPage(if (detalleId == null) "Nuevo detalle" else "Editar detalle", "Cotizacion")

    if (detalle != null && detalleId != null) {
        showDetalleFormEdit(page, cotizacionId, detalleId, detalle)
    } else {
        showDetalleFormNew(page, cotizacionId)
    }
}

private fun MainActivity.showDetalleFormEdit(
    page: LinearLayout,
    cotizacionId: String,
    detalleId: String,
    detalle: JSONObject
) {
    page.addView(readOnlyField("Articulo", detalle.optString("ARTICULO").ifBlank { "Producto" }))
    page.addView(readOnlyField("Proveedor", detalle.optString("PROVEEDOR").ifBlank { "Proveedor" }))
    page.addView(readOnlyField("Zona", detalle.optString("ZONA_DESCRIPCION").ifBlank { "Zona" }))

    val tipoProdId = detalle.optAny("TIPOPRODID").toIntOrNull() ?: 0
    val esMedida = tipoProdId == 1

    val cantidad = numericInput("Cantidad", detalle.optAny("CANTIDAD"))
    val ancho = numericInput("Ancho (m)", detalle.optAny("ANCHO"))
    val alto = numericInput("Alto (m)", detalle.optAny("ALTO"))
    val precio = numericInput("Precio producto", detalle.optAny("PRECIO"))
    val precioInst = numericInput("Precio instalacion", detalle.optAny("PRECIO_INSTALACION"))

    page.addView(fieldLabel("Cantidad *"))
    page.addView(cantidad)
    if (esMedida) {
        page.addView(fieldLabel("Ancho (m)"))
        page.addView(ancho)
        page.addView(fieldLabel("Alto (m)"))
        page.addView(alto)
    }
    page.addView(fieldLabel("Precio producto"))
    page.addView(precio)
    if (esMedida) {
        page.addView(fieldLabel("Precio instalacion"))
        page.addView(precioInst)
    }

    val totalLabel = moneyText(0)
    page.addView(fieldLabel("Total estimado"))
    page.addView(totalLabel)

    val instalacionPorAncho = detalle.optAny("INSTALACION_POR_ANCHO") == "1" || detalle.optAny("INSTALACION_POR_ANCHO").equals("true", true)
    val instalacionPorAlto = detalle.optAny("INSTALACION_POR_ALTO") == "1" || detalle.optAny("INSTALACION_POR_ALTO").equals("true", true)

    val recalc = {
        val total = calculateDetalleTotal(
            tipoProdId, cantidad.text, ancho.text, alto.text, precio.text, precioInst.text,
            instalacionPorAncho, instalacionPorAlto
        )
        totalLabel.text = formatMoney(total)
    }
    listOf(cantidad, ancho, alto, precio, precioInst).forEach { addRecalcWatcher(it, recalc) }
    recalc()

    page.addView(topActions {
        addView(secondaryButton("Cancelar") { showCotizacionDetail(cotizacionId) })
        addView(primaryButton("Guardar") {
            val body = JSONObject()
                .put("articuloid", detalle.optAny("ARTICULOID"))
                .put("listaprecioid", detalle.optAny("LISTAPRECIOID"))
                .put("zonaid", detalle.optAny("ZONAID"))
                .put("cantidad", cantidad.text.toString().trim())
                .put("ancho", ancho.text.toString().trim())
                .put("alto", alto.text.toString().trim())
                .put("precio", precio.text.toString().trim())
                .put("precioInstalacion", precioInst.text.toString().trim())
            apiCall({ api.put("/cotizaciones/$cotizacionId/detalles/$detalleId", body) }) {
                showCotizacionDetail(cotizacionId)
            }
        })
    })
}

private fun MainActivity.showDetalleFormNew(page: LinearLayout, cotizacionId: String) {
    val state = DetalleFormState()

    val zonaContainer = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
    val proveedorContainer = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
    val articuloContainer = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
    val listaContainer = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }

    page.addView(infoBanner("Selecciona proveedor, articulo y lista de precio. Los precios se autollenaran."))

    page.addView(zonaContainer)
    page.addView(proveedorContainer)
    page.addView(articuloContainer)
    page.addView(listaContainer)

    val cantidad = numericInput("Cantidad", "1")
    val ancho = numericInput("Ancho (m)", "")
    val alto = numericInput("Alto (m)", "")
    val precio = numericInput("Precio producto", "")
    val precioInst = numericInput("Precio instalacion", "")

    page.addView(fieldLabel("Cantidad *"))
    page.addView(cantidad)
    page.addView(fieldLabel("Ancho (m)"))
    page.addView(ancho)
    page.addView(fieldLabel("Alto (m)"))
    page.addView(alto)
    page.addView(fieldLabel("Precio producto"))
    page.addView(precio)
    page.addView(fieldLabel("Precio instalacion"))
    page.addView(precioInst)

    val totalLabel = moneyText(0)
    page.addView(fieldLabel("Total estimado"))
    page.addView(totalLabel)

    val recalc = {
        val total = calculateDetalleTotal(
            state.tipoProdId, cantidad.text, ancho.text, alto.text, precio.text, precioInst.text,
            state.instalacionPorAncho, state.instalacionPorAlto
        )
        totalLabel.text = formatMoney(total)
    }
    listOf(cantidad, ancho, alto, precio, precioInst).forEach { addRecalcWatcher(it, recalc) }

    apiCall({ api.get("/cotizaciones/$cotizacionId/zonas") }) { json ->
        zonaContainer.removeAllViews()
        val zonas = json.optJSONArray("zonas") ?: JSONArray()
        val zonaOptions = jsonArrayToOptions(zonas, "ZONAID", "DESCRIPCION")
        if (zonaOptions.isEmpty()) {
            zonaContainer.addView(infoBanner("No hay zonas. Crea una zona primero."))
            zonaContainer.addView(topActions {
                addView(primaryButton("Crear zona") { showZonaForm(cotizacionId, null, null) })
            })
            return@apiCall
        }
        zonaContainer.addView(labeledSpinner("Zona *", zonaOptions, "Seleccionar zona") { opt ->
            state.zonaId = opt.id
        })
    }

    apiCall({ api.get("/cotizaciones/$cotizacionId/proveedores") }) { json ->
        proveedorContainer.removeAllViews()
        val proveedores = json.optJSONArray("proveedores") ?: JSONArray()
        val provOptions = jsonArrayToOptions(proveedores, "TERCEROID", "PROVEEDOR")
        proveedorContainer.addView(labeledSpinner("Proveedor *", provOptions, "Seleccionar proveedor") { opt ->
            state.proveedorId = opt.id
            state.articuloId = ""
            state.listaPrecioId = ""
            articuloContainer.removeAllViews()
            listaContainer.removeAllViews()
            precio.setText("")
            precioInst.setText("")
            loadArticulosForProveedor(cotizacionId, state, articuloContainer, listaContainer, precio, precioInst, recalc)
        })
    }

    page.addView(topActions {
        addView(secondaryButton("Consultar precios") { showPriceLookup(cotizacionId) })
        addView(secondaryButton("Cancelar") { showCotizacionDetail(cotizacionId) })
        addView(primaryButton("Guardar") {
            if (state.zonaId.isBlank()) { showError("Selecciona una zona"); return@primaryButton }
            if (state.articuloId.isBlank()) { showError("Selecciona un articulo"); return@primaryButton }
            if (state.listaPrecioId.isBlank()) { showError("Selecciona una lista de precio"); return@primaryButton }
            val cantVal = cantidad.text.toString().trim()
            if (cantVal.isBlank() || cantVal.toDoubleOrNull() == null || cantVal.toDouble() <= 0) {
                showError("Cantidad debe ser mayor a 0"); return@primaryButton
            }
            val body = JSONObject()
                .put("articuloid", state.articuloId)
                .put("listaprecioid", state.listaPrecioId)
                .put("zonaid", state.zonaId)
                .put("cantidad", cantVal)
                .put("ancho", ancho.text.toString().trim())
                .put("alto", alto.text.toString().trim())
                .put("precio", precio.text.toString().trim().ifBlank { "0" })
                .put("precioInstalacion", precioInst.text.toString().trim().ifBlank { "0" })
            apiCall({ api.post("/cotizaciones/$cotizacionId/detalles", body) }) {
                showCotizacionDetail(cotizacionId)
            }
        })
    })
}

private class DetalleFormState {
    var zonaId: String = ""
    var proveedorId: String = ""
    var articuloId: String = ""
    var listaPrecioId: String = ""
    var tipoProdId: Int = 0
    var instalacionPorAncho: Boolean = false
    var instalacionPorAlto: Boolean = false
}

private fun MainActivity.loadArticulosForProveedor(
    cotizacionId: String,
    state: DetalleFormState,
    articuloContainer: LinearLayout,
    listaContainer: LinearLayout,
    precio: EditText,
    precioInst: EditText,
    recalc: () -> Unit
) {
    apiCall({ api.get("/cotizaciones/$cotizacionId/articulos-por-proveedor/${state.proveedorId}") }) { json ->
        articuloContainer.removeAllViews()
        listaContainer.removeAllViews()
        val articulos = json.optJSONArray("articulos") ?: JSONArray()
        val artOptions = mutableListOf<DropdownOption>()
        for (i in 0 until articulos.length()) {
            val a = articulos.optJSONObject(i) ?: continue
            val label = "${a.optAny("CODIGO")} - ${a.optString("DESCRIPCION")}".trim(' ', '-')
            artOptions.add(DropdownOption(a.optAny("ARTICULOID"), label, a))
        }
        articuloContainer.addView(labeledSpinner("Articulo *", artOptions, "Seleccionar articulo") { opt ->
            state.articuloId = opt.id
            state.tipoProdId = opt.data?.optAny("TIPOPRODID")?.toIntOrNull() ?: 0
            state.listaPrecioId = ""
            listaContainer.removeAllViews()
            precio.setText("")
            precioInst.setText("")
            loadListasPrecios(cotizacionId, state, listaContainer, precio, precioInst, recalc)
        })
    }
}

private fun MainActivity.loadListasPrecios(
    cotizacionId: String,
    state: DetalleFormState,
    listaContainer: LinearLayout,
    precio: EditText,
    precioInst: EditText,
    recalc: () -> Unit
) {
    apiCall({ api.get("/cotizaciones/$cotizacionId/listas-precios/${state.articuloId}/${state.proveedorId}") }) { json ->
        listaContainer.removeAllViews()
        val precios = json.optJSONArray("precios") ?: JSONArray()
        val listOptions = mutableListOf<DropdownOption>()
        for (i in 0 until precios.length()) {
            val lp = precios.optJSONObject(i) ?: continue
            val label = lp.optString("DESCRIPCION").ifBlank { "Lista ${i + 1}" }
            listOptions.add(DropdownOption(lp.optAny("LISTAPRECIOID"), label, lp))
        }
        listaContainer.addView(labeledSpinner("Lista de precio *", listOptions, "Seleccionar lista") { opt ->
            state.listaPrecioId = opt.id
            val lp = opt.data
            if (lp != null) {
                precio.setText(lp.optAny("COSTO"))
                precioInst.setText(lp.optAny("COSTO_INSTALACION"))
                state.instalacionPorAncho = lp.optAny("INSTALACION_POR_ANCHO") == "1" || lp.optAny("INSTALACION_POR_ANCHO").equals("true", true)
                state.instalacionPorAlto = lp.optAny("INSTALACION_POR_ALTO") == "1" || lp.optAny("INSTALACION_POR_ALTO").equals("true", true)
                recalc()
            }
        })
    }
}

internal fun MainActivity.showPriceLookup(cotizacionId: String) {
    apiCall({ api.get("/cotizaciones/$cotizacionId/proveedores") }) { json ->
        val proveedores = json.optJSONArray("proveedores") ?: JSONArray()
        val lines = StringBuilder()
        for (i in 0 until proveedores.length()) {
            val p = proveedores.optJSONObject(i) ?: continue
            lines.append(p.optString("PROVEEDOR")).append('\n')
        }
        showTextDialog("Proveedores con precios", lines.toString().ifBlank { "Sin proveedores." })
    }
}

private fun calculateDetalleTotal(
    tipoProdId: Int,
    cantidad: Any?,
    ancho: Any?,
    alto: Any?,
    precio: Any?,
    precioInstalacion: Any?,
    instalacionPorAncho: Boolean,
    instalacionPorAlto: Boolean
): Double {
    val cant = cantidad?.toString()?.replace(",", ".")?.toDoubleOrNull() ?: 0.0
    val anch = ancho?.toString()?.replace(",", ".")?.toDoubleOrNull() ?: 0.0
    val alt = alto?.toString()?.replace(",", ".")?.toDoubleOrNull() ?: 0.0
    val prec = precio?.toString()?.replace(",", ".")?.toDoubleOrNull() ?: 0.0
    val precInst = precioInstalacion?.toString()?.replace(",", ".")?.toDoubleOrNull() ?: 0.0

    return if (tipoProdId == 1) {
        val m2 = anch * alt * cant
        var costoInst = precInst
        if (instalacionPorAncho) costoInst = precInst * (anch * cant)
        if (instalacionPorAlto) costoInst = precInst * (alt * cant)
        (m2 * prec) + costoInst
    } else {
        cant * prec
    }
}

private fun jsonArrayToOptions(arr: JSONArray, idKey: String, labelKey: String): List<DropdownOption> {
    val options = mutableListOf<DropdownOption>()
    for (i in 0 until arr.length()) {
        val item = arr.optJSONObject(i) ?: continue
        val id = item.optAny(idKey)
        if (id.isBlank()) continue
        val label = item.optString(labelKey).ifBlank { item.optAny(labelKey) }
        options.add(DropdownOption(id, label, item))
    }
    return options
}

private fun MainActivity.numericInput(hint: String, value: String): EditText {
    return input(hint, value).apply {
        inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
    }
}

private fun addRecalcWatcher(editText: EditText, recalc: () -> Unit) {
    editText.addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
        override fun afterTextChanged(s: Editable?) = recalc()
    })
}
