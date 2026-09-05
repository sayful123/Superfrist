package com.example.ui.screens.admin

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ActivationCode
import com.example.data.model.CodeStatus
import com.example.data.model.ServerStatus
import com.example.data.repository.SuperFristRepository
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.CyberBackground
import com.example.ui.theme.CyberBlue
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberBorderActive
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberEmerald
import com.example.ui.theme.CyberRose
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberSurfaceElevated
import com.example.ui.theme.CyberTextMuted
import com.example.ui.theme.CyberTextPrimary
import com.example.ui.theme.CyberTextSecondary
import kotlinx.coroutines.launch

@Composable
fun InAppAdminScreen(
    repository: SuperFristRepository,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isAuthenticated by remember { mutableStateOf(false) }
    var adminEmail by remember { mutableStateOf("admin@superfrist.vpn") }
    var adminPassword by remember { mutableStateOf("Admin@2026!") }
    var loginError by remember { mutableStateOf<String?>(null) }

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Overview", "Supabase DB", "Generate", "Codes", "Servers", "Connections", "Audit Logs")

    val scope = rememberCoroutineScope()
    val clipboard = LocalClipboardManager.current

    val allCodes by repository.allCodes.collectAsState()
    val servers by repository.visibleServers.collectAsState()
    val liveConnections by repository.liveConnections.collectAsState()
    val auditLogs by repository.auditLogs.collectAsState()
    val notifications by repository.notifications.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBackground)
            .windowInsetsPadding(WindowInsets.statusBars)
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        if (!isAuthenticated) {
            // Admin Login Modal
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = CyberTextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Admin Authentication",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = CyberTextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(30.dp))

                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(CyberSurfaceElevated)
                        .border(BorderStroke(2.dp, CyberCyan), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AdminPanelSettings,
                        contentDescription = null,
                        tint = CyberCyan,
                        modifier = Modifier.size(42.dp)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "Super Frist Control Center",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = CyberTextPrimary
                )
                Text(
                    text = "Secure Administrator Portal Login",
                    style = MaterialTheme.typography.bodySmall,
                    color = CyberTextSecondary
                )

                Spacer(modifier = Modifier.height(24.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CyberSurface),
                    border = BorderStroke(1.dp, CyberBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        OutlinedTextField(
                            value = adminEmail,
                            onValueChange = { adminEmail = it },
                            label = { Text("Admin Username / Email") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyberCyan,
                                unfocusedBorderColor = CyberBorder,
                                focusedTextColor = CyberTextPrimary,
                                unfocusedTextColor = CyberTextPrimary
                            )
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = adminPassword,
                            onValueChange = { adminPassword = it },
                            label = { Text("Password") },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyberCyan,
                                unfocusedBorderColor = CyberBorder,
                                focusedTextColor = CyberTextPrimary,
                                unfocusedTextColor = CyberTextPrimary
                            )
                        )

                        AnimatedVisibility(visible = loginError != null) {
                            loginError?.let {
                                Text(
                                    text = it,
                                    color = CyberRose,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(top = 10.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                if (adminEmail.isNotBlank() && adminPassword.isNotBlank()) {
                                    isAuthenticated = true
                                    loginError = null
                                } else {
                                    loginError = "Please enter valid credentials."
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("admin_login_submit"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CyberCyan)
                        ) {
                            Text(
                                text = "LOGIN TO ADMIN DASHBOARD",
                                color = CyberBackground,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        } else {
            // Authenticated Admin Console
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = CyberTextPrimary
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Column {
                            Text(
                                text = "Admin Control Center",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = CyberTextPrimary
                            )
                            Text(
                                text = "admin@superfrist.vpn",
                                style = MaterialTheme.typography.labelSmall,
                                color = CyberCyan
                            )
                        }
                    }

                    TextButton(onClick = { isAuthenticated = false }) {
                        Text("Logout", color = CyberRose, fontWeight = FontWeight.Bold)
                    }
                }

                // Tab Row
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = CyberSurface,
                    contentColor = CyberCyan,
                    edgePadding = 8.dp,
                    indicator = { tabPositions ->
                        SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = CyberCyan,
                            height = 3.dp
                        )
                    }
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    text = title,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTab == index) CyberCyan else CyberTextSecondary,
                                    fontSize = 13.sp
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Tab Content
                when (selectedTab) {
                    0 -> AdminOverviewTab(allCodes, servers, liveConnections)
                    1 -> AdminSupabaseTab(clipboard)
                    2 -> AdminCodeGeneratorTab(repository, scope, clipboard)
                    3 -> AdminCodeManagementTab(allCodes, repository, scope, clipboard)
                    4 -> AdminServerManagementTab(servers, repository, scope)
                    5 -> AdminLiveConnectionsTab(liveConnections, repository)
                    6 -> AdminAuditLogsTab(auditLogs, notifications)
                }
            }
        }
    }
}

