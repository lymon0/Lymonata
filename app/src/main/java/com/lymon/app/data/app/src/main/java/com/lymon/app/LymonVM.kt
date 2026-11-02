package com.lymon.app

import android.app.Application
import android.media.MediaPlayer
import android.util.Base64
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lymon.app.data.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import org.json.JSONObject
import java.io.File

class LymonVM(app: Application) : AndroidViewModel(app) {
    private val store = SessionStore(app)

    var ready by mutableStateOf(false)
    var session by mutableStateOf<Session?>(null)
    var users by mutableStateOf(listOf<UserItem>())
    var messages by mutableStateOf(listOf<Msg>())
    var peer by mutableStateOf<UserItem?>(null)
    var busy by mutableStateOf(false)
    var error by mutableStateOf<String?>(null)

    private var poll: Job? = null
    private var since = 0L
    private var player: MediaPlayer? = null
    private val token get() = session?.token ?: ""

    init {
        viewModelScope.launch {
            session = store.flow.first()
            ready = true
            if (session != null) loadUsers()
        }
    }

    /** true = فشل الطلب */
    private fun failed(r: JSONObject): Boolean {
        if (r.optBoolean("ok")) return false
        val e = r.optString("error", "خطأ")
        error = e
        if (e == "unauthorized" || e == "banned") logout()
        return true
    }

    fun auth(register: Boolean, user: String, pass: String) {
        viewModelScope.launch {
            busy = true; error = null
            val r = Api.call("action" to if (register) "register" else "login",
                "username" to user.trim(), "password" to pass)
            if (!failed(r)) {
                val s = Session(r.getString("token"), r.getString("username"), r.optBoolean("admin"))
                store.save(s); session = s; loadUsers()
            }
            busy = false
        }
    }

    fun logout() {
        closeChat()
        viewModelScope.launch { store.clear(); session = null; users = emptyList() }
    }

    fun loadUsers() {
        viewModelScope.launch {
            val r = Api.call("action" to "users", "token" to token)
            if (failed(r)) return@launch
            val a = r.getJSONArray("users")
            users = (0 until a.length()).map {
                val o = a.getJSONObject(it)
                UserItem(o.getString("username"), o.optString("badge"), o.optBoolean("banned"))
            }
        }
    }

    fun openChat(u: UserItem) {
        closeChat(); peer = u; messages = emptyList(); since = 0
        poll = viewModelScope.launch {
            while (isActive) { pollOnce(u.name); delay(3000) }
        }
    }

    fun closeChat() {
        poll?.cancel(); poll = null; peer = null
        player?.release(); player = null
    }

    private suspend fun pollOnce(p: String) {
        val r = Api.call("action" to "poll", "token" to token, "peer" to p, "since" to since)
        if (failed(r)) return
        val a = r.getJSONArray("messages")
        val known = messages.map { it.id }.toSet()
        val fresh = (0 until a.length()).map {
            val o = a.getJSONObject(it)
            Msg(o.getString("id"), o.getString("from"), o.getString("to"),
                o.getString("type"), o.getString("body"), o.getLong("ts"))
        }.filter { it.id !in known }
        if (fresh.isNotEmpty()) { messages = messages + fresh; since = messages.maxOf { it.ts } }
    }

    private fun send(type: String, body: String) {
        val p = peer ?: return
        viewModelScope.launch {
            val r = Api.call("action" to "send", "token" to token, "to" to p.name, "type" to type, "body" to body)
            if (!failed(r)) pollOnce(p.name)
        }
    }

    fun sendText(t: String) { if (t.isNotBlank()) send("text", t.trim()) }

    fun sendVoice(f: File) {
        if (f.length() > 3_000_000) { error = "التسجيل كبير جداً"; return }
        send("voice", Base64.encodeToString(f.readBytes(), Base64.NO_WRAP))
        f.delete()
    }

    fun play(m: Msg) {
        viewModelScope.launch {
            val r = Api.call("action" to "media", "token" to token, "id" to m.id)
            if (failed(r)) return@launch
            val bytes = Base64.decode(r.getString("data"), Base64.NO_WRAP)
            val f = File(getApplication<Application>().cacheDir, "play_${m.id}.m4a")
            withContext(Dispatchers.IO) { f.writeBytes(bytes) }
            player?.release()
            player = MediaPlayer().apply {
                setDataSource(f.absolutePath); prepare(); start()
                setOnCompletionListener { it.release() }
            }
        }
    }

    /** op: ban | unban | verify | unverify — تُنفَّذ فقط إذا تحقق الخادم أنك المشرف */
    fun admin(op: String, target: String) {
        viewModelScope.launch {
            val r = Api.call("action" to op, "token" to token, "target" to target)
            if (!failed(r)) loadUsers()
        }
    }

    fun clearError() { error = null }

    override fun onCleared() { player?.release() }
}
