package com.example.data.model

data class UserSession(
    val code: String,
    val deviceId: String,
    val token: String,
    val activationDate: Long,
    val expiryDate: Long,
    val serverTime: Long = System.currentTimeMillis(),
    val status: CodeStatus = CodeStatus.USED,
    val userId: String = "USR-" + code.takeLast(6)
) {
    fun getRemainingMillis(currentRefTime: Long = System.currentTimeMillis()): Long {
        return (expiryDate - currentRefTime).coerceAtLeast(0L)
    }

    fun isExpired(currentRefTime: Long = System.currentTimeMillis()): Boolean {
        return expiryDate <= currentRefTime
    }

    fun formatRemainingTime(currentRefTime: Long = System.currentTimeMillis()): String {
        val diff = getRemainingMillis(currentRefTime)
        if (diff <= 0L) return "Expired"

        val seconds = diff / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24

        return if (days > 0) {
            val remHours = hours % 24
            "$days Days $remHours Hours"
        } else if (hours > 0) {
            val remMins = minutes % 60
            "$hours Hours $remMins Minutes"
        } else {
            val remSecs = seconds % 60
            "$minutes Minutes $remSecs Seconds"
        }
    }
}
