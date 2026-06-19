package co.com.disspace.app.presentacion

import android.graphics.Color
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import co.com.disspace.app.MainActivity
import co.com.disspace.app.data.datasource.optAny
import co.com.disspace.app.presentacion.common.DropdownOption
import org.json.JSONArray
import org.json.JSONObject

private val ESTADOS_PEDIDO = listOf("Creado", "Programado", "Cerrado")

internal fun MainActivity.showPedidos(filters: Map<String, String> = emptyMap()) {
    val page = appPage("Pedidos", "Fabricacion, instalacion y seguimiento.")
    val filterFields = listOf(
        "numero" to "Cotizacion #",
        "cliente" to "Cliente",
        "vendedor" to "Vendedor",
        "estadoPedido" to "Estado pedido",
        "fechaInicio" to "Fecha inicio (YYYY-MM-DD)",
        "fechaFin" to "Fecha fin (YYYY-MM-DD)",
        "mes" to "Mes (1-12)",
        "anio" to "Anio (ej: 2025)"
    )
    page.addView(topActions {
        addView(filterButton(filters.size) {
            showFiltersDialog("Filtros de pedidos", filterFields, filters) { showPedidos(it) }
        })
    })
    val holder = section("Cargando...")
    page.addView(holder)
    apiCall({ api.get("/pedidos", filters) }) { json ->
        holder.removeAllViews()
        val rows = json.optJSONArray("pedidos") ?: JSONArray()
        if (rows.length() == 0) holder.addView(emptyText("No hay pedidos."))
        for (i in 0 until rows.length()) {
            val item = rows.optJSONObject(i) ?: continue
            val id = item.optAny("PEDIDOID")
            val noMontado = item.optAny("NO_MONTADO").ifBlank { "0" }
            val montado = item.optAny("MONTADO").ifBlank { "0" }
            val totalProv = item.optAny("TOTAL_PROVEEDORES").ifBlank { "0" }
            holder.addView(card {
                addView(itemTitle(item, listOf("NUMERO", "CLIENTE")))
                addView(jsonPreview(item, listOf("ESTADO", "COTIZACION_NUMERO", "VENDEDOR", "FECHA_ENVIO", "FECHA_TENTATIVA_INSTALACION"), hideDatabaseIds = true))
                addView(dataRow("Proveedores", "No montado: $noMontado / Montado: $montado / Total: $totalProv"))
                addView(topActions {
                    addView(primaryButton("Detalle") { showPedidoDetail(id) })
                    addView(dangerButton("Eliminar") {
                        confirm("Eliminar pedido", "La cotizacion vuelve a En proceso.") {
                            apiCall({ api.delete("/pedidos/$id") }) { showPedidos(filters) }
                        }
                    })
                })
            })
        }
    }
}

