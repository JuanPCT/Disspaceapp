package co.com.disspace.app.presentacion.common

import android.app.AlertDialog
import android.graphics.Color
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

    fun page(title: String, subtitle: String = ""): LinearLayout {
        val frame = FrameLayout(this)
        val scroll = ScrollView(this)
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(18), dp(18), dp(28))
            setBackgroundColor(Color.rgb(247, 249, 251))
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
            setTextColor(Color.rgb(20, 29, 38))
            setTypeface(typeface, android.graphics.Typeface.BOLD)
        })
        if (subtitle.isNotBlank()) {
            content.addView(TextView(this).apply {
                text = subtitle
                textSize = 14f
                setTextColor(Color.rgb(83, 96, 110))
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
            setSingleLine(false)
            minLines = 1
            maxLines = 5
            setPadding(dp(12), dp(8), dp(12), dp(8))
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                setMargins(0, dp(6), 0, dp(6))
            }
        }
    }

    fun primaryButton(text: String, onClick: () -> Unit): Button = button(text, Color.rgb(27, 111, 181), Color.WHITE, onClick)
    fun secondaryButton(text: String, onClick: () -> Unit): Button = button(text, Color.rgb(232, 238, 245), Color.rgb(28, 43, 58), onClick)
    fun dangerButton(text: String, onClick: () -> Unit): Button = button(text, Color.rgb(183, 50, 57), Color.WHITE, onClick)

    private fun button(text: String, bg: Int, fg: Int, onClick: () -> Unit): Button {
        return Button(this).apply {
            this.text = text
            textSize = 12f
            setTextColor(fg)
            setBackgroundColor(bg)
            isAllCaps = false
            setOnClickListener { onClick() }
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                setMargins(0, dp(4), dp(8), dp(4))
            }
        }
    }

    fun moduleButton(title: String, description: String, onClick: () -> Unit): View {
        return card {
            addView(TextView(context).apply {
                text = title
                textSize = 18f
                setTypeface(typeface, android.graphics.Typeface.BOLD)
                setTextColor(Color.rgb(25, 36, 48))
            })
            addView(TextView(context).apply {
                text = description
                textSize = 13f
                setTextColor(Color.rgb(82, 96, 110))
                setPadding(0, dp(4), 0, 0)
            })
            setOnClickListener { onClick() }
        }
    }

    fun card(block: LinearLayout.() -> Unit): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(14), dp(12), dp(14), dp(12))
            setBackgroundColor(Color.WHITE)
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
            setTextColor(Color.rgb(28, 43, 58))
            setPadding(0, dp(16), 0, dp(6))
        }
    }

    fun helpText(text: String): TextView {
        return TextView(this).apply {
            this.text = text
            textSize = 13f
            setTextColor(Color.rgb(84, 94, 106))
            setPadding(0, dp(8), 0, dp(8))
        }
    }

    fun emptyText(text: String): TextView {
        return TextView(this).apply {
            this.text = text
            textSize = 14f
            setTextColor(Color.rgb(86, 98, 112))
            setPadding(0, dp(8), 0, dp(8))
        }
    }

    fun itemTitle(item: JSONObject, keys: List<String>): TextView {
        val text = keys.mapNotNull { k -> item.optString(k).takeIf { it.isNotBlank() } }.joinToString(" - ").ifBlank { "Registro" }
        return TextView(this).apply {
            this.text = text
            textSize = 17f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            setTextColor(Color.rgb(28, 43, 58))
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
            setTextColor(Color.rgb(54, 65, 77))
            setPadding(0, dp(6), 0, dp(6))
        }
    }

    fun topActions(block: LinearLayout.() -> Unit): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.START
            block()
        }
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
}

