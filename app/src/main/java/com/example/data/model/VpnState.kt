package com.example.data.model

enum class VpnConnectionStatus {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    DISCONNECTING
}

data class VpnTelemetry(
    val currentIp: String = "---.---.---.---",
    val serverName: String = "UAE / Dubai",
    val serverCountry: String = "UAE",
    val flagEmoji: String = "🇦🇪",
    val latencyMs: Int = 32,
    val downloadSpeedMbps: Float = 0.0f,
    val uploadSpeedMbps: Float = 0.0f,
    val connectionDurationSeconds: Long = 0L,
    val bytesIn: Long = 0L,
    val bytesOut: Long = 0L
) {
    fun formatDuration(): String {
        val hrs = connectionDurationSeconds / 3600
        val mins = (connectionDurationSeconds % 3600) / 60
        val secs = connectionDurationSeconds % 60
        return String.format("%02d:%02d:%02d", hrs, mins, secs)
    }

    fun formatDownloadSpeed(): String = String.format("%.1f Mbps", downloadSpeedMbps)
    fun formatUploadSpeed(): String = String.format("%.1f Mbps", uploadSpeedMbps)
}
