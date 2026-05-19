package co.com.disspace.app.presentacion

import android.graphics.Color
import android.text.InputType
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import co.com.disspace.app.MainActivity
import co.com.disspace.app.data.datasource.ApiClient
import co.com.disspace.app.data.datasource.firstString
import co.com.disspace.app.data.datasource.optAny
import co.com.disspace.app.domain.model.ApiField
import co.com.disspace.app.domain.model.CrudModule
import co.com.disspace.app.domain.model.DisspaceModules.articulosModule
import co.com.disspace.app.domain.model.DisspaceModules.genericModules
import co.com.disspace.app.domain.model.DisspaceModules.listaPreciosModule
import co.com.disspace.app.domain.model.FieldKind
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
internal fun MainActivity.showCotizaciones(filters: Map<String, String> = emptyMap()) {
        val page = page("Cotizaciones", "Gestion comercial por sucursal.")
        page.addView(topActions {
            addView(secondaryButton("Inicio") { showHome() })
            addView(primaryButton("Nueva") { showCotizacionForm(null, null) })
            addView(secondaryButton("Recargar") { showCotizaciones(filters) })
        })

        val filterFields = listOf("numero" to "Numero", "cliente" to "Cliente", "vendedor" to "Vendedor", "estado" to "Estado")
        val inputs = mutableMapOf<String, EditText>()
        page.addView(sectionTitle("Filtros"))
        filterFields.forEach { (key, label) ->
            val edit = input(label, filters[key] ?: "")
            inputs[key] = edit
            page.addView(edit)
        }
        page.addView(primaryButton("Aplicar filtros") {
            showCotizaciones(inputs.mapValues { it.value.text.toString().trim() }.filterValues { it.isNotBlank() })
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
                holder.addView(card {
                    addView(itemTitle(item, listOf("NUMERO", "CLIENTE")))
                    addView(jsonPreview(item, listOf("FECHA", "ESTADO", "VENDEDOR", "PEDIDOID")))
                    addView(topActions {
                        addView(primaryButton("Detalle") { showCotizacionDetail(id) })
                        addView(secondaryButton("Copiar") { postCotizacionAction(id, "/copiar") })
                        addView(secondaryButton("Enviar pedido") { postCotizacionAction(id, "/enviar-pedido") })
                        addView(dangerButton("Eliminar") {
                            confirm("Eliminar cotizacion", "Tambien se eliminaran sus detalles.") {
                                apiCall({ api.delete("/cotizaciones/$id") }) { showCotizaciones(filters) }
                            }
                        })
                    })
                })
            }
        }
    }

internal fun MainActivity.postCotizacionAction(id: String, suffix: String) {
        apiCall({ api.post("/cotizaciones/$id$suffix", JSONObject()) }) {
            toast(it.optString("message", "Operacion realizada"))
            showCotizaciones()
        }
    }

