package co.com.disspace.app.presentacion

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnAttach
import co.com.disspace.app.MainActivity
import co.com.disspace.app.R
import co.com.disspace.app.data.datasource.ApiClient
import co.com.disspace.app.data.datasource.firstString
import co.com.disspace.app.data.datasource.optAny
import co.com.disspace.app.domain.model.ApiField
import co.com.disspace.app.domain.model.CrudModule
import co.com.disspace.app.domain.model.DisspaceModules.articulosModule
import co.com.disspace.app.domain.model.DisspaceModules.genericModules
import co.com.disspace.app.domain.model.DisspaceModules.listaPreciosModule
import co.com.disspace.app.domain.model.FieldKind
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
internal fun MainActivity.showLogin() {
    val background = Color.rgb(245, 247, 250)
    val surface = Color.WHITE
    val primary = Color.rgb(25, 135, 84)
    val textPrimary = Color.rgb(25, 32, 41)
    val textSecondary = Color.rgb(88, 100, 113)

    window.statusBarColor = background
    window.navigationBarColor = background

    val frame = FrameLayout(this).apply {
        setBackgroundColor(background)
    }
    val scroll = ScrollView(this).apply {
        isFillViewport = true
        setBackgroundColor(background)
    }
    val content = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        gravity = Gravity.CENTER
        setPadding(dp(22), dp(24), dp(22), dp(24))
        layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    }
    val cardContent = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        gravity = Gravity.CENTER_HORIZONTAL
        setPadding(dp(22), dp(24), dp(22), dp(22))
    }
    val card = MaterialCardView(this).apply {
        radius = dp(8).toFloat()
        cardElevation = dp(2).toFloat()
        setCardBackgroundColor(surface)
        strokeWidth = 1
        strokeColor = Color.rgb(226, 232, 238)
        layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
            width = minOf(resources.displayMetrics.widthPixels - dp(44), dp(420))
        }
        addView(cardContent)
    }

    cardContent.addView(ImageView(this).apply {
        setImageResource(R.drawable.disspace_logo)
        scaleType = ImageView.ScaleType.CENTER_INSIDE
        setBackground(rounded(Color.WHITE, dp(6), Color.rgb(230, 234, 238)))
        layoutParams = LinearLayout.LayoutParams(dp(164), dp(72)).apply {
            bottomMargin = dp(18)
        }
    })
    cardContent.addView(TextView(this).apply {
        text = "Bienvenido"
        textSize = 24f
        gravity = Gravity.CENTER
        setTypeface(typeface, android.graphics.Typeface.BOLD)
        setTextColor(textPrimary)
    })
    cardContent.addView(TextView(this).apply {
        text = "Ingresa a Disspace"
        textSize = 14f
        gravity = Gravity.CENTER
        setTextColor(textSecondary)
        setPadding(0, dp(4), 0, dp(20))
    })

    fun materialInput(label: String, inputType: Int, passwordToggle: Boolean = false): Pair<TextInputLayout, TextInputEditText> {
        val edit = TextInputEditText(this).apply {
            this.inputType = inputType
            textSize = 15f
            setSingleLine(true)
            setTextColor(textPrimary)
            setHintTextColor(textSecondary)
        }
        val layout = TextInputLayout(this).apply {
            hint = label
            boxBackgroundMode = TextInputLayout.BOX_BACKGROUND_OUTLINE
            boxBackgroundColor = surface
            setBoxStrokeColorStateList(ColorStateList.valueOf(primary))
            setHintTextColor(ColorStateList.valueOf(textSecondary))
            setStartIconTintList(ColorStateList.valueOf(textSecondary))
            setEndIconTintList(ColorStateList.valueOf(textSecondary))
            if (passwordToggle) endIconMode = TextInputLayout.END_ICON_PASSWORD_TOGGLE
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                bottomMargin = dp(12)
            }
            addView(edit)
        }
        cardContent.addView(layout)
        return layout to edit
    }

    val (emailLayout, email) = materialInput("Email", InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS)
    val (passwordLayout, password) = materialInput("Contrasena", InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD, passwordToggle = true)
    val keepSession = MaterialCheckBox(this).apply {
        text = "Mantener sesion iniciada en este dispositivo"
        isChecked = true
        textSize = 14f
        buttonTintList = ColorStateList.valueOf(primary)
        setTextColor(textSecondary)
        layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
            bottomMargin = dp(12)
        }
    }
    cardContent.addView(keepSession)

    cardContent.addView(MaterialButton(this).apply {
        text = "Iniciar sesion"
        isAllCaps = false
        textSize = 15f
        cornerRadius = dp(8)
        backgroundTintList = ColorStateList.valueOf(primary)
        setTextColor(Color.WHITE)
        minHeight = dp(52)
        insetTop = 0
        insetBottom = 0
        layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(52)).apply {
            topMargin = dp(4)
        }
        setOnClickListener {
            emailLayout.error = null
            passwordLayout.error = null
            val emailValue = email.text?.toString()?.trim().orEmpty()
            val passwordValue = password.text?.toString().orEmpty()
            var hasError = false
            if (emailValue.isBlank()) {
                emailLayout.error = "Ingresa tu email"
                hasError = true
            }
            if (passwordValue.isBlank()) {
                passwordLayout.error = "Ingresa tu contrasena"
                hasError = true
            }
            if (hasError) return@setOnClickListener

            val body = JSONObject()
                .put("email", emailValue)
                .put("password", passwordValue)
            api = ApiClient(store.baseUrl, "")
            apiCall({ api.post("/auth/login", body) }) { json ->
                val token = json.optString("token")
                val loggedUser = json.optJSONObject("user")
                if (keepSession.isChecked) {
                    store.token = token
                    store.userJson = loggedUser
                } else {
                    store.clear()
                }
                api = ApiClient(store.baseUrl, token)
                user = loggedUser
                showHome()
            }
        }
    })

    content.addView(card)
    scroll.addView(content)
    frame.addView(scroll)
    loadingView = ProgressBar(this).apply {
        visibility = View.GONE
        layoutParams = FrameLayout.LayoutParams(dp(56), dp(56), Gravity.CENTER)
    }
    frame.addView(loadingView)
    setContentView(frame)
    root = cardContent
}

