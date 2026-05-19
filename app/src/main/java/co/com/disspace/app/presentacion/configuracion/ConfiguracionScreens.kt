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
internal fun MainActivity.showConfiguracion() {
        val page = page("Configuracion", "Factores de utilidad y preguntas del cuestionario.")
        page.addView(topActions {
            addView(secondaryButton("Inicio") { showHome() })
            addView(primaryButton("Nueva pregunta") { showPreguntaForm(null, null) })
        })
        val holder = section("Cargando...")
        page.addView(holder)
        apiCall({ api.get("/configuracion") }) { json ->
            holder.removeAllViews()
            holder.addView(sectionTitle("Factores"))
            val factores = json.optJSONArray("factores") ?: JSONArray()
            val factorValues = mutableMapOf<Int, String>()
            for (i in 0 until factores.length()) {
                val f = factores.optJSONObject(i) ?: continue
                factorValues[f.optInt("id")] = f.optString("valor")
                holder.addView(jsonPreview(f, listOf("id", "descripcion", "valor")))
            }
            holder.addView(primaryButton("Editar factores") { showFactoresForm(factorValues) })

            holder.addView(sectionTitle("Preguntas"))
            val preguntas = json.optJSONArray("preguntas") ?: JSONArray()
            if (preguntas.length() == 0) holder.addView(emptyText("Sin preguntas configuradas."))
            for (i in 0 until preguntas.length()) {
                val p = preguntas.optJSONObject(i) ?: continue
                holder.addView(card {
                    addView(itemTitle(p, listOf("TEXTO")))
                    addView(jsonPreview(p, listOf("TIPO", "ORDEN", "ACTIVA", "opciones")))
                    addView(topActions {
                        addView(secondaryButton("Editar") { showPreguntaForm(p.optAny("PREGUNTAID"), p) })
                        addView(dangerButton("Eliminar") {
                            confirm("Eliminar pregunta", "Tambien se eliminaran sus opciones y subpreguntas.") {
                                apiCall({ api.delete("/configuracion/preguntas/${p.optAny("PREGUNTAID")}") }) { showConfiguracion() }
                            }
                        })
                    })
                })
            }
        }
    }

internal fun MainActivity.showFactoresForm(values: Map<Int, String>) {
        val page = page("Factores de utilidad", "Valores positivos usados para calcular precios.")
        val inputs = (1..6).associateWith { factor ->
            input("factor$factor", values[factor].orEmpty()).also {
                it.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
                page.addView(it)
            }
        }
        page.addView(topActions {
            addView(secondaryButton("Cancelar") { showConfiguracion() })
            addView(primaryButton("Guardar") {
                val body = JSONObject()
                inputs.forEach { (factor, edit) -> body.put("factor$factor", edit.text.toString().trim()) }
                apiCall({ api.put("/configuracion/factores", body) }) { showConfiguracion() }
            })
        })
    }

internal fun MainActivity.showPreguntaForm(id: String?, pregunta: JSONObject?) {
        val page = page(if (id == null) "Nueva pregunta" else "Editar pregunta", "Cuestionario de pedidos")
        val texto = input("Texto", pregunta?.optString("TEXTO").orEmpty())
        val tipo = input("Tipo: opcion o abierta", pregunta?.optString("TIPO", "opcion") ?: "opcion")
        val opciones = input("Opciones separadas por coma", pregunta?.optJSONArray("opciones")?.let { array ->
            val values = mutableListOf<String>()
            for (i in 0 until array.length()) values.add(array.optJSONObject(i)?.optString("TEXTO").orEmpty())
            values.filter { it.isNotBlank() }.joinToString(", ")
        }.orEmpty())
        page.addView(texto)
        page.addView(tipo)
        page.addView(opciones)
        page.addView(helpText("Para preguntas de opcion escribe al menos dos opciones separadas por coma. Las subpreguntas avanzadas se pueden ajustar en la web o enviarse como JSON si se extiende el formulario."))
        page.addView(topActions {
            addView(secondaryButton("Cancelar") { showConfiguracion() })
            addView(primaryButton("Guardar") {
                val tipoValor = tipo.text.toString().trim().ifBlank { "opcion" }
                val opts = JSONArray()
                opciones.text.toString().split(",").map { it.trim() }.filter { it.isNotBlank() }.forEach {
                    opts.put(JSONObject().put("texto", it))
                }
                val body = JSONObject()
                    .put("texto", texto.text.toString().trim())
                    .put("tipo", tipoValor)
                    .put("opciones", opts)
                val call = if (id == null) {
                    { api.post("/configuracion/preguntas", body) }
                } else {
                    { api.put("/configuracion/preguntas/$id", body) }
                }
                apiCall(call) { showConfiguracion() }
            })
        })
    }