internal fun MainActivity.showCotizacionForm(id: String?, existing: JSONObject?) {
        val page = page(if (id == null) "Nueva cotizacion" else "Editar cotizacion", "Encabezado")
        val fields = listOf(
            ApiField("numero", "Numero", kind = FieldKind.NUMBER, required = true, existingKeys = listOf("NUMERO")),
            ApiField("fecha", "Fecha (YYYY-MM-DD)", kind = FieldKind.DATE, required = true, existingKeys = listOf("FECHA")),
            ApiField("cliente", "Cliente ID", kind = FieldKind.NUMBER, required = true, existingKeys = listOf("CLIENTEID")),
            ApiField("vendedor", "Vendedor ID", kind = FieldKind.NUMBER, required = true, existingKeys = listOf("VENDEDORID")),
            ApiField("observaciones", "Observaciones", existingKeys = listOf("OBSERVACIONES")),
            ApiField("estado", "Estado", required = id != null, existingKeys = listOf("ESTADO")),
            ApiField("descuento", "Descuento %", kind = FieldKind.NUMBER, existingKeys = listOf("DESCUENTO")),
            ApiField("iva", "IVA %", kind = FieldKind.NUMBER, existingKeys = listOf("IVA"))
        )
        val fillForm: (JSONObject?) -> Unit = { data ->
            page.removeAllViews()
            val detail = data?.optJSONObject("cotizacion") ?: existing ?: JSONObject()
            val inputs = mutableMapOf<ApiField, EditText>()
            val suggested = data?.optString("numeroSugerido").orEmpty()
            fields.forEach { field ->
                val default = when {
                    field.key == "numero" && id == null -> suggested
                    field.key == "fecha" && id == null -> today()
                    field.key == "estado" && id == null -> "En proceso"
                    else -> detail.firstString(field.existingKeys)
                }
                val edit = input(field.label, default)
                if (field.kind == FieldKind.NUMBER) edit.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
                inputs[field] = edit
                page.addView(edit)
            }
            page.addView(helpText("IDs de cliente/vendedor se pueden consultar en Catalogos o Terceros."))
            page.addView(topActions {
                addView(secondaryButton("Cancelar") { showCotizaciones() })
                addView(primaryButton("Guardar") {
                    val body = JSONObject()
                    inputs.forEach { (field, edit) ->
                        val value = edit.text.toString().trim()
                        if (value.isNotBlank() || field.required) body.put(field.key, value)
                    }
                    val call = if (id == null) { { api.post("/cotizaciones", body) } } else { { api.put("/cotizaciones/$id", body) } }
                    apiCall(call) {
                        toast(it.optString("message", "Cotizacion guardada"))
                        val newId = it.optString("cotizacionId", id ?: "")
                        if (newId.isNotBlank()) showCotizacionDetail(newId) else showCotizaciones()
                    }
                })
            })
        }

        if (id == null) apiCall({ api.get("/cotizaciones/form-data") }) { fillForm(it) } else apiCall({ api.get("/cotizaciones/$id") }) { fillForm(it) }
    }

internal fun MainActivity.showCotizacionDetail(id: String) {
        val page = page("Cotizacion $id", "Zonas, detalles y acciones.")
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
            holder.addView(sectionTitle("Encabezado"))
            holder.addView(jsonPreview(cot.optJSONObject("cotizacion") ?: JSONObject(), emptyList()))
            apiCall({ api.get("/cotizaciones/$id/zonas") }) { zonasJson ->
                holder.addView(sectionTitle("Zonas"))
                val zonas = zonasJson.optJSONArray("zonas") ?: JSONArray()
                if (zonas.length() == 0) holder.addView(emptyText("Sin zonas."))
                for (i in 0 until zonas.length()) {
                    val zona = zonas.optJSONObject(i) ?: continue
                    holder.addView(card {
                        addView(itemTitle(zona, listOf("DESCRIPCION")))
                        addView(topActions {
                            addView(secondaryButton("Editar") { showZonaForm(id, zona.optAny("ZONAID"), zona) })
                            addView(dangerButton("Eliminar") {
                                confirm("Eliminar zona", "Solo se puede eliminar si no tiene detalles.") {
                                    apiCall({ api.delete("/cotizaciones/$id/zonas/${zona.optAny("ZONAID")}") }) { showCotizacionDetail(id) }
                                }
                            })
                        })
                    })
                }
            }
            apiCall({ api.get("/cotizaciones/$id/detalles") }) { detJson ->
                holder.addView(sectionTitle("Detalles"))
                val detalles = detJson.optJSONArray("detalles") ?: JSONArray()
                if (detalles.length() == 0) holder.addView(emptyText("Sin detalles."))
                for (i in 0 until detalles.length()) {
                    val d = detalles.optJSONObject(i) ?: continue
                    holder.addView(card {
                        addView(itemTitle(d, listOf("ARTICULO", "PROVEEDOR")))
                        addView(jsonPreview(d, listOf("CANTIDAD", "ALTO", "ANCHO", "PRECIO", "PRECIO_INSTALACION", "ZONA_DESCRIPCION")))
                        addView(topActions {
                            addView(secondaryButton("Editar") { showDetalleForm(id, d.optAny("DETALLEID"), d) })
                            addView(dangerButton("Eliminar") {
                                confirm("Eliminar detalle", "Se quitara el producto de la cotizacion.") {
                                    apiCall({ api.delete("/cotizaciones/$id/detalles/${d.optAny("DETALLEID")}") }) { showCotizacionDetail(id) }
                                }
                            })
                        })
                    })
                }
            }
        }
    }