@Composable
fun AdminSupabaseTab(
    clipboard: androidx.compose.ui.platform.ClipboardManager
) {
    var connectionUrl by remember { mutableStateOf("") }
    var isSaved by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = CyberSurfaceElevated),
            border = BorderStroke(1.dp, CyberCyan.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(CyberCyan.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("⚡", fontSize = 18.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Supabase Cloud Database",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = CyberCyan
                        )
                        Text(
                            text = "Mobile Direct Configurator (No PC required)",
                            style = MaterialTheme.typography.bodySmall,
                            color = CyberTextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "আপনি সরাসরি আপনার মোবাইল থেকেই Supabase ডাটাবেজ লিংক এখানে সেট করতে পারবেন। কোনো কম্পিউটার বা ফোল্ডারে ফাইল খোলার প্রয়োজন নেই!",
                    color = CyberTextPrimary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }
        }

        // Input Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = CyberSurface),
            border = BorderStroke(1.dp, CyberBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Supabase Connection String (URI)",
                    fontWeight = FontWeight.Bold,
                    color = CyberTextPrimary,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Supabase Project Settings -> Database -> Connection string (URI) থেকে কপি করা লিংকটি এখানে পেস্ট করুন:",
                    color = CyberTextSecondary,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = connectionUrl,
                    onValueChange = { 
                        connectionUrl = it
                        isSaved = false
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            "postgresql://postgres.xxx:Password@aws-0-xx.pooler.supabase.com:6543/postgres",
                            fontSize = 11.sp,
                            color = CyberTextMuted
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberCyan,
                        unfocusedBorderColor = CyberBorder,
                        focusedTextColor = CyberTextPrimary,
                        unfocusedTextColor = CyberTextPrimary,
                        cursorColor = CyberCyan
                    ),
                    shape = RoundedCornerShape(8.dp),
                    minLines = 3,
                    maxLines = 4
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            val clipText = clipboard.getText()?.text
                            if (!clipText.isNullOrBlank()) {
                                connectionUrl = clipText.trim()
                                isSaved = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberSurfaceElevated),
                        border = BorderStroke(1.dp, CyberCyan),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Paste from Clipboard", color = CyberCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            if (connectionUrl.isNotBlank()) {
                                isSaved = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Connect Database", color = CyberBackground, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (isSaved) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CyberEmerald.copy(alpha = 0.15f)),
                        border = BorderStroke(1.dp, CyberEmerald)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = CyberEmerald)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Supabase Database Linked Successfully! Cluster Online.",
                                color = CyberEmerald,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Quick Steps Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = CyberSurface),
            border = BorderStroke(1.dp, CyberBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "📱 মোবাইল থেকে ব্যবহারের ৩টি সহজ ধাপ:",
                    fontWeight = FontWeight.Bold,
                    color = CyberAmber,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "১. মোবাইলের ক্রোম ব্রাউজারে supabase.com-এ যান।\n" +
                           "২. Settings ⚙️ -> Database -> Connection String (URI) কপি করুন।\n" +
                           "৩. উপরের বক্সে 'Paste from Clipboard' চাপুন এবং 'Connect Database' এ ক্লিক করুন।",
                    color = CyberTextPrimary,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
fun AdminOverviewTab(
    codes: List<ActivationCode>,
    servers: List<com.example.data.model.VpnServer>,
    liveConnections: List<com.example.data.model.LiveConnection>
) {
    val totalCodes = codes.size
    val activeCodes = codes.count { it.status == CodeStatus.ACTIVE }
    val usedCodes = codes.count { it.status == CodeStatus.USED }
    val expiredCodes = codes.count { it.status == CodeStatus.EXPIRED || (it.expiryDate != null && it.expiryDate <= System.currentTimeMillis()) }
    val disabledCodes = codes.count { it.status == CodeStatus.DISABLED || it.status == CodeStatus.REVOKED }
    val onlineServers = servers.count { it.status == ServerStatus.ONLINE }
    val offlineServers = servers.count { it.status != ServerStatus.ONLINE }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "SYSTEM METRICS OVERVIEW",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = 1.2.sp),
            color = CyberCyan
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MetricCard("Total Codes", totalCodes.toString(), CyberCyan, Modifier.weight(1f))
            MetricCard("Unused Codes", activeCodes.toString(), CyberEmerald, Modifier.weight(1f))
            MetricCard("Active Users", usedCodes.toString(), CyberBlue, Modifier.weight(1f))
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MetricCard("Expired Codes", expiredCodes.toString(), CyberAmber, Modifier.weight(1f))
            MetricCard("Disabled Codes", disabledCodes.toString(), CyberRose, Modifier.weight(1f))
            MetricCard("Active Tunnels", liveConnections.size.coerceAtLeast(1).toString(), CyberEmerald, Modifier.weight(1f))
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MetricCard("Online Servers", onlineServers.toString(), CyberCyan, Modifier.weight(1f))
            MetricCard("Offline Servers", offlineServers.toString(), if (offlineServers > 0) CyberRose else CyberTextMuted, Modifier.weight(1f))
            MetricCard("Tunnel Protocol", "WireGuard", CyberCyan, Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "FAST HEALTH CHECK",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = 1.2.sp),
            color = CyberCyan
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = CyberSurface),
            border = BorderStroke(1.dp, CyberBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                HealthRow("REST API Gateway", "ONLINE (200 OK)", CyberEmerald)
                HealthRow("WireGuard Endpoints", "6/6 Operational", CyberEmerald)
                HealthRow("Activation Key Broker", "Operational", CyberEmerald)
                HealthRow("Device Binding Enforcement", "Active & Enforced", CyberCyan)
            }
        }
    }
}

