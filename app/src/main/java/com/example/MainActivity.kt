package com.example

import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.SkyroViewModel
import com.example.ui.screens.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Request maximum refresh rate from the display (120Hz on supported devices)
        // This allows Compose animations and scrolling to run at full 120Hz
        window.attributes.preferredDisplayModeId = 0  // 0 = auto-select highest available
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            window.attributes = window.attributes.also { attr ->
                attr.preferredRefreshRate = 120f
            }
        }

        setContent {
            val viewModel: SkyroViewModel = viewModel()
            val currentRoute by viewModel.currentNavRoute.collectAsState()
            val isNight by viewModel.isNightTheme.collectAsState()
            val userPrefs by viewModel.userPreferences.collectAsState()

            // Centrally handle Android system back key and gesture
            val canGoBack = currentRoute != "home" && currentRoute != "splash" && currentRoute != "onboarding" && currentRoute != "login"
            androidx.activity.compose.BackHandler(enabled = canGoBack) {
                viewModel.navigateBack()
            }

            Surface(
                modifier = Modifier.fillMaxSize(),
                color = if (isNight) com.example.ui.theme.SkyroColors.MidnightNav else com.example.ui.theme.SkyroColors.WarmCream
            ) {
                AnimatedContent(
                    targetState = currentRoute,
                    transitionSpec = {
                        fadeIn() togetherWith fadeOut()
                    },
                    label = "ScreenNavigation"
                ) { route ->
                    when (route) {
                        "splash" -> {
                            SplashScreen(
                                viewModel = viewModel,
                                isNight = isNight,
                                onNavigateNext = {
                                    if (userPrefs?.isLoggedIn == true) {
                                        viewModel.navigateTo("home")
                                    } else {
                                        viewModel.navigateTo("onboarding")
                                    }
                                }
                            )
                        }
                        "onboarding" -> {
                            OnboardingScreen(
                                viewModel = viewModel,
                                isNight = isNight,
                                onFinished = {
                                    viewModel.navigateTo("login")
                                }
                            )
                        }
                        "login" -> {
                            LoginScreen(
                                viewModel = viewModel,
                                isNight = isNight,
                                onLoginSuccess = {
                                    viewModel.navigateTo("home")
                                }
                            )
                        }
                        "home" -> {
                            HomeScreen(
                                viewModel = viewModel,
                                isNight = isNight,
                                onCartClick = {
                                    viewModel.navigateTo("cart")
                                },
                                onTrackClick = {
                                    viewModel.navigateTo("live_tracking")
                                }
                            )
                        }
                        "restaurant_detail" -> {
                            RestaurantDetailScreen(
                                viewModel = viewModel,
                                isNight = isNight,
                                onBack = {
                                    viewModel.navigateBack()
                                }
                            )
                        }
                        "cart" -> {
                            CartScreen(
                                viewModel = viewModel,
                                isNight = isNight,
                                onBack = {
                                    viewModel.navigateBack()
                                }
                            )
                        }
                        "cinematic_sequence" -> {
                            CinematicSequenceScreen(
                                viewModel = viewModel,
                                isNight = isNight
                            )
                        }
                        "live_tracking" -> {
                            LiveTrackingScreen(
                                viewModel = viewModel,
                                isNight = isNight,
                                onBack = {
                                    viewModel.navigateBack()
                                }
                            )
                        }
                        "order_history" -> {
                            OrdersHistoryScreen(
                                viewModel = viewModel,
                                isNight = isNight,
                                onTrackClick = {
                                    viewModel.navigateTo("live_tracking")
                                }
                            )
                        }
                        "profile" -> {
                            ProfileScreen(
                                viewModel = viewModel,
                                isNight = isNight
                            )
                        }
                        "search_screen" -> {
                            SearchScreen(
                                viewModel = viewModel,
                                isNight = isNight
                            )
                        }
                        "saved_items" -> {
                            SavedItemsScreen(
                                viewModel = viewModel,
                                isNight = isNight
                            )
                        }
                        else -> {
                            HomeScreen(
                                viewModel = viewModel,
                                isNight = isNight,
                                onCartClick = { viewModel.navigateTo("cart") },
                                onTrackClick = { viewModel.navigateTo("live_tracking") }
                            )
                        }
                    }
                }
            }
        }
    }
}
