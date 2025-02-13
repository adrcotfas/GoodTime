/**
 *     Goodtime Productivity
 *     Copyright (C) 2025 Adrian Cotfas
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.apps.adrcotfas.goodtime

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.TransformOrigin
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import co.touchlab.kermit.Logger
import com.apps.adrcotfas.goodtime.bl.notifications.NotificationArchManager
import com.apps.adrcotfas.goodtime.data.settings.isDarkTheme
import com.apps.adrcotfas.goodtime.di.injectLogger
import com.apps.adrcotfas.goodtime.labels.addedit.AddEditLabelScreen
import com.apps.adrcotfas.goodtime.labels.archived.ArchivedLabelsScreen
import com.apps.adrcotfas.goodtime.labels.main.LabelsScreen
import com.apps.adrcotfas.goodtime.labels.main.LabelsViewModel
import com.apps.adrcotfas.goodtime.main.AboutDest
import com.apps.adrcotfas.goodtime.main.AddEditLabelDest
import com.apps.adrcotfas.goodtime.main.ArchivedLabelsDest
import com.apps.adrcotfas.goodtime.main.BackupDest
import com.apps.adrcotfas.goodtime.main.LabelsDest
import com.apps.adrcotfas.goodtime.main.LicensesDest
import com.apps.adrcotfas.goodtime.main.MainDest
import com.apps.adrcotfas.goodtime.main.MainScreen
import com.apps.adrcotfas.goodtime.main.NotificationSettingsDest
import com.apps.adrcotfas.goodtime.main.OnboardingDest
import com.apps.adrcotfas.goodtime.main.SettingsDest
import com.apps.adrcotfas.goodtime.main.StatsDest
import com.apps.adrcotfas.goodtime.main.TimerStyleDest
import com.apps.adrcotfas.goodtime.main.route
import com.apps.adrcotfas.goodtime.onboarding.MainViewModel
import com.apps.adrcotfas.goodtime.onboarding.OnboardingScreen
import com.apps.adrcotfas.goodtime.settings.SettingsScreen
import com.apps.adrcotfas.goodtime.settings.SettingsViewModel
import com.apps.adrcotfas.goodtime.settings.about.AboutScreen
import com.apps.adrcotfas.goodtime.settings.about.LicensesScreen
import com.apps.adrcotfas.goodtime.settings.backup.BackupScreen
import com.apps.adrcotfas.goodtime.settings.notifications.NotificationsScreen
import com.apps.adrcotfas.goodtime.settings.timerstyle.TimerStyleScreen
import com.apps.adrcotfas.goodtime.stats.StatisticsScreen
import com.apps.adrcotfas.goodtime.ui.ApplicationTheme
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class MainActivity : ComponentActivity(), KoinComponent {

    private val log: Logger by injectLogger("MainActivity")
    private val notificationManager: NotificationArchManager by inject()
    private val viewModel: MainViewModel by viewModel<MainViewModel>()
    private var fullScreenJob: Job? = null

    @SuppressLint("UnrememberedGetBackStackEntry", "UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        log.d { "onCreate" }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
        }
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            val coroutineScope = rememberCoroutineScope()
            val onboardingState by viewModel.uiState.collectAsStateWithLifecycle()

            // TODO: add loading/splash screen
            if (onboardingState.loading) {
                return@setContent
            }

            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val isDarkTheme = uiState.darkThemePreference.isDarkTheme(isSystemInDarkTheme())
            val workSessionIsInProgress = uiState.isWorkSessionInProgress
            val isActive = uiState.isActive
            val isFinished = uiState.isFinished
            var isMainScreen by rememberSaveable { mutableStateOf(true) }

            val fullscreenMode = isMainScreen && uiState.fullscreenMode && isActive
            LaunchedEffect(fullscreenMode) {
                fullscreenMode.let {
                    toggleFullscreen(it)
                    if (!it) fullScreenJob?.cancel()
                }
            }
            var hideBottomBar by remember(fullscreenMode) {
                mutableStateOf(fullscreenMode)
            }
            val onSurfaceClick = {
                if (fullscreenMode) {
                    fullScreenJob?.cancel()
                    fullScreenJob = coroutineScope.launch {
                        toggleFullscreen(false)
                        hideBottomBar = false
                        executeDelayed(3000) {
                            toggleFullscreen(true)
                            hideBottomBar = true
                        }
                    }
                }
            }

            toggleKeepScreenOn(isActive)
            if (notificationManager.isNotificationPolicyAccessGranted()) {
                if (uiState.dndDuringWork) {
                    notificationManager.toggleDndMode(workSessionIsInProgress)
                } else {
                    notificationManager.toggleDndMode(false)
                }
            }

            val startDestination = remember(onboardingState.onboardingFinished) {
                if (onboardingState.onboardingFinished) {
                    MainDest
                } else {
                    OnboardingDest
                }
            }

            DisposableEffect(isDarkTheme, onboardingState.onboardingFinished) {
                val considerDarkTheme =
                    if (!onboardingState.onboardingFinished) {
                        false
                    } else {
                        isDarkTheme
                    }
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(
                        android.graphics.Color.TRANSPARENT,
                        android.graphics.Color.TRANSPARENT,
                    ) { considerDarkTheme },
                    navigationBarStyle = SystemBarStyle.auto(
                        lightScrim,
                        darkScrim,
                    ) { considerDarkTheme },
                )
                onDispose {}
            }

            ApplicationTheme(darkTheme = isDarkTheme, dynamicColor = uiState.isDynamicColor) {
                val navController = rememberNavController()
                navController.addOnDestinationChangedListener { _, destination, _ ->
                    isMainScreen = destination.route == MainDest.route
                }

                LaunchedEffect(isFinished) {
                    val shouldNavigate = navController.currentDestination?.route != MainDest.route
                    if (isFinished && shouldNavigate) navController.navigate(MainDest)
                }
                Scaffold {
                    NavHost(
                        navController = navController,
                        startDestination = startDestination,
                        popExitTransition = {
                            scaleOut(
                                targetScale = 0.9f,
                                transformOrigin = TransformOrigin(pivotFractionX = 0.5f, pivotFractionY = 0.5f),
                            )
                        },
                        popEnterTransition = {
                            EnterTransition.None
                        },
                    ) {
                        composable<OnboardingDest> { OnboardingScreen() }
                        composable<MainDest> {
                            MainScreen(
                                onSurfaceClick = onSurfaceClick,
                                hideBottomBar = hideBottomBar,
                                navController = navController,
                            )
                        }
                        composable<LabelsDest> {
                            val backStackEntry =
                                remember { navController.getBackStackEntry(LabelsDest) }
                            val viewModel =
                                koinViewModel<LabelsViewModel>(viewModelStoreOwner = backStackEntry)
                            LabelsScreen(
                                onNavigateToLabel = navController::navigate,
                                onNavigateToArchivedLabels = {
                                    navController.navigate(ArchivedLabelsDest)
                                },
                                onNavigateBack = navController::popBackStack,
                                viewModel = viewModel,
                            )
                        }
                        composable<AddEditLabelDest> {
                            val backStackEntry =
                                remember { navController.getBackStackEntry(LabelsDest) }
                            val viewModel =
                                koinViewModel<LabelsViewModel>(viewModelStoreOwner = backStackEntry)
                            val addEditLabelDest = it.toRoute<AddEditLabelDest>()
                            AddEditLabelScreen(
                                labelName = addEditLabelDest.name,
                                onNavigateBack = navController::popBackStack,
                                viewModel = viewModel,
                            )
                        }
                        composable<ArchivedLabelsDest> {
                            val backStackEntry =
                                remember { navController.getBackStackEntry(LabelsDest) }
                            val viewModel =
                                koinViewModel<LabelsViewModel>(viewModelStoreOwner = backStackEntry)
                            ArchivedLabelsScreen(
                                onNavigateBack = navController::popBackStack,
                                viewModel = viewModel,
                            )
                        }
                        composable<StatsDest> { StatisticsScreen(onNavigateBack = navController::popBackStack) }
                        composable<SettingsDest> {
                            val backStackEntry =
                                remember { navController.getBackStackEntry(SettingsDest) }
                            val viewModel: SettingsViewModel =
                                koinViewModel(viewModelStoreOwner = backStackEntry)
                            SettingsScreen(
                                viewModel = viewModel,
                                onNavigateToTimerStyle = { navController.navigate(TimerStyleDest) },
                                onNavigateToNotifications = {
                                    navController.navigate(
                                        NotificationSettingsDest,
                                    )
                                },
                                onNavigateBack = navController::popBackStack,
                            )
                        }
                        composable<TimerStyleDest> {
                            val backStackEntry =
                                remember { navController.getBackStackEntry(SettingsDest) }
                            val viewModel: SettingsViewModel =
                                koinViewModel(viewModelStoreOwner = backStackEntry)
                            TimerStyleScreen(
                                viewModel = viewModel,
                                onNavigateBack = navController::popBackStack,
                            )
                        }
                        composable<NotificationSettingsDest> {
                            val backStackEntry =
                                remember { navController.getBackStackEntry(SettingsDest) }
                            val viewModel: SettingsViewModel =
                                koinViewModel(viewModelStoreOwner = backStackEntry)
                            NotificationsScreen(
                                viewModel = viewModel,
                                onNavigateBack = navController::popBackStack,
                            )
                        }

                        composable<BackupDest> {
                            BackupScreen(onNavigateBack = navController::popBackStack)
                        }
                        composable<AboutDest> {
                            AboutScreen(
                                onNavigateToLicenses = {
                                    navController.navigate(
                                        LicensesDest,
                                    )
                                },
                                onNavigateBack = navController::popBackStack,
                            )
                        }
                        composable<LicensesDest> {
                            LicensesScreen(onNavigateBack = navController::popBackStack)
                        }
                    }
                }
            }
        }
    }

    private fun toggleKeepScreenOn(enabled: Boolean) {
        if (enabled) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    private fun toggleFullscreen(enabled: Boolean) {
        val windowInsetsController =
            WindowCompat.getInsetsController(window, window.decorView)

        if (enabled) {
            windowInsetsController.apply {
                hide(WindowInsetsCompat.Type.systemBars())
                systemBarsBehavior = BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            windowInsetsController.show(WindowInsetsCompat.Type.systemBars())
        }
    }

    private suspend fun executeDelayed(delay: Long, block: () -> Unit) {
        coroutineScope {
            delay(delay)
            block()
        }
    }
}

private val lightScrim = android.graphics.Color.argb(0xe6, 0xFF, 0xFF, 0xFF)
private val darkScrim = android.graphics.Color.argb(0x80, 0x1b, 0x1b, 0x1b)
