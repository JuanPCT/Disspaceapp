package co.com.disspace.app.presentacion.common

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import co.com.disspace.app.data.datasource.optAny
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

abstract class BaseDisspaceActivity : AppCompatActivity() {
    internal var loadingView: ProgressBar? = null
    var root: LinearLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    fun page(
        title: String,
        subtitle: String = "",
        backgroundColor: Int = Color.rgb(247, 249, 251),
        titleColor: Int = Color.rgb(20, 29, 38),
        subtitleColor: Int = Color.rgb(83, 96, 110)
    ): LinearLayout {
        val pageBackground = backgroundColor
        window.statusBarColor = pageBackground
        window.navigationBarColor = pageBackground
        val frame = FrameLayout(this).apply {
            setBackgroundColor(pageBackground)
        }
        val scroll = ScrollView(this).apply {
            isFillViewport = true
            setBackgroundColor(pageBackground)
        }
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(18), dp(18), dp(28))
            setBackgroundColor(pageBackground)
        }
        scroll.addView(content)
        frame.addView(scroll)
        loadingView = ProgressBar(this).apply {
            visibility = View.GONE
            val size = dp(56)
            layoutParams = FrameLayout.LayoutParams(size, size, Gravity.CENTER)
        }
        frame.addView(loadingView)
        setContentView(frame)
        root = content

        content.addView(TextView(this).apply {
            text = title
            textSize = 28f
            setTextColor(titleColor)
            setTypeface(typeface, android.graphics.Typeface.BOLD)
        })
        if (subtitle.isNotBlank()) {
            content.addView(TextView(this).apply {
                text = subtitle
                textSize = 14f
                setTextColor(subtitleColor)
                setPadding(0, dp(4), 0, dp(16))
            })
        } else {
            content.addView(space(12))
        }
        return content
    }

    fun input(hint: String, value: String = ""): EditText {
        return EditText(this).apply {
            setHint(hint)
            setText(value)
            textSize = 15f
            setTextColor(Color.rgb(28, 43, 58))
            setHintTextColor(Color.rgb(116, 128, 141))
            background = rounded(Color.rgb(248, 249, 250), dp(6), Color.rgb(78, 86, 94))
            setSingleLine(false)
            minLines = 1
            maxLines = 5
            setPadding(dp(12), dp(10), dp(12), dp(10))
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                setMargins(0, dp(6), 0, dp(8))
            }
        }
    }

    fun primaryButton(text: String, onClick: () -> Unit): Button =
        button(text, Color.rgb(25, 135, 84), Color.WHITE, Color.rgb(25, 135, 84), onClick)

    fun secondaryButton(text: String, onClick: () -> Unit): Button =
        button(text, Color.TRANSPARENT, Color.rgb(214, 216, 220), Color.rgb(78, 86, 94), onClick)

    fun dangerButton(text: String, onClick: () -> Unit): Button =
        button(text, Color.rgb(183, 50, 57), Color.WHITE, Color.rgb(183, 50, 57), onClick)

    private fun button(text: String, bg: Int, fg: Int, border: Int, onClick: () -> Unit): Button {
        return Button(this).apply {
            this.text = text
            textSize = 13f
            setTextColor(fg)
            background = rounded(bg, dp(6), border)
            minWidth = 0
            minHeight = 0
            minimumWidth = 0
            minimumHeight = 0
            setPadding(dp(12), dp(8), dp(12), dp(8))
            isAllCaps = false
            setOnClickListener { onClick() }
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                setMargins(0, dp(4), 0, dp(4))
            }
        }
    }

    fun moduleButton(title: String, description: String, onClick: () -> Unit): View {
        return card {
            addView(TextView(context).apply {
                text = title
                textSize = 18f
                setTypeface(typeface, android.graphics.Typeface.BOLD)
                setTextColor(Color.rgb(224, 226, 229))
            })
            addView(TextView(context).apply {
                text = description
                textSize = 13f
                setTextColor(Color.rgb(185, 189, 194))
                setPadding(0, dp(4), 0, 0)
            })
            setOnClickListener { onClick() }
        }
    }

    fun card(block: LinearLayout.() -> Unit): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(14), dp(12), dp(14), dp(12))
            background = rounded(Color.rgb(43, 46, 51), dp(6), Color.rgb(59, 64, 70))
            elevation = dp(1).toFloat()
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                setMargins(0, dp(8), 0, dp(8))
            }
            block()
        }
    }

    fun section(title: String): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(emptyText(title))
        }
    }

    fun sectionTitle(text: String): TextView {
        return TextView(this).apply {
            this.text = text
            textSize = 17f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            setTextColor(Color.rgb(224, 226, 229))
            setPadding(0, dp(16), 0, dp(6))
        }
    }

    fun helpText(text: String): TextView {
        return TextView(this).apply {
            this.text = text
            textSize = 13f
            setTextColor(Color.rgb(185, 189, 194))
            setPadding(0, dp(8), 0, dp(8))
        }
    }

    fun emptyText(text: String): TextView {
        return TextView(this).apply {
            this.text = text
            textSize = 14f
            setTextColor(Color.rgb(185, 189, 194))
            setPadding(0, dp(8), 0, dp(8))
        }
    }

    fun fieldLabel(text: String): TextView {
        return TextView(this).apply {
            this.text = text
            textSize = 13f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            setTextColor(Color.rgb(224, 226, 229))
            setPadding(0, dp(8), 0, 0)
        }
    }

    fun labeledInput(label: String, editText: EditText): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(fieldLabel(label))
            addView(editText)
        }
    }

    fun readOnlyField(label: String, value: String): View {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), dp(10), dp(12), dp(10))
            background = rounded(Color.rgb(35, 38, 43), dp(8), Color.rgb(74, 79, 86))
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                setMargins(0, dp(6), 0, dp(8))
            }
            addView(TextView(context).apply {
                text = label
                textSize = 12f
                setTypeface(typeface, android.graphics.Typeface.BOLD)
                setTextColor(Color.rgb(185, 189, 194))
            })
            addView(TextView(context).apply {
                text = value.ifBlank { "Sin dato" }
                textSize = 15f
                setTextColor(Color.rgb(244, 245, 246))
                setPadding(0, dp(4), 0, 0)
            })
        }
    }

    fun itemTitle(item: JSONObject, keys: List<String>): TextView {
        val text = keys.mapNotNull { key ->
            if (!item.has(key) || item.isNull(key)) return@mapNotNull null
            formatDataValue(key, item.opt(key)).takeIf { it.isNotBlank() && it != "Sin dato" }
        }.joinToString(" - ").ifBlank { "Registro" }
        return TextView(this).apply {
            this.text = text
            textSize = 17f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            setTextColor(Color.rgb(224, 226, 229))
        }
    }

    fun jsonPreview(obj: JSONObject, fields: List<String>, hideDatabaseIds: Boolean = false): View {
        val keys = if (fields.isEmpty()) {
            jsonKeys(obj, hideDatabaseIds)
        } else {
            fields.filterNot { hideDatabaseIds && isDatabaseIdKey(it) }
        }
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), dp(10), dp(12), dp(10))
            background = rounded(Color.rgb(35, 38, 43), dp(8), Color.rgb(74, 79, 86))
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                setMargins(0, dp(8), 0, dp(8))
            }

            val visibleKeys = keys.filter { key -> obj.has(key) && !obj.isNull(key) }
            if (visibleKeys.isEmpty()) {
                addView(emptyDataValue("Sin datos para mostrar."))
            } else {
                visibleKeys.forEachIndexed { index, key ->
                    if (index > 0) addView(dataDivider())
                    addDataField(key, obj.opt(key), depth = 0, hideDatabaseIds = hideDatabaseIds)
                }
            }
        }
    }

    private fun LinearLayout.addDataField(key: String, value: Any?, depth: Int, hideDatabaseIds: Boolean) {
        when (value) {
            is JSONObject -> {
                addView(dataLabel(humanizeKey(key)))
                addView(dataObject(value, depth + 1, hideDatabaseIds))
            }
            is JSONArray -> {
                addView(dataRow(humanizeKey(key), describeArray(value)))
                addArrayPreview(value, depth, hideDatabaseIds)
            }
            else -> addView(dataRow(humanizeKey(key), formatDataValue(key, value)))
        }
    }

    private fun dataObject(obj: JSONObject, depth: Int, hideDatabaseIds: Boolean): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(10), dp(8), dp(10), dp(8))
            background = rounded(Color.rgb(43, 46, 51), dp(8), Color.rgb(59, 64, 70))
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                setMargins(0, dp(6), 0, 0)
            }

            val keys = jsonKeys(obj, hideDatabaseIds)
            if (keys.isEmpty()) {
                addView(emptyDataValue("Sin datos."))
            } else {
                keys.forEachIndexed { index, key ->
                    if (index > 0) addView(dataDivider(compact = true))
                    addDataField(key, obj.opt(key), depth, hideDatabaseIds)
                }
            }
        }
    }

    private fun LinearLayout.addArrayPreview(array: JSONArray, depth: Int, hideDatabaseIds: Boolean) {
        if (array.length() == 0 || depth >= 1) return

        val objectItems = mutableListOf<JSONObject>()
        val primitiveValues = mutableListOf<String>()
        for (i in 0 until array.length()) {
            when (val item = array.opt(i)) {
                is JSONObject -> objectItems.add(item)
                is JSONArray -> primitiveValues.add(describeArray(item))
                null, JSONObject.NULL -> Unit
                else -> primitiveValues.add(formatDataValue("", item))
            }
        }

        if (primitiveValues.isNotEmpty()) {
            addView(TextView(context).apply {
                text = primitiveValues.take(6).joinToString(", ")
                textSize = 13f
                setTextColor(Color.rgb(224, 226, 229))
                setPadding(0, dp(4), 0, 0)
            })
        }

        objectItems.take(3).forEachIndexed { index, item ->
            addView(dataObject(compactObject(item, maxFields = 6, hideDatabaseIds = hideDatabaseIds), depth + 1, hideDatabaseIds).apply {
                addView(TextView(context).apply {
                    text = "Registro ${index + 1}"
                    textSize = 12f
                    setTypeface(typeface, android.graphics.Typeface.BOLD)
                    setTextColor(Color.rgb(125, 218, 164))
                }, 0)
            })
        }

        val hiddenCount = objectItems.size - 3
        if (hiddenCount > 0) {
            addView(TextView(context).apply {
                text = "+ $hiddenCount registros mas"
                textSize = 12f
                setTextColor(Color.rgb(185, 189, 194))
                setPadding(0, dp(6), 0, 0)
            })
        }
    }

    private fun dataRow(label: String, value: String): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(6), 0, dp(6))
            addView(dataLabel(label).apply {
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 0.42f).apply {
                    setMargins(0, 0, dp(10), 0)
                }
            })
            addView(TextView(context).apply {
                text = value
                textSize = 14f
                setTextColor(Color.rgb(244, 245, 246))
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 0.58f)
            })
        }
    }

    private fun dataLabel(text: String): TextView {
        return TextView(this).apply {
            this.text = text
            textSize = 12f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            setTextColor(Color.rgb(185, 189, 194))
        }
    }

    private fun emptyDataValue(text: String): TextView {
        return TextView(this).apply {
            this.text = text
            textSize = 14f
            setTextColor(Color.rgb(185, 189, 194))
            setPadding(0, dp(4), 0, dp(4))
        }
    }

    fun dataDivider(compact: Boolean = false): View {
        return View(this).apply {
            setBackgroundColor(Color.rgb(59, 64, 70))
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1).apply {
                setMargins(0, if (compact) dp(2) else dp(4), 0, if (compact) dp(2) else dp(4))
            }
        }
    }

    private fun jsonKeys(obj: JSONObject, hideDatabaseIds: Boolean = false): List<String> {
        val keys = mutableListOf<String>()
        val iterator = obj.keys()
        while (iterator.hasNext()) keys.add(iterator.next())
        return keys.filterNot {
            it.equals("success", ignoreCase = true) ||
                it.equals("httpStatus", ignoreCase = true) ||
                (hideDatabaseIds && isDatabaseIdKey(it))
        }
    }

    private fun compactObject(obj: JSONObject, maxFields: Int, hideDatabaseIds: Boolean): JSONObject {
        val compact = JSONObject()
        jsonKeys(obj, hideDatabaseIds).take(maxFields).forEach { key -> compact.put(key, obj.opt(key)) }
        return compact
    }

    private fun isDatabaseIdKey(key: String): Boolean {
        val normalized = key.trim().uppercase(Locale.US)
        return normalized == "ID" || normalized.endsWith("ID")
    }

    private fun humanizeKey(key: String): String {
        val spaced = key
            .replace(Regex("([a-z])([A-Z])"), "$1 $2")
            .replace("_", " ")
            .lowercase(Locale.US)
            .trim()
        return spaced.split(" ")
            .filter { it.isNotBlank() }
            .joinToString(" ") { word -> word.replaceFirstChar { it.uppercase(Locale.US) } }
    }

    private fun describeArray(array: JSONArray): String {
        return when (array.length()) {
            0 -> "Sin datos"
            1 -> "1 elemento"
            else -> "${array.length()} elementos"
        }
    }

    private fun formatDataValue(key: String, value: Any?): String {
        if (value == null || value == JSONObject.NULL) return "Sin dato"
        val text = value.toString().trim()
        if (text.isBlank()) return "Sin dato"
        return if (isDateKey(key)) formatDateForDisplay(text) else text
    }

    private fun isDateKey(key: String): Boolean {
        val normalized = key.trim().uppercase(Locale.US)
        return normalized.contains("FECHA") || normalized.contains("DATE")
    }

    private fun formatDateForDisplay(text: String): String {
        val normalized = text.trim()
        if (Regex("""^\d{2}/\d{2}/\d{4}$""").matches(normalized)) return normalized

        val isoDate = Regex("""^(\d{4})-(\d{2})-(\d{2})""").find(normalized)
        if (isoDate != null) {
            val (year, month, day) = isoDate.destructured
            return "$day/$month/$year"
        }

        return normalized
    }

    fun topActions(block: LinearLayout.() -> Unit): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(0, dp(4), 0, dp(8))
            block()
            for (i in 0 until childCount) {
                val child = getChildAt(i)
                child.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                    setMargins(0, dp(4), 0, dp(4))
                }
            }
        }
    }

    fun filterButton(activeCount: Int, onClick: () -> Unit): Button {
        val label = if (activeCount > 0) "Filtros ($activeCount)" else "Filtros"
        return secondaryButton(label, onClick)
    }

    fun showFiltersDialog(
        title: String,
        fields: List<Pair<String, String>>,
        filters: Map<String, String>,
        onApply: (Map<String, String>) -> Unit
    ) {
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(16), dp(18), dp(6))
            background = rounded(Color.rgb(33, 37, 41), dp(8), Color.rgb(63, 68, 74))
        }
        content.addView(TextView(this).apply {
            text = title
            textSize = 20f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            setTextColor(Color.rgb(244, 245, 246))
            setPadding(0, 0, 0, dp(8))
        })
        val inputs = mutableMapOf<String, EditText>()
        fields.forEach { (key, label) ->
            val edit = input(label, filters[key].orEmpty())
            inputs[key] = edit
            content.addView(labeledInput(label, edit))
        }
        val scroll = ScrollView(this).apply {
            setBackgroundColor(Color.rgb(33, 37, 41))
            addView(content)
        }
        val dialog = AlertDialog.Builder(this)
            .setView(scroll)
            .setNegativeButton("Cancelar", null)
            .setNeutralButton("Limpiar") { _, _ -> onApply(emptyMap()) }
            .setPositiveButton("Aplicar") { _, _ ->
                onApply(inputs.mapValues { it.value.text.toString().trim() }.filterValues { it.isNotBlank() })
            }
            .show()
        dialog.window?.setBackgroundDrawable(rounded(Color.rgb(33, 37, 41), dp(8), Color.TRANSPARENT))
        dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(Color.rgb(25, 135, 84))
        dialog.getButton(AlertDialog.BUTTON_NEUTRAL)?.setTextColor(Color.rgb(214, 216, 220))
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(Color.rgb(214, 216, 220))
    }

    fun apiCall(call: () -> JSONObject, success: (JSONObject) -> Unit) {
        loadingView?.visibility = View.VISIBLE
        Thread {
            try {
                val result = call()
                runOnUiThread {
                    loadingView?.visibility = View.GONE
                    if (!result.optBoolean("success", false)) {
                        val message = result.optString("message", "La operacion no fue exitosa")
                        if (!handleUnauthorized(result.optInt("httpStatus", 0), message)) {
                            showError(message)
                        }
                    } else {
                        success(result)
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    loadingView?.visibility = View.GONE
                    showError(e.message ?: "Error inesperado")
                }
            }
        }.start()
    }

    protected open fun handleUnauthorized(httpStatus: Int, message: String): Boolean = false

    fun showError(message: String) {
        AlertDialog.Builder(this)
            .setTitle("Disspace")
            .setMessage(message)
            .setPositiveButton("Aceptar", null)
            .show()
    }

    fun showTextDialog(title: String, text: String) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(text)
            .setPositiveButton("Aceptar", null)
            .show()
    }

    fun confirm(title: String, message: String, onYes: () -> Unit) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Continuar") { _, _ -> onYes() }
            .show()
    }

    fun toast(text: String) = Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
    fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
    fun today(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    private fun space(height: Int): View = View(this).apply {
        layoutParams = LinearLayout.LayoutParams(1, dp(height))
    }

    fun rounded(color: Int, radius: Int, strokeColor: Int): GradientDrawable {
        return GradientDrawable().apply {
            setColor(color)
            cornerRadius = radius.toFloat()
            if (strokeColor != Color.TRANSPARENT) setStroke(1, strokeColor)
        }
    }

    fun spinner(
        options: List<DropdownOption>,
        placeholder: String = "Seleccionar...",
        selectedId: String = "",
        onSelected: (DropdownOption) -> Unit
    ): Spinner {
        val all = mutableListOf<DropdownOption>()
        all.add(DropdownOption("", placeholder, null))
        all.addAll(options)
        val adapter = object : ArrayAdapter<DropdownOption>(
            this, android.R.layout.simple_spinner_item, all
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

        return Spinner(this).apply {
            this.adapter = adapter
            setBackgroundColor(Color.rgb(35, 38, 43))
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(54)).apply {
                setMargins(0, dp(6), 0, dp(8))
            }
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    val opt = all[position]
                    if (opt.id.isNotBlank()) onSelected(opt)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) = Unit
            }
            if (selectedId.isNotBlank()) {
                val idx = all.indexOfFirst { it.id == selectedId }
                if (idx > 0) setSelection(idx)
            }
        }
    }

    fun labeledSpinner(
        label: String,
        options: List<DropdownOption>,
        placeholder: String = "Seleccionar...",
        selectedId: String = "",
        onSelected: (DropdownOption) -> Unit
    ): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(fieldLabel(label))
            addView(spinner(options, placeholder, selectedId, onSelected))
        }
    }

    fun formatMoney(value: Any?): String {
        val num = value?.toString()?.replace(",", ".")?.toDoubleOrNull() ?: 0.0
        return java.text.NumberFormat.getInstance(Locale("es", "CO")).apply {
            minimumFractionDigits = 0
            maximumFractionDigits = 2
        }.format(num)
    }

    fun moneyText(value: Any?): TextView {
        return TextView(this).apply {
            text = formatMoney(value)
            textSize = 16f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            setTextColor(Color.rgb(125, 218, 164))
            setPadding(0, dp(4), 0, dp(4))
        }
    }

    fun checkbox(label: String, checked: Boolean = false): CheckBox {
        return CheckBox(this).apply {
            text = label
            this.isChecked = checked
            textSize = 14f
            setTextColor(Color.rgb(224, 226, 229))
            setPadding(dp(8), dp(6), dp(8), dp(6))
        }
    }

    fun radioGroup(options: List<Pair<String, String>>, selectedId: String = ""): RadioGroup {
        return RadioGroup(this).apply {
            orientation = RadioGroup.VERTICAL
            setPadding(0, dp(4), 0, dp(4))
            options.forEach { (id, label) ->
                addView(RadioButton(this@BaseDisspaceActivity).apply {
                    this.id = View.generateViewId()
                    text = label
                    textSize = 14f
                    setTextColor(Color.rgb(224, 226, 229))
                    setPadding(dp(8), dp(6), dp(8), dp(6))
                    isChecked = id == selectedId
                    tag = id
                })
            }
        }
    }

    fun multiLineInput(hint: String, value: String = "", minLines: Int = 3): EditText {
        return input(hint, value).apply {
            setSingleLine(false)
            this.minLines = minLines
            setPadding(dp(12), dp(10), dp(12), dp(10))
        }
    }

    fun infoBanner(text: String): View {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(dp(12), dp(10), dp(12), dp(10))
            background = rounded(Color.rgb(33, 60, 75), dp(6), Color.rgb(45, 90, 110))
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                setMargins(0, dp(6), 0, dp(8))
            }
            addView(TextView(context).apply {
                this.text = text
                textSize = 13f
                setTextColor(Color.rgb(170, 200, 215))
            })
        }
    }
}

data class DropdownOption(
    val id: String,
    val label: String,
    val data: JSONObject? = null
) {
    override fun toString(): String = label
}

