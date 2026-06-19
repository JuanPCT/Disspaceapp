package co.com.disspace.app.presentacion

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
internal fun MainActivity.showGenericList(module: CrudModule, filters: Map<String, String> = emptyMap()) {
        val page = appPage(module.title, module.description)
        page.addView(topActions {
            addView(primaryButton("Nuevo") { showGenericForm(module, null, null) })
            if (module.filters.isNotEmpty()) {
                addView(filterButton(filters.size) {
                    val fields = module.filters.map { it.key to it.label }
                    showFiltersDialog("Filtros de ${module.title.lowercase()}", fields, filters) { showGenericList(module, it) }
                })
            }
        })

        val listHolder = section("Cargando...")
        page.addView(listHolder)
        apiCall({ api.get(module.path, filters) }) { json ->
            listHolder.removeAllViews()
            val rows = json.optJSONArray(module.listKey) ?: JSONArray()
            if (rows.length() == 0) {
                listHolder.addView(emptyText("No hay registros para mostrar."))
                return@apiCall
            }
            for (i in 0 until rows.length()) {
                val item = rows.optJSONObject(i) ?: continue
                listHolder.addView(card {
                    addView(itemTitle(item, module.titleFields))
                    addView(jsonPreview(item, module.previewFields))
                    addView(topActions {
                        addView(secondaryButton("Ver / editar") {
                            val id = item.optAny(module.idKey)
                            loadGenericDetail(module, id, item)
                        })
                        addView(dangerButton("Eliminar") {
                            confirm("Eliminar ${module.singular}", "Esta accion no se puede deshacer.") {
                                apiCall({ api.delete("${module.path}/${item.optAny(module.idKey)}") }) {
                                    toast(it.optString("message", "Eliminado"))
                                    showGenericList(module, filters)
                                }
                            }
                        })
                    })
                })
            }
        }
    }

internal fun MainActivity.loadGenericDetail(module: CrudModule, id: String, fallback: JSONObject) {
        apiCall({ api.get("${module.path}/$id") }) { json ->
            val detail = json.optJSONObject(module.detailKey) ?: fallback
            showGenericForm(module, id, detail)
        }
    }

internal fun MainActivity.showGenericForm(module: CrudModule, id: String?, item: JSONObject?) {
        val page = appPage(if (id == null) "Nuevo ${module.singular}" else "Editar ${module.singular}", module.title)
        val inputs = mutableMapOf<ApiField, View>()
        module.fields.forEach { field ->
            val value = item?.firstString(field.existingKeys) ?: ""
            if (field.kind == FieldKind.BOOLEAN) {
                val cb = checkbox(field.label, value == "1" || value.equals("true", ignoreCase = true))
                inputs[field] = cb
                page.addView(cb)
            } else {
                val edit = input(field.label, value)
                edit.inputType = when (field.kind) {
                    FieldKind.NUMBER -> InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
                    FieldKind.EMAIL -> InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
                    FieldKind.PASSWORD -> InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                    FieldKind.DATE -> InputType.TYPE_CLASS_DATETIME
                    else -> InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
                }
                inputs[field] = edit
                page.addView(labeledInput(field.label, edit))
            }
        }

        page.addView(topActions {
            addView(secondaryButton("Cancelar") { showGenericList(module) })
            addView(primaryButton("Guardar") {
                val body = JSONObject()
                inputs.forEach { (field, view) ->
                    val value: Any = when (view) {
                        is CheckBox -> view.isChecked
                        is EditText -> view.text.toString().trim()
                        else -> ""
                    }
                    if (value is String && value.isBlank() && !field.required) {
                        body.put(field.key, JSONObject.NULL)
                    } else {
                        body.put(field.key, value)
                    }
                }
                val call = if (id == null) {
                    { api.post(module.path, body) }
                } else {
                    { api.put("${module.path}/$id", body) }
                }
                apiCall(call) {
                    toast(it.optString("message", "Guardado"))
                    showGenericList(module)
                }
            })
        })
        if (module == articulosModule && id != null) {
            page.addView(sectionTitle("Listas de precios del articulo"))
            page.addView(primaryButton("Ver listas de precios") { showListaPrecios(id) })
        }
    }

internal fun MainActivity.showListaPrecios(articuloId: String) {
        val module = listaPreciosModule
        val page = appPage("Listas de precios", "Articulo $articuloId")
        page.addView(topActions {
            addView(secondaryButton("Atras") { showGenericList(articulosModule) })
            addView(primaryButton("Nueva lista") {
                showGenericForm(module, null, JSONObject().put("ARTICULOID", articuloId))
            })
        })
        val holder = section("Cargando...")
        page.addView(holder)
        apiCall({ api.get("/listaprecios", mapOf("articuloid" to articuloId)) }) { json ->
            holder.removeAllViews()
            val rows = json.optJSONArray("listaprecios") ?: JSONArray()
            if (rows.length() == 0) holder.addView(emptyText("Sin listas de precios."))
            for (i in 0 until rows.length()) {
                val item = rows.optJSONObject(i) ?: continue
                holder.addView(card {
                    addView(itemTitle(item, listOf("DESCRIPCION", "PROVEEDOR")))
                    addView(jsonPreview(item, listOf("COSTO", "COSTO_INSTALACION", "INSTALACION_POR_ANCHO", "INSTALACION_POR_ALTO")))
                    addView(topActions {
                        addView(secondaryButton("Editar") { showGenericForm(module, item.optAny("LISTAPRECIOID"), item) })
                        addView(dangerButton("Eliminar") {
                            confirm("Eliminar lista", "Se eliminara la lista de precios.") {
                                apiCall({ api.delete("/listaprecios/${item.optAny("LISTAPRECIOID")}") }) { showListaPrecios(articuloId) }
                            }
                        })
                    })
                })
            }
        }
    }


