package com.example.vpn

import android.content.Context
import android.content.Intent
import com.example.data.model.VpnConnectionStatus
import com.example.data.model.VpnServer
import com.example.data.model.VpnTelemetry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.random.Random

object VpnManager {
    private val _connectionStatus = MutableStateFlow(VpnConnectionStatus.DISCONNECTED)
    val connectionStatus: StateFlow<VpnConnectionStatus> = _connectionStatus.asStateFlow()

    private val _telemetry = MutableStateFlow(VpnTelemetry())
    val telemetry: StateFlow<VpnTelemetry> = _telemetry.asStateFlow()

    private var telemetryJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    fun startConnecting(context: Context, server: VpnServer, virtualIp: String = "10.8.0.2") {
        if (_connectionStatus.value == VpnConnectionStatus.CONNECTED ||
            _connectionStatus.value == VpnConnectionStatus.CONNECTING
        ) return

        _connectionStatus.value = VpnConnectionStatus.CONNECTING

        val intent = Intent(context, SuperFristVpnService::class.java).apply {
            action = SuperFristVpnService.ACTION_CONNECT
            putExtra(SuperFristVpnService.EXTRA_SERVER_ID, server.id)
            putExtra(SuperFristVpnService.EXTRA_SERVER_NAME, server.name)
            putExtra(SuperFristVpnService.EXTRA_SERVER_COUNTRY, server.country)
            putExtra(SuperFristVpnService.EXTRA_SERVER_FLAG, server.flagEmoji)
            putExtra(SuperFristVpnService.EXTRA_SERVER_HOST, server.host)
            putExtra(SuperFristVpnService.EXTRA_SERVER_PORT, server.port)
            putExtra(SuperFristVpnService.EXTRA_SERVER_KEY, server.publicKey)
            putExtra(SuperFristVpnService.EXTRA_PING, server.pingMs)
            putExtra(SuperFristVpnService.EXTRA_VIRTUAL_IP, virtualIp)
        }
        context.startService(intent)
    }

    fun startDisconnecting(context: Context) {
        if (_connectionStatus.value == VpnConnectionStatus.DISCONNECTED) return

        _connectionStatus.value = VpnConnectionStatus.DISCONNECTING

        val intent = Intent(context, SuperFristVpnService::class.java).apply {
            action = SuperFristVpnService.ACTION_DISCONNECT
        }
        context.startService(intent)
    }

    fun onTunnelEstablished(
        serverName: String,
        country: String,
        flag: String,
        basePing: Int,
        ipAddress: String
    ) {
        _connectionStatus.value = VpnConnectionStatus.CONNECTED
        telemetryJob?.cancel()

        var duration = 0L
        var bytesIn = 0L
        var bytesOut = 0L

        telemetryJob = scope.launch {
            while (isActive && _connectionStatus.value == VpnConnectionStatus.CONNECTED) {
                delay(1000L)
                duration += 1

                // Simulate realistic secure encrypted tunnel throughput
                val dlSpeed = 38.0f + Random.nextFloat() * 24.0f
                val ulSpeed = 10.0f + Random.nextFloat() * 8.0f
                val pingJitter = (basePing + Random.nextInt(-3, 4)).coerceAtLeast(15)

                bytesIn += (dlSpeed * 1024 * 1024 / 8).toLong()
                bytesOut += (ulSpeed * 1024 * 1024 / 8).toLong()

                _telemetry.value = VpnTelemetry(
                    currentIp = ipAddress,
                    serverName = serverName,
                    serverCountry = country,
                    flagEmoji = flag,
                    latencyMs = pingJitter,
                    downloadSpeedMbps = dlSpeed,
                    uploadSpeedMbps = ulSpeed,
                    connectionDurationSeconds = duration,
                    bytesIn = bytesIn,
                    bytesOut = bytesOut
                )
            }
        }
    }

    fun onTunnelDisconnected() {
        telemetryJob?.cancel()
        _connectionStatus.value = VpnConnectionStatus.DISCONNECTED
        _telemetry.value = _telemetry.value.copy(
            downloadSpeedMbps = 0.0f,
            uploadSpeedMbps = 0.0f,
            connectionDurationSeconds = 0L
        )
    }
}
