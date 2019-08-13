package co.lithobyte.networkrequests.models

import android.content.SharedPreferences
import java.text.SimpleDateFormat
import java.util.*

open class SessionManager {
    companion object {
        var session: Session? = null
    }
}

interface Session {
    fun isAuthenticated(): Boolean
    fun addAuthHeaders(headers: MutableMap<String, String>): MutableMap<String, String>
}

open class PrefsTokenSession(val prefs: SharedPreferences, val authKey: String = "Authentication"): Session {
    var prefsKey = "token"

    override fun isAuthenticated(): Boolean {
        val token = prefs.getString(prefsKey, "")
        if (token != null) {
            return token.isNotBlank()
        }
    }

    override fun addAuthHeaders(headers: MutableMap<String, String>): MutableMap<String, String> {
        val token = prefs.getString(prefsKey, "")
        headers[authKey] = token
        return headers
    }

}

class PrefsExpiringTokenSession(prefs: SharedPreferences, authKey: String): PrefsTokenSession(prefs, authKey) {
    val expirationKey = "expires_at"

    override fun isAuthenticated(): Boolean {
        val isTokenPresent = super.isAuthenticated()
        val expiresAtString = prefs.getString(expirationKey, "")
        val expiresAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(expiresAtString)
        return isTokenPresent && expiresAt.after(Date())
    }
}