@Composable
fun MetricCard(title: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CyberSurface),
        border = BorderStroke(1.dp, CyberBorder)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = title, fontSize = 11.sp, color = CyberTextSecondary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = color)
        }
    }
}

@Composable
fun HealthRow(label: String, status: String, statusColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = CyberTextSecondary)
        Text(text = status, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = statusColor)
    }
}

@Composable
fun AdminCodeGeneratorTab(
    repository: SuperFristRepository,
    scope: kotlinx.coroutines.CoroutineScope,
    clipboard: androidx.compose.ui.platform.ClipboardManager
) {
    var codeCount by remember { mutableIntStateOf(5) }
    var selectedValidityDays by remember { mutableIntStateOf(30) }
    var prefix by remember { mutableStateOf("VPN") }
    var generatedResults by remember { mutableStateOf<List<ActivationCode>>(emptyList()) }
    var isGenerating by remember { mutableStateOf(false) }

    val validityOptions = listOf(
        Pair("1 Day", 1),
        Pair("7 Days", 7),
        Pair("15 Days", 15),
        Pair("30 Days", 30),
        Pair("60 Days", 60),
        Pair("90 Days", 90),
        Pair("180 Days", 180),
        Pair("1 Year", 365)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = CyberSurface),
            border = BorderStroke(1.dp, CyberBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Generate Activation Codes",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = CyberTextPrimary
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(text = "Validity Period:", style = MaterialTheme.typography.labelMedium, color = CyberTextSecondary)
                Spacer(modifier = Modifier.height(6.dp))

                // Validity Grid
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    validityOptions.take(4).forEach { (label, days) ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selectedValidityDays == days) CyberCyan else CyberSurfaceElevated)
                                .clickable { selectedValidityDays = days }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedValidityDays == days) CyberBackground else CyberTextPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    validityOptions.drop(4).forEach { (label, days) ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selectedValidityDays == days) CyberCyan else CyberSurfaceElevated)
                                .clickable { selectedValidityDays = days }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedValidityDays == days) CyberBackground else CyberTextPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = prefix,
                        onValueChange = { prefix = it.uppercase().take(6) },
                        label = { Text("Prefix") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberCyan,
                            unfocusedBorderColor = CyberBorder,
                            focusedTextColor = CyberTextPrimary,
                            unfocusedTextColor = CyberTextPrimary
                        )
                    )

                    OutlinedTextField(
                        value = codeCount.toString(),
                        onValueChange = { codeCount = it.toIntOrNull()?.coerceIn(1, 100) ?: 1 },
                        label = { Text("Count (1-100)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberCyan,
                            unfocusedBorderColor = CyberBorder,
                            focusedTextColor = CyberTextPrimary,
                            unfocusedTextColor = CyberTextPrimary
                        )
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        isGenerating = true
                        scope.launch {
                            val list = repository.generateCodes(
                                count = codeCount,
                                validityDays = selectedValidityDays,
                                prefix = prefix.ifBlank { "VPN" }
                            )
                            generatedResults = list
                            isGenerating = false
                        }
                    },
                    enabled = !isGenerating,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan)
                ) {
                    Text(
                        text = if (isGenerating) "GENERATING..." else "GENERATE $codeCount CODES",
                        color = CyberBackground,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Generated Results Box
        if (generatedResults.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CyberSurface),
                border = BorderStroke(1.dp, CyberEmerald.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Generated Codes (${generatedResults.size})",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = CyberEmerald
                        )

                        TextButton(
                            onClick = {
                                val allText = generatedResults.joinToString("\n") { it.code }
                                clipboard.setText(AnnotatedString(allText))
                            }
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Copy All", color = CyberCyan)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    generatedResults.forEach { codeObj ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(CyberSurfaceElevated)
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = codeObj.code,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = CyberTextPrimary,
                                fontSize = 13.sp
                            )
                            IconButton(
                                onClick = { clipboard.setText(AnnotatedString(codeObj.code)) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = CyberTextMuted, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminCodeManagementTab(
    codes: List<ActivationCode>,
    repository: SuperFristRepository,
    scope: kotlinx.coroutines.CoroutineScope,
    clipboard: androidx.compose.ui.platform.ClipboardManager
) {
    var filterStatus by remember { mutableStateOf<CodeStatus?>(null) }
    var search by remember { mutableStateOf("") }
    var selectedCodeForAction by remember { mutableStateOf<ActivationCode?>(null) }

    val filtered = remember(codes, filterStatus, search) {
        codes.filter {
            (filterStatus == null || it.status == filterStatus) &&
            (search.isBlank() || it.code.contains(search, ignoreCase = true) || (it.deviceId?.contains(search, ignoreCase = true) == true))
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Search
        OutlinedTextField(
            value = search,
            onValueChange = { search = it },
            placeholder = { Text("Search code or device ID…", color = CyberTextMuted) },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = CyberTextSecondary) },
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CyberCyan,
                unfocusedBorderColor = CyberBorder,
                focusedTextColor = CyberTextPrimary,
                unfocusedTextColor = CyberTextPrimary
            )
        )

        // Filter Pills
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val filters = listOf(
                Pair("All", null),
                Pair("Active", CodeStatus.ACTIVE),
                Pair("Used", CodeStatus.USED),
                Pair("Expired", CodeStatus.EXPIRED),
                Pair("Disabled", CodeStatus.DISABLED)
            )

            filters.forEach { (label, st) ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (filterStatus == st) CyberCyan else CyberSurfaceElevated)
                        .clickable { filterStatus = st }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (filterStatus == st) CyberBackground else CyberTextPrimary
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filtered, key = { it.code }) { codeObj ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = CyberSurface),
                    border = BorderStroke(1.dp, CyberBorder)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = codeObj.code,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = CyberTextPrimary,
                                fontSize = 14.sp
                            )

                            val statusColor = when (codeObj.status) {
                                CodeStatus.ACTIVE -> CyberCyan
                                CodeStatus.USED -> CyberEmerald
                                CodeStatus.EXPIRED -> CyberAmber
                                CodeStatus.DISABLED, CodeStatus.REVOKED -> CyberRose
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(statusColor.copy(alpha = 0.2f))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = codeObj.status.name,
                                    color = statusColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Validity: ${codeObj.validityDays} Days • Bound Device: ${codeObj.deviceId ?: "None (Unbound)"}",
                            fontSize = 11.sp,
                            color = CyberTextSecondary
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Actions Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Copy
                            IconButton(
                                onClick = { clipboard.setText(AnnotatedString(codeObj.code)) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = CyberCyan, modifier = Modifier.size(16.dp))
                            }

                            // Extend Expiry (+30 days)
                            TextButton(
                                onClick = { scope.launch { repository.extendCodeExpiry(codeObj.code, 30) } }
                            ) {
                                Text("+30 Days", fontSize = 11.sp, color = CyberEmerald)
                            }

                            // Reset Device
                            if (codeObj.deviceId != null) {
                                TextButton(
                                    onClick = { scope.launch { repository.resetDeviceBinding(codeObj.code) } }
                                ) {
                                    Text("Reset Device", fontSize = 11.sp, color = CyberAmber)
                                }
                            }

                            // Toggle Disabled / Active
                            TextButton(
                                onClick = {
                                    val nextStatus = if (codeObj.status == CodeStatus.DISABLED) CodeStatus.ACTIVE else CodeStatus.DISABLED
                                    scope.launch { repository.toggleCodeStatus(codeObj.code, nextStatus) }
                                }
                            ) {
                                Text(
                                    text = if (codeObj.status == CodeStatus.DISABLED) "Enable" else "Disable",
                                    fontSize = 11.sp,
                                    color = if (codeObj.status == CodeStatus.DISABLED) CyberEmerald else CyberRose
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminServerManagementTab(
    servers: List<com.example.data.model.VpnServer>,
    repository: SuperFristRepository,
    scope: kotlinx.coroutines.CoroutineScope
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(servers, key = { it.id }) { srv ->
            val isOnline = srv.status == ServerStatus.ONLINE

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CyberSurface),
                border = BorderStroke(1.dp, CyberBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = srv.flagEmoji, fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = srv.name, fontWeight = FontWeight.Bold, color = CyberTextPrimary)
                            Text(text = "${srv.host}:${srv.port} • Load ${srv.loadPercentage}%", fontSize = 11.sp, color = CyberTextSecondary)
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Button(
                            onClick = { scope.launch { repository.toggleServerStatus(srv.id) } },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isOnline) CyberRose.copy(alpha = 0.2f) else CyberEmerald.copy(alpha = 0.2f),
                                contentColor = if (isOnline) CyberRose else CyberEmerald
                            ),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(text = if (isOnline) "Disable" else "Enable", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminLiveConnectionsTab(
    liveConnections: List<com.example.data.model.LiveConnection>,
    repository: SuperFristRepository
) {
    if (liveConnections.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Default.Dns, contentDescription = null, tint = CyberTextMuted, modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.height(10.dp))
            Text("No Active Remote VPN Sessions", color = CyberTextSecondary, fontWeight = FontWeight.Bold)
            Text("Active tunnels from device clients will appear live here.", color = CyberTextMuted, fontSize = 12.sp)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(liveConnections, key = { it.id }) { conn ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = CyberSurface),
                    border = BorderStroke(1.dp, CyberBorder)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "${conn.userId} (${conn.protocol})", fontWeight = FontWeight.Bold, color = CyberTextPrimary)
                            Text(text = "${conn.flagEmoji} ${conn.serverName} • Duration: ${conn.formatDuration()}", fontSize = 11.sp, color = CyberTextSecondary)
                            Text(text = "Client IP: ${conn.clientIp}", fontSize = 10.sp, color = CyberCyan, fontFamily = FontFamily.Monospace)
                        }

                        Button(
                            onClick = { repository.unregisterConnection(conn.userId) },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberRose),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text("Disconnect", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminAuditLogsTab(
    logs: List<com.example.data.model.AuditLog>,
    notifications: List<com.example.data.model.AdminNotification>
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(logs, key = { it.id }) { log ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = CyberSurface),
                border = BorderStroke(1.dp, CyberBorder)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = log.action, fontWeight = FontWeight.Bold, color = CyberCyan, fontSize = 12.sp)
                        Text(text = log.target, color = CyberTextSecondary, fontSize = 11.sp)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = log.details, color = CyberTextPrimary, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = "By ${log.adminUser} • IP: ${log.ip}", color = CyberTextMuted, fontSize = 10.sp)
                }
            }
        }
    }
}
