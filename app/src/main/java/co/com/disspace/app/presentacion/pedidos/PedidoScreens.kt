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
internal fun MainActivity.showPedidos(filters: Map<String, String> = emptyMap()) {
        val page = appPage("Pedidos", "Fabricacion, instalacion y seguimiento.")
        val filterFields = listOf("numero" to "Numero", "cliente" to "Cliente", "vendedor" to "Vendedor", "estadoPedido" to "Estado")
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
                holder.addView(card {
                    addView(itemTitle(item, listOf("NUMERO", "CLIENTE")))
                    addView(jsonPreview(item, listOf("ESTADO", "FECHA_ENVIO", "FECHA_TENTATIVA_INSTALACION", "NO_MONTADO", "MONTADO", "TOTAL_PROVEEDORES")))
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
            addView(secondaryButton("Fecha") { showSimpleBodyForm("Fecha instalacion", "fecha", "/pedidos/$id/fecha-instalacion", "PUT") { showPedidoDetail(id) } })
            addView(secondaryButton("Estado") { showSimpleBodyForm("Estado pedido", "estado", "/pedidos/$id/estado", "PUT") { showPedidoDetail(id) } })
            addView(secondaryButton("Respuestas JSON") { showRawJsonForm("Guardar respuestas", "/pedidos/$id/respuestas", "PUT") { showPedidoDetail(id) } })
        })
        val holder = section("Cargando...")
        page.addView(holder)
        apiCall({ api.get("/pedidos/$id") }) { json ->
            holder.removeAllViews()
            holder.addView(sectionTitle("Pedido"))
            holder.addView(jsonPreview(json.optJSONObject("pedido") ?: JSONObject(), emptyList()))

            holder.addView(sectionTitle("Proveedores"))
            val proveedores = json.optJSONArray("proveedores") ?: JSONArray()
            for (i in 0 until proveedores.length()) {
                val p = proveedores.optJSONObject(i) ?: continue
                holder.addView(card {
                    addView(itemTitle(p, listOf("PROVEEDOR", "ESTADO")))
                    addView(jsonPreview(p, listOf("PRODUCTOS")))
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
                    addView(jsonPreview(c, listOf("COMENTARIO", "ADJUNTOS")))
                    addView(topActions {
                        addView(secondaryButton("Editar") { showComentarioForm(id, c.optAny("PEDIDOCOMENTARIOID"), c.optString("COMENTARIO")) })
                        addView(dangerButton("Eliminar") {
                            confirm("Eliminar comentario", "Solo funciona si el comentario es tuyo.") {
                                apiCall({ api.delete("/pedidos/$id/comentarios/${c.optAny("PEDIDOCOMENTARIOID")}") }) { showPedidoDetail(id) }
                            }
                        })
                    })
                })
            }

            holder.addView(sectionTitle("Preguntas"))
            val preguntas = json.optJSONArray("preguntas") ?: JSONArray()
            if (preguntas.length() == 0) holder.addView(emptyText("No hay preguntas activas."))
            for (i in 0 until preguntas.length()) {
                val p = preguntas.optJSONObject(i) ?: continue
                holder.addView(jsonPreview(p, listOf("TEXTO", "TIPO", "MODO_RESPUESTA", "respuestaOpcionId", "respuestaOpcionesIds", "respuestaTexto", "opciones")))
            }
        }
    }

internal fun MainActivity.updateProveedorEstado(pedidoId: String, proveedorPedidoId: String, estado: String) {
        apiCall({ api.put("/pedidos/$pedidoId/proveedores/$proveedorPedidoId/estado", JSONObject().put("estado", estado)) }) {
            showPedidoDetail(pedidoId)
        }
    }

internal fun MainActivity.showComentarioForm(pedidoId: String, comentarioId: String? = null, current: String = "") {
        val page = appPage(if (comentarioId == null) "Nuevo comentario" else "Editar comentario", "Pedido $pedidoId")
        val comentario = input("Comentario", current)
        comentario.minLines = 4
        page.addView(comentario)
        page.addView(helpText("La API soporta adjuntos; esta pantalla envia comentarios de texto con multipart/form-data."))
        page.addView(topActions {
            addView(secondaryButton("Cancelar") { showPedidoDetail(pedidoId) })
            addView(primaryButton("Guardar") {
                val form = mapOf(
                    "comentario" to comentario.text.toString().trim(),
                    "fechaUsuario" to SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
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
        page.addView(value)
        page.addView(topActions {
            addView(secondaryButton("Cancelar") { after() })
            addView(primaryButton("Guardar") {
                val body = JSONObject().put(key, value.text.toString().trim())
                val call = if (method == "PUT") { { api.put(path, body) } } else { { api.post(path, body) } }
                apiCall(call) { after() }
            })
        })
    }

internal fun MainActivity.showRawJsonForm(title: String, path: String, method: String, after: () -> Unit) {
        val page = appPage(title, path)
        val raw = input("JSON")
        raw.minLines = 8
        raw.setText("""{"respuestas":{}}""")
        page.addView(raw)
        page.addView(helpText("""Ejemplo: {"respuestas":{"12":{"opcionId":3},"13":{"texto":"Listo"}}}"""))
        page.addView(topActions {
            addView(secondaryButton("Cancelar") { after() })
            addView(primaryButton("Enviar") {
                val body = JSONObject(raw.text.toString())
                val call = if (method == "PUT") { { api.put(path, body) } } else { { api.post(path, body) } }
                apiCall(call) { after() }
            })
        })
    }