internal fun MainActivity.showPedidoDetail(id: String) {
    val page = appPage("Pedido $id", "Estados, proveedores, comentarios y cuestionario.")
    page.addView(topActions {
        addView(secondaryButton("Atras") { showPedidos() })
        addView(primaryButton("Comentario") { showComentarioForm(id) })
    })
    val holder = section("Cargando...")
    page.addView(holder)
    apiCall({ api.get("/pedidos/$id") }) { json ->
        holder.removeAllViews()
        val pedido = json.optJSONObject("pedido") ?: JSONObject()
        val estadosValidosPedido = json.optJSONArray("estadosValidosPedido")?.let { arr ->
            List(arr.length()) { arr.optString(it) }.filter { it.isNotBlank() }
        } ?: ESTADOS_PEDIDO

        holder.addView(sectionTitle("Pedido"))
        holder.addView(jsonPreview(pedido, listOf("NUMERO", "ESTADO", "COTIZACION_NUMERO", "CLIENTE", "VENDEDOR", "FECHA_ENVIO", "FECHA_TENTATIVA_INSTALACION", "CLIENTE_TELEFONO", "CLIENTE_DIRECCION"), hideDatabaseIds = true))

        holder.addView(sectionTitle("Acciones"))
        holder.addView(topActions {
            addView(secondaryButton("Fecha instalacion") {
                showSimpleBodyForm("Fecha instalacion", "fecha", "/pedidos/$id/fecha-instalacion", "PUT") { showPedidoDetail(id) }
            })
        })
        holder.addView(labeledSpinner("Estado pedido", estadosValidosPedido.map { DropdownOption(it, it) }, "Seleccionar estado", pedido.optAny("ESTADO")) { opt ->
            apiCall({ api.put("/pedidos/$id/estado", JSONObject().put("estado", opt.id)) }) {
                toast("Estado actualizado")
                showPedidoDetail(id)
            }
        })

        holder.addView(sectionTitle("Proveedores"))
        val proveedores = json.optJSONArray("proveedores") ?: JSONArray()
        for (i in 0 until proveedores.length()) {
            val p = proveedores.optJSONObject(i) ?: continue
            holder.addView(card {
                addView(itemTitle(p, listOf("PROVEEDOR", "ESTADO")))
                val productos = p.optJSONArray("PRODUCTOS") ?: JSONArray()
                if (productos.length() > 0) {
                    addView(fieldLabel("Productos"))
                    for (j in 0 until productos.length()) {
                        val prod = productos.optJSONObject(j) ?: continue
                        addView(dataRow(
                            prod.optAny("CODIGO").ifBlank { "N/A" },
                            "${prod.optAny("producto")} | Cant: ${prod.optAny("cantidad")} | ${prod.optAny("ancho")}x${prod.optAny("alto")}"
                        ))
                    }
                }
                addView(topActions {
                    addView(secondaryButton("No montado") { updateProveedorEstado(id, p.optAny("PEDIDOPROVEEDORID"), "No montado") })
                    addView(primaryButton("Montado") { updateProveedorEstado(id, p.optAny("PEDIDOPROVEEDORID"), "Montado") })
                })
            })
        }

        holder.addView(sectionTitle("Comentarios"))
        val comentarios = json.optJSONArray("comentarios") ?: JSONArray()
        if (comentarios.length() == 0) holder.addView(emptyText("Sin comentarios."))
        for (i in 0 until comentarios.length()) {
            val c = comentarios.optJSONObject(i) ?: continue
            holder.addView(card {
                addView(itemTitle(c, listOf("AUTOR_DISPLAY", "FECHA_USUARIO")))
                val comentarioTexto = c.optAny("COMENTARIO")
                if (comentarioTexto.isNotBlank()) {
                    addView(helpText(comentarioTexto))
                }
                val adjuntos = c.optJSONArray("ADJUNTOS") ?: JSONArray()
                if (adjuntos.length() > 0) {
                    addView(fieldLabel("Adjuntos (${adjuntos.length()})"))
                    for (j in 0 until adjuntos.length()) {
                        val adj = adjuntos.optJSONObject(j) ?: continue
                        val esImagen = adj.optAny("ES_IMAGEN") == "true" || adj.optString("ES_IMAGEN") == "true"
                        val icon = if (esImagen) "[IMG] " else "[ARCH] "
                        addView(helpText("$icon${adj.optAny("NOMBRE_ORIGINAL")}"))
                    }
                }
                addView(topActions {
                    addView(secondaryButton("Editar") { showComentarioForm(id, c.optAny("PEDIDOCOMENTARIOID"), c.optAny("COMENTARIO")) })
                    addView(dangerButton("Eliminar") {
                        confirm("Eliminar comentario", "Solo funciona si el comentario es tuyo.") {
                            apiCall({ api.delete("/pedidos/$id/comentarios/${c.optAny("PEDIDOCOMENTARIOID")}") }) { showPedidoDetail(id) }
                        }
                    })
                })
            })
        }

        val preguntas = json.optJSONArray("preguntas") ?: JSONArray()
        if (preguntas.length() > 0) {
            holder.addView(sectionTitle("Cuestionario"))
            holder.addView(topActions {
                addView(primaryButton("Responder cuestionario") { showCuestionarioForm(id, preguntas) })
            })
            for (i in 0 until preguntas.length()) {
                val p = preguntas.optJSONObject(i) ?: continue
                val texto = p.optAny("TEXTO")
                val tipo = p.optAny("TIPO")
                val modo = p.optAny("MODO_RESPUESTA").ifBlank { "unica" }
                val respTexto = p.optAny("respuestaTexto")
                val respOpcion = p.optAny("respuestaOpcionId")
                val respOpciones = p.optJSONArray("respuestaOpcionesIds") ?: JSONArray()
                val respuestaResumen = when {
                    respTexto.isNotBlank() -> respTexto
                    respOpcion.isNotBlank() -> "Opcion: $respOpcion"
                    respOpciones.length() > 0 -> "Opciones: ${List(respOpciones.length()) { respOpciones.optString(it) }.joinToString(", ")}"
                    else -> "Sin responder"
                }
                holder.addView(card {
                    addView(itemTitle(p, listOf("TEXTO")))
                    addView(helpText("Tipo: $tipo | Modo: $modo"))
                    addView(dataRow("Respuesta", respuestaResumen))
                })
            }
        }
    }
}

