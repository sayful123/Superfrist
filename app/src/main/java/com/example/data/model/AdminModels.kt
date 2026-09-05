package com.example.data.model

data class AdminStats(
    val totalUsers: Int = 124,
    val activeUsers: Int = 89,
    val expiredUsers: Int = 35,
    val totalCodes: Int = 250,
    val unusedCodes: Int = 126,
    val usedCodes: Int = 124,
    val activeVpnConnections: Int = 42,
    val onlineServers: Int = 6,
    val offlineServers: Int = 0
)

data class LiveConnection(
    val id: String,
    val userId: String,
    val deviceId: String,
    val serverName: String,
    val flagEmoji: String,
    val clientIp: String,
    val connectedAt: Long,
    val durationSeconds: Long,
    val status: String = "Connected",
    val protocol: String = "WireGuard"
) {
    fun formatDuration(): String {
        val hrs = durationSeconds / 3600
        val mins = (durationSeconds % 3600) / 60
        val secs = durationSeconds % 60
        return String.format("%02d:%02d:%02d", hrs, mins, secs)
    }
}

data class AuditLog(
    val id: String,
    val timestamp: Long = System.currentTimeMillis(),
    val adminUser: String = "admin@superfrist.vpn",
    val action: String,
    val target: String,
    val details: String,
    val ip: String = "127.0.0.1"
)

data class AdminNotification(
    val id: String,
    val title: String,
    val message: String,
    val type: String, // "info", "warning", "success", "danger"
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)
