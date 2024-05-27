package com.apps.adrcotfas.goodtime

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import co.touchlab.kermit.Logger
import com.apps.adrcotfas.goodtime.data.settings.SettingsRepository
import com.apps.adrcotfas.goodtime.di.injectLogger
import com.apps.adrcotfas.goodtime.labels.LabelsScreen
import com.apps.adrcotfas.goodtime.main.BottomNavigationBar
import com.apps.adrcotfas.goodtime.main.Destination
import com.apps.adrcotfas.goodtime.main.MainScreen
import com.apps.adrcotfas.goodtime.main.MainViewModel
import com.apps.adrcotfas.goodtime.settings.SettingsScreen
import com.apps.adrcotfas.goodtime.stats.StatsScreen
import com.apps.adrcotfas.goodtime.ui.ApplicationTheme
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class MainActivity : ComponentActivity(), KoinComponent {

    private val log: Logger by injectLogger("MainActivity")

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    private val viewModel: MainViewModel by viewModel()

    private val settingsRepository: SettingsRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            settingsRepository.saveAutoStartBreak(false)
            settingsRepository.saveAutoStartWork(true)
        }

        setContent {
            LaunchedEffect(savedInstanceState) {
                askForNotificationPermission()
            }
            ApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    Scaffold(bottomBar = { BottomNavigationBar(navController = navController) }) {
                        NavHost(
                            modifier = Modifier.padding(it),
                            navController = navController,
                            startDestination = Destination.Main.route
                        ) {
                            composable(Destination.Main.label) { MainScreen() }
                            composable(Destination.Labels.label) { LabelsScreen() }
                            composable(Destination.Stats.label) { StatsScreen() }
                            composable(Destination.Settings.label) { SettingsScreen() }
                        }
                    }
                }
            }
        }
    }

    //TODO: clean-up
    private fun askForNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
            && !shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)
        ) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            val settingsIntent: Intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .putExtra(Settings.EXTRA_APP_PACKAGE, applicationContext.packageName)
            startActivity(settingsIntent)
        }
    }
}