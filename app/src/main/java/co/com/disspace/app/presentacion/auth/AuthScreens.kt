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
internal fun MainActivity.showLogin() {
        val page = page("Disspace", "App operativa para cotizaciones, pedidos, catalogos y administracion.")
        val baseUrl = input("URL de API", store.baseUrl.ifBlank { "http://10.0.2.2:5000/api/app/v1" })
        val email = input("Email")
        val password = input("Contrasena")
        password.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD

        page.addView(baseUrl)
        page.addView(email)
        page.addView(password)
        page.addView(primaryButton("Iniciar sesion") {
            val body = JSONObject()
                .put("email", email.text.toString().trim())
                .put("password", password.text.toString())
            store.baseUrl = baseUrl.text.toString().trim().trimEnd('/')
            api = ApiClient(store.baseUrl, "")
            apiCall({ api.post("/auth/login", body) }) { json ->
                store.token = json.optString("token")
                store.userJson = json.optJSONObject("user")
                api = ApiClient(store.baseUrl, store.token)
                user = store.userJson
                showHome()
            }
        })
        page.addView(helpText("En emulador Android usa 10.0.2.2 para apuntar al localhost del PC. En celular fisico usa la IP LAN del servidor."))
    }

internal fun MainActivity.showHome() {
        val currentUser = user
        val subtitle = if (currentUser != null) {
            "${currentUser.optString("email")} - ${currentUser.optString("role")} - ${currentUser.optString("sucursal")}"
        } else {
            "Sesion activa"
        }

        val page = page("Disspace", subtitle)
        page.addView(sectionTitle("Operacion"))
        page.addView(moduleButton("Cotizaciones", "Crear, editar, copiar, enviar a pedido, zonas y detalles.") { showCotizaciones() })
        page.addView(moduleButton("Pedidos", "Logistica, instalacion, proveedores, comentarios y cuestionarios.") { showPedidos() })
        page.addView(moduleButton("Catalogos", "Sucursales, roles, tipos, clientes, vendedores y proveedores.") { showCatalogos() })

        page.addView(sectionTitle("Administracion"))
        genericModules.forEach { module ->
            page.addView(moduleButton(module.title, module.description) { showGenericList(module) })
        }
        page.addView(moduleButton("Configuracion", "Factores de utilidad y preguntas del cuestionario de pedidos.") { showConfiguracion() })
        page.addView(moduleButton("Reportes", "Reportes por articulo, vendedor y mes.") { showReportes() })
        page.addView(moduleButton("Cambiar sucursal", "Actualiza la sucursal del usuario y renueva el JWT.") { showChangeSucursal() })

        page.addView(secondaryButton("Cerrar sesion") {
            store.clear()
            api = ApiClient(store.baseUrl, "")
            showLogin()
        })
    }


