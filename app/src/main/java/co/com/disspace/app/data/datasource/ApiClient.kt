package co.com.disspace.app.data.datasource

import org.json.JSONObject
import java.io.BufferedReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class ApiClient(private val baseUrl: String, private val token: String) {
    fun get(path: String, query: Map<String, String> = emptyMap()): JSONObject = request("GET", path, null, query)
    fun post(path: String, body: JSONObject): JSONObject = request("POST", path, body)
    fun put(path: String, body: JSONObject): JSONObject = request("PUT", path, body)
    fun delete(path: String): JSONObject = request("DELETE", path, null)

    private fun request(method: String, path: String, body: JSONObject?, query: Map<String, String> = emptyMap()): JSONObject {
        val conn = (URL(buildUrl(path, query)).openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = 15000
            readTimeout = 30000
            setRequestProperty("Accept", "application/json")
            if (token.isNotBlank()) setRequestProperty("Authorization", "Bearer $token")
            if (body != null) {
                doOutput = true
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
            }
        }
        if (body != null) {
            OutputStreamWriter(conn.outputStream, Charsets.UTF_8).use { it.write(body.toString()) }
        }
        return parseResponse(conn)
    }

    fun multipart(path: String, method: String, fields: Map<String, String>): JSONObject {
        val boundary = "DisspaceBoundary${System.currentTimeMillis()}"
        val conn = (URL(buildUrl(path, emptyMap())).openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = 15000
            readTimeout = 30000
            doOutput = true
            setRequestProperty("Accept", "application/json")
            setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
            if (token.isNotBlank()) setRequestProperty("Authorization", "Bearer $token")
        }
        conn.outputStream.use { stream ->
            val writer = OutputStreamWriter(stream, Charsets.UTF_8)
            fields.forEach { (key, value) ->
                writer.write("--$boundary\r\n")
                writer.write("Content-Disposition: form-data; name=\"$key\"\r\n\r\n")
                writer.write(value)
                writer.write("\r\n")
            }
            writer.write("--$boundary--\r\n")
            writer.flush()
        }
        return parseResponse(conn)
    }

    private fun parseResponse(conn: HttpURLConnection): JSONObject {
        val code = conn.responseCode
        val stream = if (code in 200..299) conn.inputStream else conn.errorStream
        val text = stream?.bufferedReader()?.use(BufferedReader::readText).orEmpty()
        val json = if (text.isBlank()) JSONObject().put("success", code in 200..299) else JSONObject(text)
        json.put("httpStatus", code)
        return json
    }

    private fun buildUrl(path: String, query: Map<String, String>): String {
        val normalized = if (path.startsWith("/")) path else "/$path"
        val queryString = query.entries
            .filter { it.value.isNotBlank() }
            .joinToString("&") { "${encode(it.key)}=${encode(it.value)}" }
        return baseUrl.trimEnd('/') + normalized + if (queryString.isBlank()) "" else "?$queryString"
    }

    private fun encode(value: String): String = URLEncoder.encode(value, "UTF-8")
}
