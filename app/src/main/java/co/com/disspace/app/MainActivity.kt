package co.com.disspace.app

import android.os.Bundle
import co.com.disspace.app.data.datasource.ApiClient
import co.com.disspace.app.data.datasource.SessionStore
import co.com.disspace.app.presentacion.showHome
import co.com.disspace.app.presentacion.showLogin
import co.com.disspace.app.presentacion.common.BaseDisspaceActivity
import org.json.JSONObject

class MainActivity : BaseDisspaceActivity() {
    internal lateinit var api: ApiClient
    internal lateinit var store: SessionStore
    internal var user: JSONObject? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        store = SessionStore(this)
        api = ApiClient(store.baseUrl, store.token)
        user = store.userJson

        if (store.token.isBlank()) {
            showLogin()
        } else {
            page("Disspace", "Validando sesion...")
            validateSession()
        }
    }

    override fun handleUnauthorized(httpStatus: Int, message: String): Boolean {
        if (store.token.isBlank() && user == null) return false

        val normalized = message.lowercase()
        val isUnauthorized = httpStatus == 401 || httpStatus == 403
        val isTokenError = normalized.contains("jwt") || normalized.contains("token")
        if (!isUnauthorized && !isTokenError) return false

        closeSession()
        toast("Sesion expirada. Inicia sesion nuevamente.")
        return true
    }

    internal fun closeSession() {
        store.clear()
        api = ApiClient(store.baseUrl, "")
        user = null
        showLogin()
    }

    private fun validateSession() {
        apiCall({ api.get("/auth/me") }) { json ->
            user = json.optJSONObject("user")
            store.userJson = user
            showHome()
        }
    }

}