internal fun MainActivity.showHome() {
    val currentUser = user
    val email = currentUser?.optString("email").orEmpty().ifBlank { "Usuario" }
    val page = appPage("", "", showTitle = false)

    page.addView(TextView(this).apply {
        text = email
        textSize = 18f
        setTypeface(typeface, android.graphics.Typeface.BOLD)
        setTextColor(Color.rgb(224, 226, 229))
        setPadding(0, 0, 0, dp(12))
    })
    page.addView(quickActionsPanel())
    page.addView(dashboardModuleCard(R.drawable.bi_file_earmark_text, "Cotizaciones", "Gestiona tus cotizaciones", "Ver todas") { showCotizaciones() })
    page.addView(dashboardModuleCard(R.drawable.bi_clipboard_check, "Pedidos", "Seguimiento de pedidos", "Ver pedidos") { showPedidos() })
    menuModuleByPath("/terceros")?.let { module ->
        page.addView(dashboardModuleCard(R.drawable.bi_people_fill, "Terceros", "Clientes y proveedores", "Gestionar") { showGenericList(module) })
    }
    page.addView(dashboardModuleCard(R.drawable.bi_box_seam, "Articulos", "Productos y servicios", "Ver catalogo") { showGenericList(articulosModule) })
    page.addView(dashboardModuleCard(R.drawable.bi_gear, "Configuracion", "Ajustes del sistema", "Configurar") { showConfiguracion() })
}

