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
internal fun MainActivity.showReportes() {
        val page = appPage("Reportes", "Consultas analiticas del backend.")
        val holder = section("Selecciona un reporte.")
        page.addView(topActions {
            addView(primaryButton("Por articulo") { loadReporte("/reportes/articulo", holder) })
            addView(primaryButton("Por vendedor") { loadReporte("/reportes/vendedor", holder) })
            addView(primaryButton("Por mes") { loadReporte("/reportes/mes", holder) })
        })
        page.addView(holder)
    }


internal fun MainActivity.loadReporte(path: String, holder: LinearLayout) {
        holder.removeAllViews()
        holder.addView(emptyText("Cargando..."))
        apiCall({ api.get(path) }) { json ->
            holder.removeAllViews()
            holder.addView(jsonPreview(json, emptyList()))
        }
}
