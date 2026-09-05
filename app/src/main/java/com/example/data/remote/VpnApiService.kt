package com.example.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface VpnApiService {
    @POST("activation/verify")
    suspend fun verifyCode(
        @Body request: VerifyCodeRequest
    ): Response<BaseResponse<Map<String, Any>>>

    @POST("activation/activate")
    suspend fun activateCode(
        @Body request: ActivateCodeRequest
    ): Response<BaseResponse<ActivationResponseData>>

    @GET("user/status")
    suspend fun getUserStatus(
        @Header("Authorization") token: String,
        @Query("deviceId") deviceId: String
    ): Response<BaseResponse<UserStatusResponse>>

    @GET("vpn/servers")
    suspend fun getServers(
        @Header("Authorization") token: String? = null
    ): Response<BaseResponse<List<ServerDto>>>

    @POST("vpn/connect")
    suspend fun connectVpn(
        @Header("Authorization") token: String,
        @Body request: VpnConnectRequest
    ): Response<BaseResponse<Map<String, String>>>

    @POST("vpn/disconnect")
    suspend fun disconnectVpn(
        @Header("Authorization") token: String
    ): Response<BaseResponse<Map<String, String>>>

    @GET("vpn/config")
    suspend fun getVpnConfig(
        @Header("Authorization") token: String,
        @Query("serverId") serverId: String
    ): Response<BaseResponse<VpnConfigResponse>>
}