internal fun MainActivity.appPage(
    title: String,
    subtitle: String = "",
    showTitle: Boolean = true
): LinearLayout {
    val currentUser = user
    val email = currentUser?.optString("email").orEmpty().ifBlank { "Usuario" }
    val role = currentUser?.optString("role").orEmpty().uppercase(Locale.US)
    val sucursal = currentUser?.optString("sucursal").orEmpty()
    val year = currentUser?.optString("año").orEmpty()
    val drawerWidth = minOf((resources.displayMetrics.widthPixels * 0.82f).toInt(), dp(320))
    val pageBackground = Color.rgb(43, 46, 51)

    window.statusBarColor = Color.BLACK
    window.navigationBarColor = pageBackground

    val rootFrame = FrameLayout(this).apply {
        setBackgroundColor(pageBackground)
    }
    val main = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setBackgroundColor(pageBackground)
        layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    }
    val scrim = View(this).apply {
        visibility = View.GONE
        alpha = 0f
        setBackgroundColor(Color.argb(170, 0, 0, 0))
        layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    }
    lateinit var drawer: LinearLayout

    fun closeDrawer() {
        drawer.animate().translationX(-drawerWidth.toFloat()).setDuration(180).start()
        scrim.animate().alpha(0f).setDuration(180).withEndAction {
            scrim.visibility = View.GONE
        }.start()
    }

    fun openDrawer() {
        scrim.visibility = View.VISIBLE
        scrim.animate().alpha(1f).setDuration(180).start()
        drawer.animate().translationX(0f).setDuration(180).start()
    }

    main.addView(mobileTopbar { openDrawer() })
    main.addView(ScrollView(this).apply {
        isFillViewport = true
        layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f)
        addView(LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), dp(6), dp(12), dp(24))
            if (showTitle && title.isNotBlank()) {
                addView(TextView(context).apply {
                    text = title
                    textSize = 26f
                    setTypeface(typeface, android.graphics.Typeface.BOLD)
                    setTextColor(Color.rgb(224, 226, 229))
                })
                if (subtitle.isNotBlank()) {
                    addView(TextView(context).apply {
                        text = subtitle
                        textSize = 14f
                        setTextColor(Color.rgb(185, 189, 194))
                        setPadding(0, dp(3), 0, dp(14))
                    })
                } else {
                    addView(View(context).apply {
                        layoutParams = LinearLayout.LayoutParams(1, dp(12))
                    })
                }
            }
        })
    })

    drawer = mobileDrawer(drawerWidth, email, role, sucursal, year) {
        closeDrawer()
    }.apply {
        translationX = -drawerWidth.toFloat()
    }

    scrim.setOnClickListener { closeDrawer() }
    rootFrame.addView(main)
    rootFrame.addView(scrim)
    rootFrame.addView(drawer)
    setContentView(rootFrame)
    val scroll = main.getChildAt(1) as ScrollView
    val content = scroll.getChildAt(0) as LinearLayout
    root = content
    return content
}

private fun MainActivity.mobileTopbar(onMenu: () -> Unit): LinearLayout {
    val baseHeight = dp(58)
    val horizontalPaddingStart = dp(14)
    val horizontalPaddingEnd = dp(12)
    val baseVerticalPadding = dp(6)
    return LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        setPadding(horizontalPaddingStart, baseVerticalPadding, horizontalPaddingEnd, baseVerticalPadding)
        setBackgroundColor(Color.BLACK)
        layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, baseHeight)
        ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
            val statusTop = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            view.setPadding(horizontalPaddingStart, statusTop + baseVerticalPadding, horizontalPaddingEnd, baseVerticalPadding)
            val params = view.layoutParams
            val targetHeight = baseHeight + statusTop
            if (params.height != targetHeight) {
                params.height = targetHeight
                view.layoutParams = params
            }
            insets
        }
        doOnAttach { ViewCompat.requestApplyInsets(it) }
        addView(hamburgerButton().apply {
            setOnClickListener { onMenu() }
        })
        addView(ImageView(context).apply {
            setImageResource(R.drawable.disspace_logo)
            scaleType = ImageView.ScaleType.CENTER_INSIDE
            setBackground(rounded(Color.WHITE, dp(4), Color.TRANSPARENT))
            layoutParams = LinearLayout.LayoutParams(dp(112), dp(44)).apply {
                setMargins(dp(14), 0, 0, 0)
            }
        })
    }
}

private fun MainActivity.hamburgerButton(): LinearLayout {
    return LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        gravity = Gravity.CENTER
        isClickable = true
        isFocusable = true
        layoutParams = LinearLayout.LayoutParams(dp(42), ViewGroup.LayoutParams.MATCH_PARENT)
        repeat(3) {
            addView(View(context).apply {
                setBackgroundColor(Color.WHITE)
                layoutParams = LinearLayout.LayoutParams(dp(26), dp(2)).apply {
                    setMargins(0, dp(3), 0, dp(3))
                }
            })
        }
    }
}