internal fun MainActivity.showCuestionarioForm(pedidoId: String, preguntas: JSONArray) {
    val page = appPage("Cuestionario", "Pedido $pedidoId")
    val holder = section("Cargando preguntas...")
    page.addView(holder)
    page.addView(topActions {
        addView(secondaryButton("Cancelar") { showPedidoDetail(pedidoId) })
    })

    holder.removeAllViews()
    val respuestas = mutableMapOf<String, JSONObject>()

    for (i in 0 until preguntas.length()) {
        val pregunta = preguntas.optJSONObject(i) ?: continue
        val preguntaId = pregunta.optAny("PREGUNTAID")
        val texto = pregunta.optAny("TEXTO")
        val tipo = pregunta.optAny("TIPO")
        val modo = pregunta.optAny("MODO_RESPUESTA").ifBlank { "unica" }

        val opciones = pregunta.optJSONArray("opciones") ?: JSONArray()
        val respOpcion = pregunta.optAny("respuestaOpcionId")
        val respOpciones = pregunta.optJSONArray("respuestaOpcionesIds") ?: JSONArray()
        val respTexto = pregunta.optAny("respuestaTexto")

        val respObj = JSONObject()
        respuestas[preguntaId] = respObj

        holder.addView(card {
            addView(sectionTitle(texto))
            addView(helpText("Tipo: $tipo" + if (modo != "unica") " | Modo: $modo" else ""))

            when {
                tipo.equals("abierta", true) -> {
                    val edit = multiLineInput("Tu respuesta...", respTexto)
                    addView(labeledInput("Respuesta", edit))
                    edit.addTextChangedListener(object : android.text.TextWatcher {
                        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
                        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
                        override fun afterTextChanged(s: android.text.Editable?) {
                            val v = s?.toString()?.trim().orEmpty()
                            if (v.isNotBlank()) respObj.put("texto", v) else respObj.remove("texto")
                        }
                    })
                }
                modo.equals("multiple", true) -> {
                    val checks = mutableListOf<CheckBox>()
                    for (j in 0 until opciones.length()) {
                        val op = opciones.optJSONObject(j) ?: continue
                        val opId = op.optAny("OPCIONID")
                        val opTexto = op.optAny("TEXTO")
                        val isChecked = respOpciones.length() > 0 && (0 until respOpciones.length()).any { respOpciones.optString(it) == opId }
                        val cb = checkbox(opTexto, isChecked)
                        cb.setOnCheckedChangeListener { _, checked ->
                            val current = respObj.optJSONArray("opcionesIds") ?: JSONArray()
                            val ids = mutableSetOf<String>()
                            for (k in 0 until current.length()) ids.add(current.optString(k))
                            if (checked) ids.add(opId) else ids.remove(opId)
                            val arr = JSONArray()
                            ids.forEach { arr.put(it) }
                            if (arr.length() > 0) respObj.put("opcionesIds", arr) else respObj.remove("opcionesIds")
                        }
                        if (isChecked) {
                            val arr = respObj.optJSONArray("opcionesIds") ?: JSONArray()
                            if ((0 until arr.length()).none { arr.optString(it) == opId }) arr.put(opId)
                            respObj.put("opcionesIds", arr)
                        }
                        checks.add(cb)
                        addView(cb)
                    }
                }
                else -> {
                    val radio = RadioGroup(context)
                    radio.orientation = RadioGroup.VERTICAL
                    for (j in 0 until opciones.length()) {
                        val op = opciones.optJSONObject(j) ?: continue
                        val opId = op.optAny("OPCIONID")
                        val opTexto = op.optAny("TEXTO")
                        val rb = RadioButton(context).apply {
                            id = View.generateViewId()
                            text = opTexto
                            textSize = 14f
                            setTextColor(Color.rgb(224, 226, 229))
                            setPadding(dp(8), dp(6), dp(8), dp(6))
                            isChecked = opId == respOpcion
                            tag = opId
                        }
                        radio.addView(rb)
                        if (opId == respOpcion) respObj.put("opcionId", opId)
                    }
                    radio.setOnCheckedChangeListener { _, checkedId ->
                        val rb = radio.findViewById<RadioButton>(checkedId)
                        val opId = rb?.tag as? String ?: ""
                        if (opId.isNotBlank()) respObj.put("opcionId", opId) else respObj.remove("opcionId")
                    }
                    addView(radio)

                    for (j in 0 until opciones.length()) {
                        val op = opciones.optJSONObject(j) ?: continue
                        val subpregunta = op.optJSONObject("subpregunta")
                        if (subpregunta != null) {
                            val subPreguntaId = subpregunta.optAny("PREGUNTAID")
                            val subTexto = subpregunta.optAny("TEXTO")
                            val subTipo = subpregunta.optAny("TIPO")
                            val subOpciones = subpregunta.optJSONArray("opciones") ?: JSONArray()
                            val subRespOpcion = subpregunta.optAny("respuestaOpcionId")
                            val subRespTexto = subpregunta.optAny("respuestaTexto")
                            val subRespObj = JSONObject()
                            respuestas[subPreguntaId] = subRespObj

                            addView(LinearLayout(context).apply {
                                orientation = LinearLayout.VERTICAL
                                setPadding(dp(16), dp(8), dp(8), dp(8))
                                addView(TextView(context).apply {
                                    text = "  > $subTexto"
                                    textSize = 14f
                                    setTypeface(typeface, android.graphics.Typeface.BOLD)
                                    setTextColor(Color.rgb(185, 189, 194))
                                })
                                if (subTipo.equals("abierta", true)) {
                                    val subEdit = multiLineInput("Respuesta...", subRespTexto)
                                    addView(labeledInput("Respuesta", subEdit))
                                    subEdit.addTextChangedListener(object : android.text.TextWatcher {
                                        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
                                        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
                                        override fun afterTextChanged(s: android.text.Editable?) {
                                            val v = s?.toString()?.trim().orEmpty()
                                            if (v.isNotBlank()) subRespObj.put("texto", v) else subRespObj.remove("texto")
                                        }
                                    })
                                } else {
                                    val subRadio = RadioGroup(context)
                                    subRadio.orientation = RadioGroup.VERTICAL
                                    for (k in 0 until subOpciones.length()) {
                                        val subOp = subOpciones.optJSONObject(k) ?: continue
                                        val subOpId = subOp.optAny("OPCIONID")
                                        val subOpTexto = subOp.optAny("TEXTO")
                                        val subRb = RadioButton(context).apply {
                                            id = View.generateViewId()
                                            text = subOpTexto
                                            textSize = 13f
                                            setTextColor(Color.rgb(200, 203, 207))
                                            setPadding(dp(8), dp(4), dp(8), dp(4))
                                            isChecked = subOpId == subRespOpcion
                                            tag = subOpId
                                        }
                                        subRadio.addView(subRb)
                                        if (subOpId == subRespOpcion) subRespObj.put("opcionId", subOpId)
                                    }
                                    subRadio.setOnCheckedChangeListener { _, checkedId ->
                                        val rb = subRadio.findViewById<RadioButton>(checkedId)
                                        val opId = rb?.tag as? String ?: ""
                                        if (opId.isNotBlank()) subRespObj.put("opcionId", opId) else subRespObj.remove("opcionId")
                                    }
                                    addView(subRadio)
                                }
                            })
                        }
                    }
                }
            }
        })
    }

    page.addView(topActions {
        addView(secondaryButton("Cancelar") { showPedidoDetail(pedidoId) })
        addView(primaryButton("Guardar respuestas") {
            val body = JSONObject()
            val respJson = JSONObject()
            respuestas.forEach { (pid, resp) ->
                if (resp.length() > 0) respJson.put(pid, resp)
            }
            body.put("respuestas", respJson)
            apiCall({ api.put("/pedidos/$pedidoId/respuestas", body) }) {
                toast("Respuestas guardadas")
                showPedidoDetail(pedidoId)
            }
        })
    })
}

