package com.ogapp.subai.ui.nav

import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import com.ogapp.subai.ui.screens.ChargingScreen
import com.ogapp.subai.ui.screens.HarnessScreen
import com.ogapp.subai.ui.screens.HomeScreen
import com.ogapp.subai.ui.screens.ImpedanceScreen
import com.ogapp.subai.ui.screens.LocScreen
import com.ogapp.subai.ui.screens.PowerScreen
import com.ogapp.subai.ui.screens.SettingsScreen
import com.ogapp.subai.ui.screens.SplashScreen
import com.ogapp.subai.ui.screens.TroubleshootScreen

object Routes {
    const val SPLASH = "splash"
    const val HOME = "home"
    const val IMPEDANCE = "impedance"
    const val POWER = "power"
    const val TROUBLESHOOT = "troubleshoot"
    const val HARNESS = "harness"
    const val LOC = "loc"
    const val CHARGING = "charging"
    const val SETTINGS = "settings"
}

@Composable
fun SubAiApp() {
    val nav = rememberNavController()

    NavHost(
        navController = nav,
        startDestination = Routes.SPLASH,
        enterTransition = { slideInHorizontally(tween(260)) { it / 4 } + fadeIn(tween(260)) },
        exitTransition = { fadeOut(tween(180)) },
        popEnterTransition = { fadeIn(tween(200)) },
        popExitTransition = { slideOutHorizontally(tween(220)) { it / 4 } + fadeOut(tween(220)) }
    ) {
        composable(Routes.SPLASH) {
            SplashScreen(onDone = {
                nav.navigate(Routes.HOME) {
                    popUpTo(Routes.SPLASH) { inclusive = true }
                }
            })
        }
        composable(Routes.HOME) {
            HomeScreen(onNavigate = { route -> nav.navigate(route) })
        }
        composable(Routes.IMPEDANCE) { ImpedanceScreen(onBack = { nav.popBackStack() }) }
        composable(Routes.POWER) { PowerScreen(onBack = { nav.popBackStack() }) }
        composable(Routes.TROUBLESHOOT) { TroubleshootScreen(onBack = { nav.popBackStack() }) }
        composable(Routes.HARNESS) { HarnessScreen(onBack = { nav.popBackStack() }) }
        composable(Routes.LOC) { LocScreen(onBack = { nav.popBackStack() }) }
        composable(Routes.CHARGING) { ChargingScreen(onBack = { nav.popBackStack() }) }
        composable(Routes.SETTINGS) { SettingsScreen(onBack = { nav.popBackStack() }) }
    }
}
