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
            validateSession()
        }
    }

    private fun validateSession() {
        apiCall({ api.get("/auth/me") }) { json ->
            user = json.optJSONObject("user")
            store.userJson = user
            showHome()
        }
    }

}
