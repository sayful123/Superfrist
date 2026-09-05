package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class VpnProtocol {
    WIREGUARD,
    OPENVPN
}

enum class ServerStatus {
    ONLINE,
    OFFLINE,
    MAINTENANCE
}

@Entity(tableName = "vpn_servers")
data class VpnServer(
    @PrimaryKey
    val id: String,
    val name: String,
    val country: String,
    val countryCode: String,
    val flagEmoji: String,
    val city: String,
    val host: String,
    val port: Int = 51820,
    val protocol: VpnProtocol = VpnProtocol.WIREGUARD,
    val publicKey: String,
    val pingMs: Int = 35,
    val loadPercentage: Int = 42,
    val maxUsers: Int = 1000,
    val currentUsers: Int = 420,
    val status: ServerStatus = ServerStatus.ONLINE,
    val isVisible: Boolean = true
)
