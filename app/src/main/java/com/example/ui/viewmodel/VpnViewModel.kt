package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.LiveConnection
import com.example.data.model.UserSession
import com.example.data.model.VpnConnectionStatus
import com.example.data.model.VpnServer
import com.example.data.model.VpnTelemetry
import com.example.data.repository.ActivationResult
import com.example.data.repository.SessionVerifyResult
import com.example.data.repository.SuperFristRepository
import com.example.vpn.VpnManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class VpnViewModel(application: Application) : AndroidViewModel(application) {
    val repository = SuperFristRepository(application, viewModelScope)

    val currentSession: StateFlow<UserSession?> = repository.currentSession
    val selectedServer: StateFlow<VpnServer> = repository.selectedServer
    val visibleServers: StateFlow<List<VpnServer>> = repository.visibleServers

    val connectionStatus: StateFlow<VpnConnectionStatus> = VpnManager.connectionStatus
    val telemetry: StateFlow<VpnTelemetry> = VpnManager.telemetry

    private val _isActivating = MutableStateFlow(false)
    val isActivating: StateFlow<Boolean> = _isActivating.asStateFlow()

    private val _activationError = MutableStateFlow<String?>(null)
    val activationError: StateFlow<String?> = _activationError.asStateFlow()

    private val _isCheckingSession = MutableStateFlow(true)
    val isCheckingSession: StateFlow<Boolean> = _isCheckingSession.asStateFlow()

    init {
        checkStoredSession()
    }

    fun checkStoredSession() {
        viewModelScope.launch {
            _isCheckingSession.value = true
            when (val res = repository.verifyStoredSession()) {
                is SessionVerifyResult.Valid -> {
                    _activationError.value = null
                }
                is SessionVerifyResult.Expired -> {
                    _activationError.value = "Activation Code Expired"
                }
                is SessionVerifyResult.Disabled -> {
                    _activationError.value = res.message
                }
                is SessionVerifyResult.NotActivated -> {
                    // Ready for first-time activation
                }
            }
            _isCheckingSession.value = false
        }
    }

    fun activateCode(code: String) {
        viewModelScope.launch {
            _isActivating.value = true
            _activationError.value = null

            when (val result = repository.activateCode(code)) {
                is ActivationResult.Success -> {
                    _activationError.value = null
                }
                is ActivationResult.Error -> {
                    _activationError.value = result.message
                }
            }
            _isActivating.value = false
        }
    }

    fun toggleVpnConnection(context: Context) {
        val currentStatus = connectionStatus.value
        val server = selectedServer.value

        if (currentStatus == VpnConnectionStatus.CONNECTED || currentStatus == VpnConnectionStatus.CONNECTING) {
            VpnManager.startDisconnecting(context)
            currentSession.value?.let { sess ->
                repository.unregisterConnection(sess.userId)
            }
        } else {
            // Check session validity before starting connection
            val session = currentSession.value
            if (session == null || session.isExpired()) {
                _activationError.value = "Activation Code Expired"
                return
            }

            VpnManager.startConnecting(context, server)

            // Register live connection for admin monitoring
            repository.registerActiveConnection(
                LiveConnection(
                    id = UUID.randomUUID().toString(),
                    userId = session.userId,
                    deviceId = session.deviceId,
                    serverName = server.name,
                    flagEmoji = server.flagEmoji,
                    clientIp = "10.8.0.2",
                    connectedAt = System.currentTimeMillis(),
                    durationSeconds = 0L,
                    status = "Connected",
                    protocol = repository.preferences.selectedProtocol
                )
            )
        }
    }

    fun selectServer(server: VpnServer, context: Context? = null) {
        val wasConnected = connectionStatus.value == VpnConnectionStatus.CONNECTED
        repository.selectServer(server)

        if (wasConnected && context != null) {
            // Reconnect to new server
            VpnManager.startDisconnecting(context)
            VpnManager.startConnecting(context, server)
        }
    }

    fun logout(context: Context) {
        if (connectionStatus.value == VpnConnectionStatus.CONNECTED) {
            VpnManager.startDisconnecting(context)
        }
        repository.logout()
    }
}
