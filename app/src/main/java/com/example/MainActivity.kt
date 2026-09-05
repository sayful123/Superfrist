package com.example

import android.app.Activity
import android.net.VpnService
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.data.model.VpnConnectionStatus
import com.example.ui.screens.activation.ActivationScreen
import com.example.ui.screens.admin.InAppAdminScreen
import com.example.ui.screens.dashboard.VpnDashboardScreen
import com.example.ui.screens.servers.ServerSelectionScreen
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.theme.SuperFristTheme
import com.example.ui.viewmodel.VpnViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SuperFristTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SuperFristApp()
                }
            }
        }
    }
}

object Routes {
    const val ACTIVATION = "activation"
    const val DASHBOARD = "dashboard"
    const val SERVERS = "servers"
    const val SETTINGS = "settings"
    const val ADMIN = "admin"
}

@Composable
fun SuperFristApp(vpnViewModel: VpnViewModel = viewModel()) {
    val navController = rememberNavController()
    val context = LocalContext.current

    val currentSession by vpnViewModel.currentSession.collectAsState()
    val selectedServer by vpnViewModel.selectedServer.collectAsState()
    val visibleServers by vpnViewModel.visibleServers.collectAsState()
    val connectionStatus by vpnViewModel.connectionStatus.collectAsState()
    val telemetry by vpnViewModel.telemetry.collectAsState()
    val isActivating by vpnViewModel.isActivating.collectAsState()
    val activationError by vpnViewModel.activationError.collectAsState()
    val isCheckingSession by vpnViewModel.isCheckingSession.collectAsState()

    // Activity result launcher for VpnService.prepare
    val vpnPrepareLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            vpnViewModel.toggleVpnConnection(context)
        }
    }

    // Function to safely toggle VPN
    val onToggleVpn = {
        if (connectionStatus == VpnConnectionStatus.CONNECTED || connectionStatus == VpnConnectionStatus.CONNECTING) {
            vpnViewModel.toggleVpnConnection(context)
        } else {
            val prepareIntent = VpnService.prepare(context)
            if (prepareIntent != null) {
                vpnPrepareLauncher.launch(prepareIntent)
            } else {
                vpnViewModel.toggleVpnConnection(context)
            }
        }
    }

    // Auto-navigate between activation and dashboard based on session
    LaunchedEffect(currentSession, isCheckingSession) {
        if (!isCheckingSession) {
            val currentRoute = navController.currentDestination?.route
            if (currentSession != null) {
                if (currentRoute == Routes.ACTIVATION || currentRoute == null) {
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.ACTIVATION) { inclusive = true }
                    }
                }
            } else {
                if (currentRoute != Routes.ACTIVATION && currentRoute != Routes.ADMIN) {
                    navController.navigate(Routes.ACTIVATION) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = if (currentSession != null) Routes.DASHBOARD else Routes.ACTIVATION
    ) {
        composable(Routes.ACTIVATION) {
            ActivationScreen(
                isLoading = isActivating,
                errorMessage = activationError,
                onActivate = { code ->
                    vpnViewModel.activateCode(code)
                },
                onOpenAdmin = {
                    navController.navigate(Routes.ADMIN)
                }
            )
        }

        composable(Routes.DASHBOARD) {
            currentSession?.let { session ->
                VpnDashboardScreen(
                    session = session,
                    selectedServer = selectedServer,
                    connectionStatus = connectionStatus,
                    telemetry = telemetry,
                    onToggleConnection = onToggleVpn,
                    onSelectServer = {
                        navController.navigate(Routes.SERVERS)
                    },
                    onOpenSettings = {
                        navController.navigate(Routes.SETTINGS)
                    },
                    onOpenAdmin = {
                        navController.navigate(Routes.ADMIN)
                    }
                )
            }
        }

        composable(Routes.SERVERS) {
            ServerSelectionScreen(
                servers = visibleServers,
                selectedServerId = selectedServer.id,
                onSelectServer = { server ->
                    vpnViewModel.selectServer(server, context)
                    navController.popBackStack()
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                preferences = vpnViewModel.repository.preferences,
                onOpenAdmin = {
                    navController.navigate(Routes.ADMIN)
                },
                onLogoutSession = {
                    vpnViewModel.logout(context)
                    navController.navigate(Routes.ACTIVATION) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.ADMIN) {
            InAppAdminScreen(
                repository = vpnViewModel.repository,
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}