private fun MainActivity.quickActionsPanel(): LinearLayout {
    return LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(0, 0, 0, dp(10))
        setBackground(rounded(Color.rgb(43, 46, 51), dp(5), Color.rgb(59, 64, 70)))
        layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
            setMargins(0, 0, 0, dp(18))
        }
        addView(TextView(context).apply {
            text = "Acciones Rapidas"
            textSize = 18f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            setTextColor(Color.rgb(224, 226, 229))
            setPadding(dp(14), dp(12), dp(14), dp(10))
        })
        addView(separator())
            addView(quickAction(R.drawable.bi_plus_circle, "Nueva Cotizacion") { showCotizacionForm(null, null) })
            addView(quickAction(R.drawable.bi_file_earmark_text, "Ver Cotizaciones") { showCotizaciones() })
            addView(quickAction(R.drawable.bi_clipboard_check, "Ver Pedidos") { showPedidos() })
            addView(quickAction(R.drawable.bi_box_seam, "Articulos") { showGenericList(articulosModule) })
    }
}

private fun MainActivity.quickAction(iconRes: Int, title: String, onClick: () -> Unit): LinearLayout {
    return LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER
        setPadding(dp(12), 0, dp(12), 0)
        setBackground(rounded(Color.rgb(25, 135, 84), dp(6), Color.TRANSPARENT))
        isClickable = true
        isFocusable = true
        setOnClickListener { onClick() }
        layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(58)).apply {
            setMargins(dp(10), dp(10), dp(10), 0)
        }
        addView(iconImage(iconRes, Color.WHITE, dp(24)).apply {
            layoutParams = LinearLayout.LayoutParams(dp(28), dp(28)).apply {
                setMargins(0, 0, dp(8), 0)
            }
        })
        addView(TextView(context).apply {
            text = title
            textSize = 17f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            setTextColor(Color.WHITE)
        })
    }
}

private fun MainActivity.dashboardModuleCard(iconRes: Int, title: String, description: String, action: String, onClick: () -> Unit): LinearLayout {
    return LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        gravity = Gravity.CENTER_HORIZONTAL
        setPadding(dp(14), dp(16), dp(14), dp(12))
        setBackground(rounded(Color.rgb(43, 46, 51), dp(5), Color.rgb(59, 64, 70)))
        layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
            setMargins(0, 0, 0, dp(18))
        }
        addView(iconImage(iconRes, Color.rgb(21, 117, 255), dp(42)).apply {
            layoutParams = LinearLayout.LayoutParams(dp(54), dp(48))
        })
        addView(TextView(context).apply {
            text = title
            textSize = 19f
            gravity = Gravity.CENTER
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            setTextColor(Color.rgb(224, 226, 229))
        })
        addView(TextView(context).apply {
            text = description
            textSize = 15f
            gravity = Gravity.CENTER
            setTextColor(Color.rgb(185, 189, 194))
            setPadding(0, dp(10), 0, dp(14))
        })
        addView(TextView(context).apply {
            text = action
            textSize = 15f
            gravity = Gravity.CENTER
            setTextColor(Color.rgb(25, 135, 84))
            setPadding(dp(14), 0, dp(14), 0)
            setBackground(rounded(Color.TRANSPARENT, dp(5), Color.rgb(25, 135, 84)))
            isClickable = true
            isFocusable = true
            setOnClickListener { onClick() }
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, dp(46))
        })
    }
}

