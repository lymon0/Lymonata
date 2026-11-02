package com.lymon.app.data

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.ds by preferencesDataStore("lymon_session")

data class Session(val token: String, val username: String, val admin: Boolean)

class SessionStore(private val ctx: Context) {
    private val T = stringPreferencesKey("token")
    private val U = stringPreferencesKey("user")
    private val A = booleanPreferencesKey("admin")

    val flow: Flow<Session?> = ctx.ds.data.map { p ->
        p[T]?.let { Session(it, p[U] ?: "", p[A] ?: false) }
    }

    suspend fun save(s: Session) = ctx.ds.edit {
        it[T] = s.token; it[U] = s.username; it[A] = s.admin
    }

    suspend fun clear() = ctx.ds.edit { it.clear() }
}
