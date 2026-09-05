package com.example.ui.screens.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.VpnLock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.UserSession
import com.example.data.model.VpnConnectionStatus
import com.example.data.model.VpnServer
import com.example.data.model.VpnTelemetry
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.CyberBackground
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberBorderActive
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberCyanGlow
import com.example.ui.theme.CyberEmerald
import com.example.ui.theme.CyberEmeraldGlow
import com.example.ui.theme.CyberRose
import com.example.ui.theme.CyberRoseGlow
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberSurfaceElevated
import com.example.ui.theme.CyberTextMuted
import com.example.ui.theme.CyberTextPrimary
import com.example.ui.theme.CyberTextSecondary

@Composable
fun VpnDashboardScreen(
    session: UserSession,
    selectedServer: VpnServer,
    connectionStatus: VpnConnectionStatus,
    telemetry: VpnTelemetry,
    onToggleConnection: () -> Unit,
    onSelectServer: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenAdmin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isConnected = connectionStatus == VpnConnectionStatus.CONNECTED
    val isConnecting = connectionStatus == VpnConnectionStatus.CONNECTING
    val isDisconnecting = connectionStatus == VpnConnectionStatus.DISCONNECTING

    // Glow pulse animation for connect button
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (isConnected || isConnecting) 1.15f else 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val buttonColor by animateColorAsState(
        targetValue = when (connectionStatus) {
            VpnConnectionStatus.CONNECTED -> CyberEmerald
            VpnConnectionStatus.CONNECTING -> CyberAmber
            VpnConnectionStatus.DISCONNECTING -> CyberAmber
            VpnConnectionStatus.DISCONNECTED -> CyberCyan
        },
        label = "buttonColor"
    )

    val glowColor = when (connectionStatus) {
        VpnConnectionStatus.CONNECTED -> CyberEmeraldGlow
        VpnConnectionStatus.CONNECTING -> CyberAmber.copy(alpha = 0.25f)
        VpnConnectionStatus.DISCONNECTING -> CyberAmber.copy(alpha = 0.25f)
        VpnConnectionStatus.DISCONNECTED -> CyberCyanGlow
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBackground)
            .windowInsetsPadding(WindowInsets.statusBars)
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top App Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(CyberSurfaceElevated)
                            .border(BorderStroke(1.dp, CyberBorderActive), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = CyberCyan,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = CyberTextPrimary
                        )
                        Text(
                            text = "WireGuard Core v1.0",
                            style = MaterialTheme.typography.labelSmall,
                            color = CyberTextMuted
                        )
                    }
                }

                Row {
                    IconButton(
                        onClick = onOpenAdmin,
                        modifier = Modifier.testTag("admin_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = "Admin Panel",
                            tint = CyberCyan
                        )
                    }
                    IconButton(
                        onClick = onOpenSettings,
                        modifier = Modifier.testTag("settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = CyberTextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Connection Status Pill
            Surface(
                shape = RoundedCornerShape(30.dp),
                color = when (connectionStatus) {
                    VpnConnectionStatus.CONNECTED -> CyberEmerald.copy(alpha = 0.15f)
                    VpnConnectionStatus.CONNECTING, VpnConnectionStatus.DISCONNECTING -> CyberAmber.copy(alpha = 0.15f)
                    VpnConnectionStatus.DISCONNECTED -> CyberSurfaceElevated
                },
                border = BorderStroke(
                    1.dp,
                    when (connectionStatus) {
                        VpnConnectionStatus.CONNECTED -> CyberEmerald
                        VpnConnectionStatus.CONNECTING, VpnConnectionStatus.DISCONNECTING -> CyberAmber
                        VpnConnectionStatus.DISCONNECTED -> CyberBorder
                    }
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(buttonColor)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when (connectionStatus) {
                            VpnConnectionStatus.CONNECTED -> stringResource(R.string.status_connected)
                            VpnConnectionStatus.CONNECTING -> stringResource(R.string.status_connecting)
                            VpnConnectionStatus.DISCONNECTING -> stringResource(R.string.status_disconnecting)
                            VpnConnectionStatus.DISCONNECTED -> stringResource(R.string.status_disconnected)
                        },
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        ),
                        color = when (connectionStatus) {
                            VpnConnectionStatus.CONNECTED -> CyberEmerald
                            VpnConnectionStatus.CONNECTING, VpnConnectionStatus.DISCONNECTING -> CyberAmber
                            VpnConnectionStatus.DISCONNECTED -> CyberTextSecondary
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(26.dp))

            // Main Large Connect Button (Centerpiece)
            Box(
                modifier = Modifier
                    .size(220.dp)
                    .clickable { onToggleConnection() }
                    .testTag("connect_button"),
                contentAlignment = Alignment.Center
            ) {
                // Outer Pulse Ring
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(glowColor)
                )

                // Middle Border Ring
                Box(
                    modifier = Modifier
                        .size(175.dp)
                        .clip(CircleShape)
                        .background(CyberSurface)
                        .border(
                            BorderStroke(3.dp, buttonColor.copy(alpha = 0.7f)),
                            CircleShape
                        )
                )

                // Inner Illuminated Button Core
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    buttonColor.copy(alpha = 0.35f),
                                    CyberSurfaceElevated
                                )
                            )
                        )
                        .border(BorderStroke(1.5.dp, buttonColor), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = if (isConnected) Icons.Default.VpnLock else Icons.Default.PowerSettingsNew,
                            contentDescription = if (isConnected) "Disconnect" else "Connect",
                            tint = buttonColor,
                            modifier = Modifier.size(46.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = when (connectionStatus) {
                                VpnConnectionStatus.CONNECTED -> stringResource(R.string.button_disconnect)
                                VpnConnectionStatus.CONNECTING -> "LINKING…"
                                VpnConnectionStatus.DISCONNECTING -> "CLOSING…"
                                VpnConnectionStatus.DISCONNECTED -> stringResource(R.string.button_connect)
                            },
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.sp
                            ),
                            color = buttonColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Connection Duration Timer
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = null,
                    tint = if (isConnected) CyberCyan else CyberTextMuted,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.connection_time) + ": ",
                    style = MaterialTheme.typography.bodySmall,
                    color = CyberTextSecondary
                )
                Text(
                    text = if (isConnected) telemetry.formatDuration() else "00:00:00",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    ),
                    color = if (isConnected) CyberTextPrimary else CyberTextMuted
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Current Server Selection Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectServer() }
                    .testTag("server_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CyberSurface),
                border = BorderStroke(1.dp, CyberBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = selectedServer.flagEmoji,
                            fontSize = 28.sp
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = stringResource(R.string.current_server),
                                style = MaterialTheme.typography.labelSmall,
                                color = CyberTextMuted
                            )
                            Text(
                                text = selectedServer.name,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = CyberTextPrimary
                            )
                            Text(
                                text = "${selectedServer.city} • Ping ${if (isConnected) telemetry.latencyMs else selectedServer.pingMs} ms",
                                style = MaterialTheme.typography.bodySmall,
                                color = CyberCyan
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(CyberSurfaceElevated)
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.change_server),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = CyberCyan
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowForwardIos,
                            contentDescription = null,
                            tint = CyberTextMuted,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Remaining Validity Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CyberSurface),
                border = BorderStroke(1.dp, CyberBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = CyberCyan,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = stringResource(R.string.remaining_validity),
                                style = MaterialTheme.typography.labelSmall,
                                color = CyberTextMuted
                            )
                            Text(
                                text = session.formatRemainingTime(),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = CyberTextPrimary
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(CyberEmerald.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "ACTIVE PASS",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = CyberEmerald
                        )
                    }
                }
            }

            // Live Telemetry Details (Speed, IP, Latency)
            AnimatedVisibility(visible = isConnected) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CyberSurface),
                        border = BorderStroke(1.dp, CyberBorderActive.copy(alpha = 0.4f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "LIVE TUNNEL TELEMETRY",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.2.sp
                                ),
                                color = CyberCyan,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            // Speed Metrics Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Download Speed
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.TrendingDown,
                                            contentDescription = null,
                                            tint = CyberCyan,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = stringResource(R.string.download_speed),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = CyberTextSecondary
                                        )
                                    }
                                    Text(
                                        text = telemetry.formatDownloadSpeed(),
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        ),
                                        color = CyberTextPrimary
                                    )
                                }

                                // Upload Speed
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.TrendingUp,
                                            contentDescription = null,
                                            tint = CyberEmerald,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = stringResource(R.string.upload_speed),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = CyberTextSecondary
                                        )
                                    }
                                    Text(
                                        text = telemetry.formatUploadSpeed(),
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        ),
                                        color = CyberTextPrimary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // IP & Latency Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Public,
                                            contentDescription = null,
                                            tint = CyberTextMuted,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = stringResource(R.string.virtual_ip),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = CyberTextSecondary
                                        )
                                    }
                                    Text(
                                        text = telemetry.currentIp,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            fontFamily = FontFamily.Monospace
                                        ),
                                        color = CyberTextPrimary
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.NetworkCheck,
                                            contentDescription = null,
                                            tint = CyberTextMuted,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = stringResource(R.string.ping_latency),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = CyberTextSecondary
                                        )
                                    }
                                    Text(
                                        text = "${telemetry.latencyMs} ms (Optimal)",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.SemiBold
                                        ),
                                        color = CyberEmerald
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