internal fun MainActivity.updateProveedorEstado(pedidoId: String, proveedorPedidoId: String, estado: String) {
    apiCall({ api.put("/pedidos/$pedidoId/proveedores/$proveedorPedidoId/estado", JSONObject().put("estado", estado)) }) {
        showPedidoDetail(pedidoId)
    }
}

internal fun MainActivity.showComentarioForm(pedidoId: String, comentarioId: String? = null, current: String = "") {
    val page = appPage(if (comentarioId == null) "Nuevo comentario" else "Editar comentario", "Pedido $pedidoId")
    val comentario = multiLineInput("Comentario", current, minLines = 4)
    page.addView(labeledInput("Comentario", comentario))
    page.addView(helpText("La API soporta adjuntos; esta pantalla envia comentarios de texto con multipart/form-data."))
    page.addView(topActions {
        addView(secondaryButton("Cancelar") { showPedidoDetail(pedidoId) })
        addView(primaryButton("Guardar") {
            val form = mapOf(
                "comentario" to comentario.text.toString().trim(),
                "fechaUsuario" to java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US).format(java.util.Date())
            )
            val call = if (comentarioId == null) {
                { api.multipart("/pedidos/$pedidoId/comentarios", "POST", form) }
            } else {
                { api.multipart("/pedidos/$pedidoId/comentarios/$comentarioId", "PUT", form) }
            }
            apiCall(call) { showPedidoDetail(pedidoId) }
        })
    })
}

internal fun MainActivity.showSimpleBodyForm(title: String, key: String, path: String, method: String, after: () -> Unit) {
    val page = appPage(title, path)
    val value = input(key)
    if (key.equals("fecha", true)) {
        value.inputType = InputType.TYPE_CLASS_DATETIME
    }
    page.addView(labeledInput(key, value))
    page.addView(topActions {
        addView(secondaryButton("Cancelar") { after() })
        addView(primaryButton("Guardar") {
            val body = JSONObject().put(key, value.text.toString().trim())
            val call = if (method == "PUT") { { api.put(path, body) } } else { { api.post(path, body) } }
            apiCall(call) { after() }
        })
    })
}

private fun MainActivity.dataRow(label: String, value: String): LinearLayout {
    return LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        setPadding(0, dp(6), 0, dp(6))
        addView(TextView(context).apply {
            text = label
            textSize = 14f
            setTextColor(Color.rgb(185, 189, 194))
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 0.45f)
        })
        addView(TextView(context).apply {
            text = value
            textSize = 14f
            setTextColor(Color.rgb(244, 245, 246))
            gravity = Gravity.END
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 0.55f)
        })
    }
}
