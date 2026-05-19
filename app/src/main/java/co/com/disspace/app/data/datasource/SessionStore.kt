package co.com.disspace.app.data.datasource

import android.content.Context
import org.json.JSONObject

class SessionStore(context: Context) {
    private val prefs = context.getSharedPreferences("disspace-session", Context.MODE_PRIVATE)

    var baseUrl: String
        get() = prefs.getString("baseUrl", "http://10.0.2.2:5000/api/app/v1") ?: "http://10.0.2.2:5000/api/app/v1"
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
