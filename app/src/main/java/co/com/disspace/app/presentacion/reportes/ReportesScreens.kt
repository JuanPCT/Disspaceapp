package co.com.disspace.app.presentacion

import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import co.com.disspace.app.MainActivity
import co.com.disspace.app.data.datasource.optAny
import co.com.disspace.app.presentacion.common.DropdownOption
import org.json.JSONArray
import org.json.JSONObject

private val MESES = listOf(
    "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
    "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
)

internal fun MainActivity.showReportes() {
    val page = appPage("Reportes", "Consultas analiticas del backend.")
    page.addView(topActions {
        addView(primaryButton("Por articulo") { loadReporteArticulo() })
        addView(primaryButton("Por vendedor") { loadReporteVendedor() })
        addView(primaryButton("Por mes") { loadReporteMes(null) })
    })
}

internal fun MainActivity.loadReporteArticulo() {
    val page = appPage("Ventas por articulo", "Agrupado por lista de precios.")
    page.addView(topActions {
        addView(secondaryButton("Atras") { showReportes() })
    })
    val holder = section("Cargando...")
    page.addView(holder)
    apiCall({ api.get("/reportes/articulo") }) { json ->
        holder.removeAllViews()
        val grupos = json.optJSONArray("grupos") ?: JSONArray()
        if (grupos.length() == 0) {
            holder.addView(emptyText("No hay datos para mostrar."))
            return@apiCall
        }
        var granTotal = 0.0
        var granTotalCant = 0.0
        for (i in 0 until grupos.length()) {
            val grupo = grupos.optJSONObject(i) ?: continue
            val nombre = grupo.optAny("nombre").ifBlank { "Sin lista" }
            val filas = grupo.optJSONArray("filas") ?: JSONArray()
            val subtotal = grupo.optAny("subtotal").toDoubleOrNull() ?: 0.0
            val subtotalCant = grupo.optAny("subtotalCant").toDoubleOrNull() ?: 0.0
            granTotal += subtotal
            granTotalCant += subtotalCant

            holder.addView(card {
                addView(sectionTitle(nombre))
                for (j in 0 until filas.length()) {
                    val fila = filas.optJSONObject(j) ?: continue
                    addView(reportRow(
                        fila.optAny("CODIGO"),
                        fila.optAny("DESCRIPCION"),
                        "Cant: ${fila.optAny("CANTIDAD")}",
                        formatMoney(fila.optAny("VENTA"))
                    ))
                }
                addView(dataDivider())
                addView(reportRow("", "Subtotal", "Cant: $subtotalCant", formatMoney(subtotal)))
            })
        }
        holder.addView(card {
            addView(sectionTitle("Gran total"))
            addView(reportRow("", "Total general", "Cant: $granTotalCant", formatMoney(granTotal)))
        })
    }
}

internal fun MainActivity.loadReporteVendedor() {
    val page = appPage("Ventas por vendedor", "Rendimiento comercial.")
    page.addView(topActions {
        addView(secondaryButton("Atras") { showReportes() })
    })
    val holder = section("Cargando...")
    page.addView(holder)
    apiCall({ api.get("/reportes/vendedor") }) { json ->
        holder.removeAllViews()
        val rows = json.optJSONArray("rows") ?: JSONArray()
        if (rows.length() == 0) {
            holder.addView(emptyText("No hay datos para mostrar."))
            return@apiCall
        }
        var granTotal = 0.0
        for (i in 0 until rows.length()) {
            val row = rows.optJSONObject(i) ?: continue
            val venta = row.optAny("TOTAL_VENTA").toDoubleOrNull() ?: 0.0
            granTotal += venta
            holder.addView(card {
                addView(sectionTitle(row.optAny("VENDEDOR").ifBlank { "Sin vendedor" }))
                addView(reportRow("Pedidos", row.optAny("TOTAL_PEDIDOS"), "", ""))
                addView(reportRow("Venta total", "", "", formatMoney(venta)))
            })
        }
        holder.addView(card {
            addView(sectionTitle("Gran total"))
            addView(reportRow("", "Total general", "", formatMoney(granTotal)))
        })
    }
}

internal fun MainActivity.loadReporteMes(añoOverride: String?) {
    val page = appPage("Ventas por mes", "Tendencia mensual.")
    page.addView(topActions {
        addView(secondaryButton("Atras") { showReportes() })
    })
    val holder = section("Cargando...")
    page.addView(holder)

    val query = if (añoOverride != null) mapOf("año" to añoOverride) else emptyMap()
    apiCall({ api.get("/reportes/mes", query) }) { json ->
        holder.removeAllViews()
        val ventasPorMes = json.optJSONArray("ventasPorMes") ?: JSONArray()
        val añosDisponibles = json.optJSONArray("añosDisponibles") ?: JSONArray()
        val añoSeleccionado = json.optAny("añoSeleccionado")

        if (añosDisponibles.length() > 1) {
            val añoOptions = mutableListOf<DropdownOption>()
            for (i in 0 until añosDisponibles.length()) {
                val a = añosDisponibles.optString(i)
                if (a.isNotBlank()) añoOptions.add(DropdownOption(a, a))
            }
            holder.addView(labeledSpinner("Anio", añoOptions, "Seleccionar anio", añoSeleccionado) { opt ->
                loadReporteMes(opt.id)
            })
        }

        if (añoSeleccionado.isNotBlank()) {
            holder.addView(infoBanner("Anio seleccionado: $añoSeleccionado"))
        }

        var granTotal = 0.0
        for (i in 0 until ventasPorMes.length()) {
            val venta = ventasPorMes.optString(i).toDoubleOrNull() ?: 0.0
            if (venta <= 0) continue
            granTotal += venta
            val mesName = if (i < MESES.size) MESES[i] else "Mes ${i + 1}"
            holder.addView(card {
                addView(reportRow(mesName, "", "", formatMoney(venta)))
            })
        }

        if (granTotal > 0) {
            holder.addView(card {
                addView(sectionTitle("Total del anio"))
                addView(reportRow("", "Total", "", formatMoney(granTotal)))
            })
        } else {
            holder.addView(emptyText("Sin ventas en este periodo."))
        }
    }
}

private fun MainActivity.reportRow(col1: String, col2: String, col3: String, col4: String): LinearLayout {
    return LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        setPadding(0, dp(4), 0, dp(4))
        val weight1 = if (col1.isNotBlank()) 0.15f else 0f
        val weight2 = if (col2.isNotBlank()) 0.40f else 0f
        val weight3 = if (col3.isNotBlank()) 0.20f else 0f
        val weight4 = if (col4.isNotBlank()) 0.25f else 0f
        if (col1.isNotBlank()) addView(reportCell(col1, weight1, false))
        if (col2.isNotBlank()) addView(reportCell(col2, weight2, false))
        if (col3.isNotBlank()) addView(reportCell(col3, weight3, false))
        if (col4.isNotBlank()) addView(reportCell(col4, weight4, true))
    }
}

private fun MainActivity.reportCell(text: String, weight: Float, bold: Boolean): View {
    return TextView(this).apply {
        this.text = text
        textSize = 13f
        if (bold) {
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            setTextColor(Color.rgb(125, 218, 164))
            gravity = Gravity.END
        } else {
            setTextColor(Color.rgb(224, 226, 229))
        }
        layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, weight).apply {
            setMargins(dp(4), 0, dp(4), 0)
        }
    }
}
