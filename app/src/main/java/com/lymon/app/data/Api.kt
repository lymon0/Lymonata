package com.lymon.app.data

import com.lymon.app.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class UserItem(val name: String, val badge: String, val banned: Boolean)
data class Msg(val id: String, val from: String, val to: String, val type: String, val body: String, val ts: Long)

object Api {
    /** كل الطلبات POST بصيغة JSON إلى Google Apps Script (يتبع التحويل 302 تلقائياً). */
    suspend fun call(vararg kv: Pair<String, Any?>): JSONObject = withContext(Dispatchers.IO) {
        val body = JSONObject().apply { kv.forEach { put(it.first, it.second) } }
        val c = URL(BuildConfig.API_URL).openConnection() as HttpURLConnection
        try {
            c.requestMethod = "POST"
            c.doOutput = true
            c.connectTimeout = 20000
            c.readTimeout = 60000
            c.setRequestProperty("Content-Type", "text/plain;charset=utf-8")
            c.outputStream.use { it.write(body.toString().toByteArray()) }
            val stream = if (c.responseCode < 400) c.inputStream else c.errorStream
            JSONObject(stream.bufferedReader().readText())
        } catch (e: Exception) {
            JSONObject().put("ok", false).put("error", "تعذر الاتصال بالخادم")
        } finally {
            c.disconnect()
        }
    }
}
