package co.com.disspace.app.presentacion.common

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import co.com.disspace.app.data.datasource.optAny
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

abstract class BaseDisspaceActivity : AppCompatActivity() {
    private var loadingView: ProgressBar? = null
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

    fun itemTitle(item: JSONObject, keys: List<String>): TextView {
        val text = keys.mapNotNull { k -> item.optString(k).takeIf { it.isNotBlank() } }.joinToString(" - ").ifBlank { "Registro" }
        return TextView(this).apply {
            this.text = text
            textSize = 17f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            setTextColor(Color.rgb(224, 226, 229))
        }
    }

    fun jsonPreview(obj: JSONObject, fields: List<String>): TextView {
        val text = if (fields.isEmpty()) {
            obj.toString(2)
        } else {
            fields.joinToString("\n") { "$it: ${obj.optAny(it)}" }
        }
        return TextView(this).apply {
            this.text = text
            textSize = 13f
            setTextColor(Color.rgb(185, 189, 194))
            setPadding(0, dp(6), 0, dp(6))
        }
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
            content.addView(edit)
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
                        showError(result.optString("message", "La operacion no fue exitosa"))
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

    private fun rounded(color: Int, radius: Int, strokeColor: Int): GradientDrawable {
        return GradientDrawable().apply {
            setColor(color)
            cornerRadius = radius.toFloat()
            if (strokeColor != Color.TRANSPARENT) setStroke(1, strokeColor)
        }
    }
}