private fun MainActivity.mobileDrawer(
    width: Int,
    email: String,
    role: String,
    sucursal: String,
    year: String,
    closeDrawer: () -> Unit
): LinearLayout {
    val isLimitedRole = role == "LOGISTICA" || role == "INSTALADOR"
    val isInstaller = role == "INSTALADOR"
    return LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setBackgroundColor(Color.BLACK)
        setPadding(0, 0, 0, 0)
        layoutParams = FrameLayout.LayoutParams(width, ViewGroup.LayoutParams.MATCH_PARENT, Gravity.START)
        ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
            val statusTop = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            if (view.paddingTop != statusTop) {
                view.setPadding(0, statusTop, 0, 0)
            }
            insets
        }
        doOnAttach { ViewCompat.requestApplyInsets(it) }

        addView(ImageView(context).apply {
            setImageResource(R.drawable.disspace_logo)
            scaleType = ImageView.ScaleType.CENTER_INSIDE
            setBackground(rounded(Color.WHITE, dp(4), Color.TRANSPARENT))
            layoutParams = LinearLayout.LayoutParams(dp(150), dp(66)).apply {
                gravity = Gravity.CENTER_HORIZONTAL
                setMargins(0, dp(8), 0, dp(18))
            }
        })

        addView(ScrollView(context).apply {
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f)
            addView(LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(18), 0, dp(18), dp(18))
                if (!isLimitedRole) {
                    addView(drawerGroup(R.drawable.bi_table, "Tablas") {
                        menuModuleByPath("/articulos")?.let { module -> addView(drawerItem(R.drawable.bi_box_seam, "Articulos", closeDrawer) { showGenericList(module) }) }
                        menuModuleByPath("/terceros")?.let { module -> addView(drawerItem(R.drawable.bi_person_badge_fill, "Terceros", closeDrawer) { showGenericList(module) }) }
                        menuModuleByPath("/users")?.let { module -> addView(drawerItem(R.drawable.bi_people_fill, "Usuarios", closeDrawer) { showGenericList(module) }) }
                        menuModuleByPath("/sucursales")?.let { module -> addView(drawerItem(R.drawable.bi_building, "Sucursales", closeDrawer) { showGenericList(module) }) }
                    })
                }

                addView(drawerGroup(R.drawable.bi_receipt, "Facturacion") {
                    if (!isInstaller) {
                        val cotTitle = if (role == "LOGISTICA") "Cotizaciones cerradas" else "Cotizaciones"
                        addView(drawerItem(R.drawable.bi_file_earmark_text, cotTitle, closeDrawer) { showCotizaciones() })
                    }
                    addView(drawerItem(R.drawable.bi_clipboard_check, "Pedidos", closeDrawer) { showPedidos() })
                })

                if (!isLimitedRole) {
                    addView(drawerGroup(R.drawable.bi_bar_chart_fill, "Reportes") {
                        addView(drawerItem(R.drawable.bi_box_seam, "Ventas por Articulo", closeDrawer) { loadReporteArticulo() })
                        addView(drawerItem(R.drawable.bi_person_badge, "Ventas por Vendedor", closeDrawer) { loadReporteVendedor() })
                        addView(drawerItem(R.drawable.bi_calendar3, "Ventas por Mes", closeDrawer) { loadReporteMes(null) })
                    })
                }
            })
        })

        addView(separator())
        addView(drawerFooter(email, sucursal, year, isLimitedRole, closeDrawer))
    }
}

private fun MainActivity.drawerGroup(iconRes: Int, title: String, children: LinearLayout.() -> Unit): LinearLayout {
    return LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        val items = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            visibility = View.GONE
            children()
        }
        val chevron = iconImage(R.drawable.bi_chevron_down, Color.rgb(214, 216, 220), dp(18)).apply {
            layoutParams = LinearLayout.LayoutParams(dp(30), ViewGroup.LayoutParams.WRAP_CONTENT)
        }
        val header = drawerSection(iconRes, title, chevron) {
            setCollapsibleVisible(items, chevron, items.visibility != View.VISIBLE)
        }
        addView(header)
        addView(items)
    }
}

private fun MainActivity.drawerSection(iconRes: Int, title: String, chevron: ImageView, onClick: () -> Unit): LinearLayout {
    return LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        setPadding(0, dp(12), 0, dp(8))
        isClickable = true
        isFocusable = true
        setOnClickListener { onClick() }
        addView(iconImage(iconRes, Color.rgb(214, 216, 220), dp(22)).apply {
            layoutParams = LinearLayout.LayoutParams(dp(42), dp(30))
        })
        addView(TextView(context).apply {
            text = title
            textSize = 17f
            setTextColor(Color.rgb(214, 216, 220))
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        })
        addView(chevron)
    }
}

