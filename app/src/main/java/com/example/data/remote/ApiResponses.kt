package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BaseResponse<T>(
    @Json(name = "success") val success: Boolean,
    @Json(name = "message") val message: String? = null,
    @Json(name = "data") val data: T? = null,
    @Json(name = "error") val error: String? = null
)

@JsonClass(generateAdapter = true)
data class VerifyCodeRequest(
    @Json(name = "code") val code: String,
    @Json(name = "deviceId") val deviceId: String
)

@JsonClass(generateAdapter = true)
data class ActivateCodeRequest(
    @Json(name = "code") val code: String,
    @Json(name = "deviceId") val deviceId: String,
    @Json(name = "deviceModel") val deviceModel: String = "Android Device",
    @Json(name = "osVersion") val osVersion: String = "Android 15"
)

@JsonClass(generateAdapter = true)
data class ActivationResponseData(
    @Json(name = "token") val token: String,
    @Json(name = "userId") val userId: String,
    @Json(name = "code") val code: String,
    @Json(name = "deviceId") val deviceId: String,
    @Json(name = "activationDate") val activationDate: Long,
    @Json(name = "expiryDate") val expiryDate: Long,
    @Json(name = "serverTime") val serverTime: Long,
    @Json(name = "status") val status: String,
    @Json(name = "validityDays") val validityDays: Int,
    @Json(name = "serverList") val serverList: List<ServerDto>? = null
)

@JsonClass(generateAdapter = true)
data class UserStatusResponse(
    @Json(name = "userId") val userId: String,
    @Json(name = "code") val code: String,
    @Json(name = "status") val status: String, // ACTIVE, USED, EXPIRED, DISABLED, REVOKED
    @Json(name = "expiryDate") val expiryDate: Long,
    @Json(name = "serverTime") val serverTime: Long,
    @Json(name = "remainingSeconds") val remainingSeconds: Long,
    @Json(name = "isExpired") val isExpired: Boolean,
    @Json(name = "isDisabled") val isDisabled: Boolean
)

@JsonClass(generateAdapter = true)
data class ServerDto(
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String,
    @Json(name = "country") val country: String,
    @Json(name = "countryCode") val countryCode: String,
    @Json(name = "flagEmoji") val flagEmoji: String,
    @Json(name = "city") val city: String,
    @Json(name = "host") val host: String,
    @Json(name = "port") val port: Int,
    @Json(name = "protocol") val protocol: String,
    @Json(name = "publicKey") val publicKey: String,
    @Json(name = "pingMs") val pingMs: Int,
    @Json(name = "loadPercentage") val loadPercentage: Int,
    @Json(name = "status") val status: String,
    @Json(name = "isVisible") val isVisible: Boolean
)

@JsonClass(generateAdapter = true)
data class VpnConnectRequest(
    @Json(name = "serverId") val serverId: String,
    @Json(name = "protocol") val protocol: String = "WIREGUARD"
)

@JsonClass(generateAdapter = true)
data class VpnConfigResponse(
    @Json(name = "interfaceIp") val interfaceIp: String,
    @Json(name = "dns") val dns: List<String>,
    @Json(name = "endpoint") val endpoint: String,
    @Json(name = "serverPublicKey") val serverPublicKey: String,
    @Json(name = "allowedIps") val allowedIps: List<String>,
    @Json(name = "mtu") val mtu: Int = 1420
)
