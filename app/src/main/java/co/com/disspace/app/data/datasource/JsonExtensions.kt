package co.com.disspace.app.data.datasource

import org.json.JSONObject

fun JSONObject.optAny(key: String): String {
    if (!has(key) || isNull(key)) return ""
    return opt(key)?.toString() ?: ""
}

fun JSONObject.firstString(keys: List<String>): String {
    for (key in keys) {
        if (has(key) && !isNull(key)) return opt(key)?.toString() ?: ""
    }
    return ""
}
