package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import java.util.UUID

class VpnPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("super_frist_vpn_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_DEVICE_ID = "device_id"
        private const val KEY_ACTIVE_CODE = "active_code"
        private const val KEY_SESSION_TOKEN = "session_token"
        private const val KEY_EXPIRY_TIMESTAMP = "expiry_timestamp"
        private const val KEY_ACTIVATION_TIMESTAMP = "activation_timestamp"
        private const val KEY_SELECTED_SERVER_ID = "selected_server_id"
        private const val KEY_KILL_SWITCH = "kill_switch_enabled"
        private const val KEY_AUTO_CONNECT = "auto_connect_enabled"
        private const val KEY_NOTIFICATIONS = "notifications_enabled"
        private const val KEY_PROTOCOL = "selected_protocol"
        private const val KEY_BACKEND_URL = "backend_url"
        private const val KEY_THEME = "app_theme"
        private const val KEY_LANGUAGE = "app_language"
        private const val KEY_USE_REMOTE_BACKEND = "use_remote_backend"
    }

    fun getDeviceId(): String {
        var devId = prefs.getString(KEY_DEVICE_ID, null)
        if (devId.isNullOrEmpty()) {
            devId = "DEV-" + UUID.randomUUID().toString().replace("-", "").take(12).uppercase()
            prefs.edit().putString(KEY_DEVICE_ID, devId).apply()
        }
        return devId
    }

    fun saveSession(code: String, token: String, activationTime: Long, expiryTime: Long) {
        prefs.edit()
            .putString(KEY_ACTIVE_CODE, code)
            .putString(KEY_SESSION_TOKEN, token)
            .putLong(KEY_ACTIVATION_TIMESTAMP, activationTime)
            .putLong(KEY_EXPIRY_TIMESTAMP, expiryTime)
            .apply()
    }

    fun clearSession() {
        prefs.edit()
            .remove(KEY_ACTIVE_CODE)
            .remove(KEY_SESSION_TOKEN)
            .remove(KEY_ACTIVATION_TIMESTAMP)
            .remove(KEY_EXPIRY_TIMESTAMP)
            .apply()
    }

    fun getActiveCode(): String? = prefs.getString(KEY_ACTIVE_CODE, null)
    fun getSessionToken(): String? = prefs.getString(KEY_SESSION_TOKEN, null)
    fun getActivationTimestamp(): Long = prefs.getLong(KEY_ACTIVATION_TIMESTAMP, 0L)
    fun getExpiryTimestamp(): Long = prefs.getLong(KEY_EXPIRY_TIMESTAMP, 0L)

    var selectedServerId: String
        get() = prefs.getString(KEY_SELECTED_SERVER_ID, "srv_uae_dubai") ?: "srv_uae_dubai"
        set(value) = prefs.edit().putString(KEY_SELECTED_SERVER_ID, value).apply()

    var isKillSwitchEnabled: Boolean
        get() = prefs.getBoolean(KEY_KILL_SWITCH, false)
        set(value) = prefs.edit().putBoolean(KEY_KILL_SWITCH, value).apply()

    var isAutoConnectEnabled: Boolean
        get() = prefs.getBoolean(KEY_AUTO_CONNECT, false)
        set(value) = prefs.edit().putBoolean(KEY_AUTO_CONNECT, value).apply()

    var isNotificationsEnabled: Boolean
        get() = prefs.getBoolean(KEY_NOTIFICATIONS, true)
        set(value) = prefs.edit().putBoolean(KEY_NOTIFICATIONS, value).apply()

    var selectedProtocol: String
        get() = prefs.getString(KEY_PROTOCOL, "WIREGUARD") ?: "WIREGUARD"
        set(value) = prefs.edit().putString(KEY_PROTOCOL, value).apply()

    var backendUrl: String
        get() = prefs.getString(KEY_BACKEND_URL, "https://api.superfrist.vpn") ?: "https://api.superfrist.vpn"
        set(value) = prefs.edit().putString(KEY_BACKEND_URL, value).apply()

    var useRemoteBackend: Boolean
        get() = prefs.getBoolean(KEY_USE_REMOTE_BACKEND, false)
        set(value) = prefs.edit().putBoolean(KEY_USE_REMOTE_BACKEND, value).apply()

    var appTheme: String
        get() = prefs.getString(KEY_THEME, "DARK") ?: "DARK"
        set(value) = prefs.edit().putString(KEY_THEME, value).apply()

    var appLanguage: String
        get() = prefs.getString(KEY_LANGUAGE, "English") ?: "English"
        set(value) = prefs.edit().putString(KEY_LANGUAGE, value).apply()
}
