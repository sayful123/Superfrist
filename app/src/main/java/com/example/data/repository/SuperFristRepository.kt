package com.example.data.repository

import android.content.Context
import com.example.data.local.SuperFristDatabase
import com.example.data.local.VpnDao
import com.example.data.local.VpnPreferences
import com.example.data.model.ActivationCode
import com.example.data.model.AdminNotification
import com.example.data.model.AdminStats
import com.example.data.model.AuditLog
import com.example.data.model.CodeStatus
import com.example.data.model.LiveConnection
import com.example.data.model.ServerStatus
import com.example.data.model.UserSession
import com.example.data.model.VpnProtocol
import com.example.data.model.VpnServer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.security.SecureRandom
import java.util.UUID

sealed class ActivationResult {
    data class Success(val session: UserSession) : ActivationResult()
    data class Error(val message: String) : ActivationResult()
}

sealed class SessionVerifyResult {
    data class Valid(val session: UserSession) : SessionVerifyResult()
    object Expired : SessionVerifyResult()
    data class Disabled(val message: String) : SessionVerifyResult()
    object NotActivated : SessionVerifyResult()
}

class SuperFristRepository(
    private val context: Context,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    private val database = SuperFristDatabase.getInstance(context)
    private val dao: VpnDao = database.vpnDao()
    val preferences = VpnPreferences(context)

    private val _currentSession = MutableStateFlow<UserSession?>(null)
    val currentSession: StateFlow<UserSession?> = _currentSession.asStateFlow()

    private val _selectedServer = MutableStateFlow<VpnServer>(getDefaultServer())
    val selectedServer: StateFlow<VpnServer> = _selectedServer.asStateFlow()

    private val _liveConnections = MutableStateFlow<List<LiveConnection>>(emptyList())
    val liveConnections: StateFlow<List<LiveConnection>> = _liveConnections.asStateFlow()

    private val _auditLogs = MutableStateFlow<List<AuditLog>>(emptyList())
    val auditLogs: StateFlow<List<AuditLog>> = _auditLogs.asStateFlow()

    private val _notifications = MutableStateFlow<List<AdminNotification>>(emptyList())
    val notifications: StateFlow<List<AdminNotification>> = _notifications.asStateFlow()

    val allCodes: StateFlow<List<ActivationCode>> = dao.getAllCodes()
        .stateIn(scope, SharingStarted.Lazily, emptyList())

    val visibleServers: StateFlow<List<VpnServer>> = dao.getVisibleServers()
        .stateIn(scope, SharingStarted.Lazily, emptyList())

    init {
        scope.launch {
            seedInitialDataIfNeeded()
            loadSavedSelectedServer()
        }
    }

    private fun getDefaultServer(): VpnServer {
        return VpnServer(
            id = "srv_uae_dubai",
            name = "UAE - Dubai",
            country = "UAE",
            countryCode = "AE",
            flagEmoji = "🇦🇪",
            city = "Dubai",
            host = "ae-dxb.vpn.superfrist.net",
            port = 51820,
            protocol = VpnProtocol.WIREGUARD,
            publicKey = "0a5k9N2Z8wXz1mK+SuperFristUAEDubaiNode=",
            pingMs = 32,
            loadPercentage = 45,
            status = ServerStatus.ONLINE,
            isVisible = true
        )
    }

    private suspend fun seedInitialDataIfNeeded() = withContext(Dispatchers.IO) {
        val defaultServers = listOf(
            getDefaultServer(),
            VpnServer(
                id = "srv_singapore",
                name = "Singapore",
                country = "Singapore",
                countryCode = "SG",
                flagEmoji = "🇸🇬",
                city = "Singapore Central",
                host = "sg-sin.vpn.superfrist.net",
                port = 51820,
                protocol = VpnProtocol.WIREGUARD,
                publicKey = "9uY71B+SuperFristSingaporeGatewayPubKey=",
                pingMs = 48,
                loadPercentage = 58,
                status = ServerStatus.ONLINE,
                isVisible = true
            ),
            VpnServer(
                id = "srv_germany",
                name = "Germany - Frankfurt",
                country = "Germany",
                countryCode = "DE",
                flagEmoji = "🇩🇪",
                city = "Frankfurt",
                host = "de-fra.vpn.superfrist.net",
                port = 51820,
                protocol = VpnProtocol.WIREGUARD,
                publicKey = "7jK4xL+SuperFristGermanyNodePublicKy=",
                pingMs = 65,
                loadPercentage = 38,
                status = ServerStatus.ONLINE,
                isVisible = true
            ),
            VpnServer(
                id = "srv_netherlands",
                name = "Netherlands - Amsterdam",
                country = "Netherlands",
                countryCode = "NL",
                flagEmoji = "🇳🇱",
                city = "Amsterdam",
                host = "nl-ams.vpn.superfrist.net",
                port = 51820,
                protocol = VpnProtocol.WIREGUARD,
                publicKey = "5mN8qW+SuperFristNetherlandsHighSpeed=",
                pingMs = 68,
                loadPercentage = 42,
                status = ServerStatus.ONLINE,
                isVisible = true
            ),
            VpnServer(
                id = "srv_usa",
                name = "United States - New York",
                country = "United States",
                countryCode = "US",
                flagEmoji = "🇺🇸",
                city = "New York",
                host = "us-nyc.vpn.superfrist.net",
                port = 51820,
                protocol = VpnProtocol.WIREGUARD,
                publicKey = "3pL2oK+SuperFristUSANewYorkSecureKey=",
                pingMs = 110,
                loadPercentage = 64,
                status = ServerStatus.ONLINE,
                isVisible = true
            ),
            VpnServer(
                id = "srv_uk",
                name = "United Kingdom - London",
                country = "United Kingdom",
                countryCode = "GB",
                flagEmoji = "🇬🇧",
                city = "London",
                host = "uk-lon.vpn.superfrist.net",
                port = 51820,
                protocol = VpnProtocol.WIREGUARD,
                publicKey = "1wE9rT+SuperFristLondonDirectAccessKey=",
                pingMs = 72,
                loadPercentage = 51,
                status = ServerStatus.ONLINE,
                isVisible = true
            )
        )
        dao.insertServers(defaultServers)

        // Seed initial activation codes
        val now = System.currentTimeMillis()
        val seedCodes = listOf(
            ActivationCode(
                code = "VPN-8F7K-29MX-QP4A",
                status = CodeStatus.ACTIVE,
                validityDays = 30,
                createdDate = now - 86400000L,
                notes = "Production seed code (30 Days)"
            ),
            ActivationCode(
                code = "VPN-FAST-7777-UAE1",
                status = CodeStatus.ACTIVE,
                validityDays = 60,
                createdDate = now - 172800000L,
                notes = "VIP Super Fast Code (60 Days)"
            ),
            ActivationCode(
                code = "VPN-PRO1-9999-YEAR",
                status = CodeStatus.ACTIVE,
                validityDays = 365,
                createdDate = now - 3600000L,
                notes = "Annual Pass Code (365 Days)"
            ),
            ActivationCode(
                code = "VPN-EXPR-1111-TEST",
                status = CodeStatus.EXPIRED,
                validityDays = 7,
                createdDate = now - (10L * 86400000L),
                activationDate = now - (9L * 86400000L),
                expiryDate = now - (2L * 86400000L),
                notes = "Test Expired Code"
            ),
            ActivationCode(
                code = "VPN-DISA-2222-TEST",
                status = CodeStatus.DISABLED,
                validityDays = 30,
                createdDate = now - 86400000L,
                notes = "Test Administrator Disabled Code"
            ),
            ActivationCode(
                code = "VPN-USED-3333-TEST",
                status = CodeStatus.USED,
                validityDays = 30,
                createdDate = now - (5L * 86400000L),
                activationDate = now - (4L * 86400000L),
                expiryDate = now + (26L * 86400000L),
                deviceId = "DEV-OTHER-999",
                notes = "Test Bound To Another Device"
            )
        )
        dao.insertCodes(seedCodes)

        // Seed sample audit log
        _auditLogs.value = listOf(
            AuditLog(
                id = UUID.randomUUID().toString(),
                timestamp = now - 3600000L,
                adminUser = "admin@superfrist.vpn",
                action = "SYSTEM_INIT",
                target = "Core Engine",
                details = "Super Frist VPN cluster initialized with 6 global nodes."
            )
        )
    }

    private suspend fun loadSavedSelectedServer() = withContext(Dispatchers.IO) {
        val savedId = preferences.selectedServerId
        val srv = dao.getServerById(savedId) ?: getDefaultServer()
        _selectedServer.value = srv
    }

    fun selectServer(server: VpnServer) {
        _selectedServer.value = server
        preferences.selectedServerId = server.id
    }

    suspend fun verifyStoredSession(): SessionVerifyResult = withContext(Dispatchers.IO) {
        val code = preferences.getActiveCode() ?: return@withContext SessionVerifyResult.NotActivated
        val deviceId = preferences.getDeviceId()

        val dbCode = dao.getCodeByValue(code)
        if (dbCode == null) {
            preferences.clearSession()
            return@withContext SessionVerifyResult.NotActivated
        }

        if (dbCode.status == CodeStatus.DISABLED || dbCode.status == CodeStatus.REVOKED) {
            return@withContext SessionVerifyResult.Disabled("Activation Code Disabled by Administrator")
        }

        val now = System.currentTimeMillis()
        val exp = dbCode.expiryDate ?: preferences.getExpiryTimestamp()
        if (exp <= now || dbCode.status == CodeStatus.EXPIRED) {
            return@withContext SessionVerifyResult.Expired
        }

        if (dbCode.deviceId != null && dbCode.deviceId != deviceId) {
            return@withContext SessionVerifyResult.Disabled("Code Already Activated on Another Device")
        }

        val session = UserSession(
            code = dbCode.code,
            deviceId = deviceId,
            token = preferences.getSessionToken() ?: ("tok_" + UUID.randomUUID().toString().take(8)),
            activationDate = dbCode.activationDate ?: preferences.getActivationTimestamp(),
            expiryDate = exp,
            serverTime = now,
            status = dbCode.status
        )
        _currentSession.value = session
        SessionVerifyResult.Valid(session)
    }

    suspend fun activateCode(inputCode: String): ActivationResult = withContext(Dispatchers.IO) {
        val cleanCode = inputCode.trim().uppercase()
        if (cleanCode.length < 8) {
            return@withContext ActivationResult.Error("Invalid Activation Code")
        }

        val deviceId = preferences.getDeviceId()
        val dbCode = dao.getCodeByValue(cleanCode)
            ?: return@withContext ActivationResult.Error("Invalid Activation Code")

        val now = System.currentTimeMillis()

        // Verify status
        if (dbCode.status == CodeStatus.DISABLED) {
            return@withContext ActivationResult.Error("Activation Code Disabled")
        }
        if (dbCode.status == CodeStatus.REVOKED) {
            return@withContext ActivationResult.Error("Activation Code Revoked")
        }
        if (dbCode.status == CodeStatus.EXPIRED || (dbCode.expiryDate != null && dbCode.expiryDate <= now)) {
            return@withContext ActivationResult.Error("Activation Code Expired")
        }

        // Verify device authorization
        if (dbCode.deviceId != null && dbCode.deviceId != deviceId) {
            return@withContext ActivationResult.Error("Code Already Activated on Another Device")
        }

        // Calculate validity
        val activationDate = dbCode.activationDate ?: now
        val expiryDate = dbCode.expiryDate ?: (now + dbCode.validityDays * 86400000L)
        val token = "sftk_" + UUID.randomUUID().toString().replace("-", "")

        val updated = dbCode.copy(
            status = CodeStatus.USED,
            activationDate = activationDate,
            expiryDate = expiryDate,
            deviceId = deviceId,
            userId = "USR-" + cleanCode.takeLast(4),
            lastConnection = now
        )
        dao.updateCode(updated)

        // Save session locally
        preferences.saveSession(
            code = cleanCode,
            token = token,
            activationTime = activationDate,
            expiryTime = expiryDate
        )

        val session = UserSession(
            code = cleanCode,
            deviceId = deviceId,
            token = token,
            activationDate = activationDate,
            expiryDate = expiryDate,
            serverTime = now,
            status = CodeStatus.USED
        )
        _currentSession.value = session

        // Record Audit log
        addAuditLog("CODE_ACTIVATED", cleanCode, "Activated on device $deviceId")

        // Notification
        addNotification(
            title = "New Activation",
            message = "Code $cleanCode activated on $deviceId",
            type = "success"
        )

        ActivationResult.Success(session)
    }

    suspend fun generateCodes(
        count: Int,
        validityDays: Int,
        prefix: String = "VPN"
    ): List<ActivationCode> = withContext(Dispatchers.IO) {
        val random = SecureRandom()
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        val generatedList = mutableListOf<ActivationCode>()
        val now = System.currentTimeMillis()

        for (i in 1..count.coerceIn(1, 500)) {
            val part1 = (1..4).map { chars[random.nextInt(chars.length)] }.joinToString("")
            val part2 = (1..4).map { chars[random.nextInt(chars.length)] }.joinToString("")
            val part3 = (1..4).map { chars[random.nextInt(chars.length)] }.joinToString("")
            val codeStr = "$prefix-$part1-$part2-$part3"

            val codeObj = ActivationCode(
                code = codeStr,
                status = CodeStatus.ACTIVE,
                validityDays = validityDays,
                createdDate = now,
                notes = "Admin batch generated ($validityDays Days)"
            )
            generatedList.add(codeObj)
        }

        dao.insertCodes(generatedList)
        addAuditLog("BATCH_GENERATE", "$count codes", "Generated $count codes with $validityDays days validity")
        generatedList
    }

    suspend fun extendCodeExpiry(code: String, additionalDays: Int) = withContext(Dispatchers.IO) {
        val c = dao.getCodeByValue(code) ?: return@withContext
        val now = System.currentTimeMillis()
        val baseTime = if (c.expiryDate != null && c.expiryDate > now) c.expiryDate else now
        val newExpiry = baseTime + additionalDays * 86400000L

        val updated = c.copy(
            expiryDate = newExpiry,
            status = if (c.status == CodeStatus.EXPIRED) CodeStatus.USED else c.status
        )
        dao.updateCode(updated)

        // If this is currently active session on device, update stored session
        if (preferences.getActiveCode() == code) {
            preferences.saveSession(code, preferences.getSessionToken() ?: "", updated.activationDate ?: now, newExpiry)
            _currentSession.value = _currentSession.value?.copy(expiryDate = newExpiry)
        }

        addAuditLog("EXTEND_EXPIRY", code, "Extended expiry by $additionalDays days to $newExpiry")
    }

    suspend fun resetDeviceBinding(code: String) = withContext(Dispatchers.IO) {
        val c = dao.getCodeByValue(code) ?: return@withContext
        val updated = c.copy(deviceId = null)
        dao.updateCode(updated)

        if (preferences.getActiveCode() == code) {
            preferences.clearSession()
            _currentSession.value = null
        }

        addAuditLog("RESET_DEVICE", code, "Cleared bound device for code $code")
    }

    suspend fun toggleCodeStatus(code: String, newStatus: CodeStatus) = withContext(Dispatchers.IO) {
        val c = dao.getCodeByValue(code) ?: return@withContext
        val updated = c.copy(status = newStatus)
        dao.updateCode(updated)

        if (preferences.getActiveCode() == code && (newStatus == CodeStatus.DISABLED || newStatus == CodeStatus.REVOKED)) {
            _currentSession.value = null
        }

        addAuditLog("STATUS_CHANGE", code, "Status changed to ${newStatus.name}")
    }

    suspend fun deleteCode(code: String) = withContext(Dispatchers.IO) {
        dao.deleteCode(code)
        if (preferences.getActiveCode() == code) {
            preferences.clearSession()
            _currentSession.value = null
        }
        addAuditLog("DELETE_CODE", code, "Deleted activation code from system")
    }

    suspend fun toggleServerStatus(serverId: String) = withContext(Dispatchers.IO) {
        val s = dao.getServerById(serverId) ?: return@withContext
        val newStatus = if (s.status == ServerStatus.ONLINE) ServerStatus.OFFLINE else ServerStatus.ONLINE
        dao.updateServer(s.copy(status = newStatus))
    }

    fun registerActiveConnection(connection: LiveConnection) {
        val current = _liveConnections.value.filter { it.userId != connection.userId }
        _liveConnections.value = current + connection
        addNotification(
            title = "User Connected",
            message = "${connection.userId} connected to ${connection.serverName}",
            type = "info"
        )
    }

    fun unregisterConnection(userId: String) {
        _liveConnections.value = _liveConnections.value.filter { it.userId != userId }
    }

    fun addAuditLog(action: String, target: String, details: String) {
        val log = AuditLog(
            id = UUID.randomUUID().toString(),
            timestamp = System.currentTimeMillis(),
            adminUser = "admin@superfrist.vpn",
            action = action,
            target = target,
            details = details
        )
        _auditLogs.value = listOf(log) + _auditLogs.value.take(49)
    }

    fun addNotification(title: String, message: String, type: String) {
        val n = AdminNotification(
            id = UUID.randomUUID().toString(),
            title = title,
            message = message,
            type = type,
            timestamp = System.currentTimeMillis()
        )
        _notifications.value = listOf(n) + _notifications.value.take(49)
    }

    fun logout() {
        preferences.clearSession()
        _currentSession.value = null
    }
}