private fun MainActivity.drawerItem(iconRes: Int, title: String, closeDrawer: () -> Unit, onClick: () -> Unit): LinearLayout {
    return LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        setPadding(0, dp(7), 0, dp(7))
        isClickable = true
        isFocusable = true
        setOnClickListener {
            closeDrawer()
            postDelayed({ onClick() }, 180)
        }
        addView(iconImage(iconRes, Color.rgb(214, 216, 220), dp(22)).apply {
            layoutParams = LinearLayout.LayoutParams(dp(42), dp(34))
        })
        addView(TextView(context).apply {
            text = title
            textSize = 17f
            setTextColor(Color.rgb(214, 216, 220))
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        })
    }
}

private fun MainActivity.drawerFooter(
    email: String,
    sucursal: String,
    year: String,
    isLimitedRole: Boolean,
    closeDrawer: () -> Unit
): LinearLayout {
    return LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(18), dp(10), dp(18), dp(14))
        val actions = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            visibility = View.GONE
            setPadding(0, dp(8), 0, 0)
            addView(drawerItem(R.drawable.bi_building, "Cambiar Sucursal", closeDrawer) { showChangeSucursal() })
            if (!isLimitedRole) addView(drawerItem(R.drawable.bi_gear, "Configuracion", closeDrawer) { showConfiguracion() })
            addView(drawerItem(R.drawable.bi_box_arrow_left, "Cerrar Sesion", closeDrawer) {
                closeSession()
            })
        }
        val chevron = iconImage(R.drawable.bi_chevron_down, Color.rgb(214, 216, 220), dp(18)).apply {
            layoutParams = LinearLayout.LayoutParams(dp(30), dp(30))
        }
        val session = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, 0, 0, 0)
            isClickable = true
            isFocusable = true
            setOnClickListener {
                setCollapsibleVisible(actions, chevron, actions.visibility != View.VISIBLE)
            }
            addView(LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
                addView(TextView(context).apply {
                    text = email
                    textSize = 13f
                    setTypeface(typeface, android.graphics.Typeface.BOLD)
                    setTextColor(Color.rgb(214, 216, 220))
                    setSingleLine(true)
                })
                addView(TextView(context).apply {
                    text = listOf(sucursal, year).filter { it.isNotBlank() }.joinToString(" - ").ifBlank { "Sesion activa" }
                    textSize = 12f
                    setTextColor(Color.rgb(185, 189, 194))
                    setPadding(0, dp(2), 0, 0)
                })
            })
            addView(chevron)
        }
        addView(session)
        addView(actions)
    }
}

private fun MainActivity.setCollapsibleVisible(content: View, chevron: View, expanded: Boolean) {
    content.animate().cancel()
    chevron.animate().cancel()
    chevron.animate()
        .rotation(if (expanded) 180f else 0f)
        .setDuration(160)
        .start()

    if (expanded) {
        content.visibility = View.VISIBLE
        content.alpha = 0f
        content.translationY = -dp(6).toFloat()
        content.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(180)
            .start()
    } else {
        content.animate()
            .alpha(0f)
            .translationY(-dp(6).toFloat())
            .setDuration(140)
            .withEndAction {
                content.visibility = View.GONE
                content.alpha = 1f
                content.translationY = 0f
            }
            .start()
    }
}

private fun View.postDelayed(block: () -> Unit, delayMs: Long) {
    handler?.postDelayed(block, delayMs) ?: block()
}

private fun menuModuleByPath(path: String): CrudModule? {
    return genericModules.firstOrNull { it.path == path } ?: if (articulosModule.path == path) articulosModule else null
}

private fun MainActivity.separator(): View {
    return View(this).apply {
        setBackgroundColor(Color.rgb(59, 64, 70))
        layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1)
    }
}

private fun MainActivity.iconImage(iconRes: Int, color: Int, size: Int): ImageView {
    return ImageView(this).apply {
        setImageResource(iconRes)
        setColorFilter(color)
        scaleType = ImageView.ScaleType.CENTER_INSIDE
        minimumWidth = size
        minimumHeight = size
    }
}

private fun rounded(color: Int, radius: Int, strokeColor: Int): GradientDrawable {
    return GradientDrawable().apply {
        setColor(color)
        cornerRadius = radius.toFloat()
        if (strokeColor != Color.TRANSPARENT) setStroke(1, strokeColor)
    }
}

