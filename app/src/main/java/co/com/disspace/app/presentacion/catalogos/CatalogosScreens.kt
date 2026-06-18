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
internal fun MainActivity.showCatalogos() {
        val page = appPage("Catalogos", "Datos base para formularios y seleccion de IDs.")
        val holder = section("Cargando...")
        page.addView(holder)
        apiCall({ api.get("/catalogos") }) { json ->
            holder.removeAllViews()
            holder.addView(jsonPreview(json.optJSONObject("catalogos") ?: JSONObject(), emptyList()))
        }
    }

internal fun MainActivity.showChangeSucursal() {
        val page = appPage("Cambiar sucursal", "Selecciona una sucursal disponible.")
        val holder = section("Cargando...")
        page.addView(holder)
        apiCall({ api.get("/catalogos") }) { json ->
            holder.removeAllViews()
            val sucursales = json.optJSONObject("catalogos")?.optJSONArray("sucursales") ?: JSONArray()
            for (i in 0 until sucursales.length()) {
                val s = sucursales.optJSONObject(i) ?: continue
                holder.addView(moduleButton("${s.optString("NOMBRE")} ${s.optString("AÑO")}", "ID ${s.optAny("SUCURSALID")}") {
                    apiCall({ api.post("/auth/change-sucursal", JSONObject().put("sucursalId", s.optAny("SUCURSALID"))) }) { resp ->
                        store.token = resp.optString("token")
                        store.userJson = resp.optJSONObject("user")
                        api = ApiClient(store.baseUrl, store.token)
                        user = store.userJson
                        showHome()
                    }
                })
            }
        }
    }


