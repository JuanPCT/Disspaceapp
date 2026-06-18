package co.com.disspace.app.data.datasource

import android.content.Context
import org.json.JSONObject

const val ProductionBaseUrl = "https://www.disspace.com.co/api/app/v1"

class SessionStore(context: Context) {
    private val prefs = context.getSharedPreferences("disspace-session", Context.MODE_PRIVATE)

    var baseUrl: String
        get() {
            val saved = prefs.getString("baseUrl", ProductionBaseUrl).orEmpty()
            return if (saved.isBlank() || saved.isLocalDevelopmentUrl()) ProductionBaseUrl else saved
        }
        set(value) = prefs.edit().putString("baseUrl", value.trimEnd('/')).apply()

    var token: String
        get() = prefs.getString("token", "") ?: ""
        set(value) = prefs.edit().putString("token", value).apply()

    var userJson: JSONObject?
        get() = prefs.getString("user", null)?.let { JSONObject(it) }
        set(value) = prefs.edit().putString("user", value?.toString()).apply()

    fun clear() {
        val currentBase = baseUrl
        prefs.edit().clear().putString("baseUrl", currentBase).apply()
    }
}

private fun String.isLocalDevelopmentUrl(): Boolean =
    contains("10.0.2.2") || contains("localhost") || contains("127.0.0.1")
