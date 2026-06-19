# AGENTS.md

Notes for OpenCode agents working in this repo. Single-module Android app (`:app`,
package `co.com.disspace.app`) — a Spanish-language business client for the Disspace
backend (cotizaciones, pedidos, catálogos, reportes, auth, configuración).

## Toolchain — do not "fix" these

- AGP **9.2.1**, Gradle **9.4.1**, Java/JVM **11**. See `gradle/libs.versions.toml`,
  `gradle/wrapper/gradle-wrapper.properties`.
- `compileSdk` uses the AGP 9 block form `release(36) { minorApiLevel = 1 }` in
  `app/build.gradle.kts`. It is intentional — don't rewrite it to `compileSdk = 36`.
- **No Kotlin Gradle plugin is applied.** AGP 9 ships built-in Kotlin support, so the
  `kotlin { compilerOptions { jvmTarget = JVM_11 } }` block works without
  `org.jetbrains.kotlin.android`. Do not add the Kotlin plugin; it can conflict.
- Dependencies are deliberately minimal: `appcompat`, `core-ktx`, `material` only.
  No Compose, no Retrofit/OkHttp, no kotlinx.serialization, no coroutines, no Room/Hilt.
  Match this — don't introduce frameworks the project avoids.
- `local.properties` (holds `sdk.dir`) is gitignored and **required** to build from CLI.
  Android Studio generates it; if it's missing, create it pointing at your Android SDK.

## Architecture — not obvious from filenames

- **UI is 100% programmatic.** There is no `res/layout/` directory. Every screen is
  built with Android Views in Kotlin via a custom DSL.
- `presentacion/common/BaseDisspaceActivity.kt` is the DSL base class and the only
  `AppCompatActivity` subclass pattern. It exposes `page()`, `appPage()`, `input()`,
  `multiLineInput()`, `primaryButton()`/`secondaryButton()`/`dangerButton()`, `card()`,
  `jsonPreview()`, `apiCall()`, `showFiltersDialog()`, `showError()`, `confirm()`,
  `toast()`, `dp()`, `spinner()`, `labeledSpinner()`, `formatMoney()`, `moneyText()`,
  `checkbox()`, `radioGroup()`, `infoBanner()`, `dataDivider()`, `dataRow()`.
  `DropdownOption` is a top-level data class in the same file for spinner options.
  All Activities extend it.
- **Single-Activity navigation.** `MainActivity` is the launcher. Routing is done via
  `MainActivity.showX()` extension functions defined in `presentacion/*.kt`
  (`showLogin`, `showHome`, `showCotizaciones`, `showPedidos`, `showGenericList`,
  `showConfiguracion`, …). Each `showX()` rebuilds the view tree. **No Intents, no
  Fragments, no Jetpack Navigation** — don't add them.
- Layers: `data/datasource` (ApiClient, SessionStore, JsonExtensions),
  `domain/model` (CrudModule, DisspaceModules), `presentacion/<module>`
  (folder names are in Spanish: `auth`, `cotizaciones`, `pedidos`, `catalogos`,
  `reportes`, `configuracion`, `generic`, `common`).

## Networking and session

- `ApiClient` uses `java.net.HttpURLConnection` + raw `Thread { }` / `runOnUiThread`.
  JSON is `org.json.JSONObject`/`JSONArray`. Every response is returned as a
  `JSONObject` with `httpStatus` injected; `apiCall` treats `optBoolean("success")`
  as the success gate. `handleUnauthorized` catches 401/403 or "jwt"/"token" messages
  and calls `closeSession()`.
- `SessionStore` (SharedPreferences `disspace-session`) holds `baseUrl`, `token`,
  `userJson`. Default base URL = `ProductionBaseUrl` in `SessionStore.kt`
  (`https://www.disspace.com.co/api/app/v1`). **Local dev URLs are rejected by
  default**: any `baseUrl` containing `10.0.2.2`, `localhost`, or `127.0.0.1` falls
  back to production. To hit a local backend from the emulator, save a non-loopback
  host or override `ProductionBaseUrl` — don't rely on the emulator loopback alias.

## Adding a CRUD table screen

- `domain/model/DisspaceModules.kt` declaratively defines backend CRUD endpoints as
  `CrudModule`s (path, list/detail keys, `idKey`, title/preview fields, filters,
  form `ApiField`s with `FieldKind`). `presentacion/generic/GenericCrudScreens.kt`
  renders any `CrudModule`. **To add a table screen, add a `CrudModule` entry and
  wire it into `genericModules` (or the drawer in `AuthScreens.kt`) — do not write a
  bespoke screen.** `ApiField.existingKeys` maps form keys to the backend's
  uppercase response keys.

## UI strings convention

- UI text and toasts are in **Spanish without accents** in code literals
  (e.g. "Contrasena", "Sesion", "Configuracion", "Acciones Rapidas", "registros mas").
  Match this no-accent style in new UI strings. Backend data keys may carry accents
  (e.g. `AÑO`) — preserve those exactly as returned.

## Commands (Windows)

Run via `./gradlew` (works in pwsh) or `gradlew.bat`. No custom scripts, no CI.

- Build debug APK: `./gradlew assembleDebug`
- Install on connected device/emulator: `./gradlew installDebug`
- Lint: `./gradlew lint`
- Unit tests: `./gradlew test`
- Single unit test class: `./gradlew test --tests "co.com.disspace.app.ExampleUnitTest"`
- Instrumented tests (need a device/emulator): `./gradlew connectedAndroidTest`

## Tests

- Only the Android Studio templates exist: `ExampleUnitTest` (JUnit 4) and
  `ExampleInstrumentedTest` (Espresso). There is no real test suite and no CI.
  Don't assume a project-specific test setup beyond JUnit4 + Espresso.
