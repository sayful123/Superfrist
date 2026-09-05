package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class CodeStatus {
    ACTIVE,
    USED,
    EXPIRED,
    DISABLED,
    REVOKED
}

@Entity(tableName = "activation_codes")
data class ActivationCode(
    @PrimaryKey
    val code: String,
    val status: CodeStatus = CodeStatus.ACTIVE,
    val createdDate: Long = System.currentTimeMillis(),
    val activationDate: Long? = null,
    val expiryDate: Long? = null,
    val validityDays: Int = 30,
    val deviceId: String? = null,
    val userId: String? = null,
    val lastConnection: Long? = null,
    val notes: String = ""
) {
    val isAvailable: Boolean
        get() = status == CodeStatus.ACTIVE && (expiryDate == null || expiryDate > System.currentTimeMillis())

    fun isUsableOnDevice(devId: String): Boolean {
        return (status == CodeStatus.ACTIVE || status == CodeStatus.USED) &&
                (deviceId == null || deviceId == devId) &&
                (expiryDate == null || expiryDate > System.currentTimeMillis())
    }
}