internal fun MainActivity.showZonaForm(cotizacionId: String, zonaId: String?, zona: JSONObject?) {
        val page = page(if (zonaId == null) "Nueva zona" else "Editar zona", "Cotizacion $cotizacionId")
        val descripcion = input("Descripcion", zona?.optString("DESCRIPCION").orEmpty())
        page.addView(descripcion)
        page.addView(topActions {
            addView(secondaryButton("Cancelar") { showCotizacionDetail(cotizacionId) })
            addView(primaryButton("Guardar") {
                val body = JSONObject().put("descripcion", descripcion.text.toString().trim())
                val call = if (zonaId == null) { { api.post("/cotizaciones/$cotizacionId/zonas", body) } } else { { api.put("/cotizaciones/$cotizacionId/zonas/$zonaId", body) } }
                apiCall(call) { showCotizacionDetail(cotizacionId) }
            })
        })
    }

internal fun MainActivity.showDetalleForm(cotizacionId: String, detalleId: String?, detalle: JSONObject?) {
        val page = page(if (detalleId == null) "Nuevo detalle" else "Editar detalle", "Cotizacion $cotizacionId")
        val fields = listOf(
            ApiField("articuloid", "Articulo ID", kind = FieldKind.NUMBER, required = true, existingKeys = listOf("ARTICULOID")),
            ApiField("cantidad", "Cantidad", kind = FieldKind.NUMBER, required = true, existingKeys = listOf("CANTIDAD")),
            ApiField("alto", "Alto", kind = FieldKind.NUMBER, existingKeys = listOf("ALTO")),
            ApiField("ancho", "Ancho", kind = FieldKind.NUMBER, existingKeys = listOf("ANCHO")),
            ApiField("listaprecioid", "Lista precio ID", kind = FieldKind.NUMBER, required = true, existingKeys = listOf("LISTAPRECIOID")),
            ApiField("precio", "Precio", kind = FieldKind.NUMBER, existingKeys = listOf("PRECIO")),
            ApiField("precioInstalacion", "Precio instalacion", kind = FieldKind.NUMBER, existingKeys = listOf("PRECIO_INSTALACION")),
            ApiField("zonaid", "Zona ID", kind = FieldKind.NUMBER, required = true, existingKeys = listOf("ZONAID"))
        )
        val inputs = mutableMapOf<ApiField, EditText>()
        fields.forEach { field ->
            val edit = input(field.label, detalle?.firstString(field.existingKeys).orEmpty())
            edit.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            inputs[field] = edit
            page.addView(edit)
        }
        page.addView(topActions {
            addView(secondaryButton("Consultar precios") { showPriceLookup(cotizacionId) })
            addView(secondaryButton("Cancelar") { showCotizacionDetail(cotizacionId) })
            addView(primaryButton("Guardar") {
                val body = JSONObject()
                inputs.forEach { (field, edit) -> body.put(field.key, edit.text.toString().trim()) }
                val call = if (detalleId == null) { { api.post("/cotizaciones/$cotizacionId/detalles", body) } } else { { api.put("/cotizaciones/$cotizacionId/detalles/$detalleId", body) } }
                apiCall(call) { showCotizacionDetail(cotizacionId) }
            })
        })
    }

internal fun MainActivity.showPriceLookup(cotizacionId: String) {
        apiCall({ api.get("/cotizaciones/$cotizacionId/proveedores") }) { json ->
            val proveedores = json.optJSONArray("proveedores") ?: JSONArray()
            val lines = StringBuilder()
            for (i in 0 until proveedores.length()) {
                val p = proveedores.optJSONObject(i) ?: continue
                lines.append(p.optAny("TERCEROID")).append(" - ").append(p.optString("PROVEEDOR")).append('\n')
            }
            showTextDialog("Proveedores con precios", lines.toString().ifBlank { "Sin proveedores." })
        }
    }


