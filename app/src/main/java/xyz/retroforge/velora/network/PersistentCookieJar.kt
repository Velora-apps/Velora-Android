package xyz.retroforge.velora.network

import android.content.Context
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl

/**
 * Velora's API auth is a plain PHP session cookie (see Auth::bootSession()),
 * not a bearer token. This jar persists that cookie in SharedPreferences so
 * the user stays logged in across app restarts, and hands it back on every
 * request the way a browser would.
 */
class PersistentCookieJar(context: Context) : CookieJar {

    private val prefs = context.applicationContext
        .getSharedPreferences("velora_cookies", Context.MODE_PRIVATE)

    private val cache = mutableMapOf<String, MutableList<Cookie>>()

    init {
        val raw = prefs.getString(KEY, null)
        if (raw != null) {
            raw.split("\n").forEach { line ->
                if (line.isBlank()) return@forEach
                val parts = line.split("|", limit = 2)
                if (parts.size == 2) {
                    Cookie.parse(HttpUrl.Builder().scheme("https").host(parts[0]).build(), parts[1])
                        ?.let { cookie ->
                            cache.getOrPut(parts[0]) { mutableListOf() }.add(cookie)
                        }
                }
            }
        }
    }

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        if (cookies.isEmpty()) return
        val host = url.host
        val existing = cache.getOrPut(host) { mutableListOf() }
        cookies.forEach { new ->
            existing.removeAll { it.name == new.name }
            existing.add(new)
        }
        persist()
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val host = url.host
        val stored = cache[host] ?: return emptyList()
        val now = System.currentTimeMillis()
        val valid = stored.filter { it.expiresAt > now }
        if (valid.size != stored.size) {
            cache[host] = valid.toMutableList()
            persist()
        }
        return valid
    }

    fun clear() {
        cache.clear()
        prefs.edit().remove(KEY).apply()
    }

    private fun persist() {
        val lines = cache.entries.flatMap { (host, cookies) ->
            cookies.map { "$host|${it.toString()}" }
        }
        prefs.edit().putString(KEY, lines.joinToString("\n")).apply()
    }

    companion object {
        private const val KEY = "cookies"
    }
}
