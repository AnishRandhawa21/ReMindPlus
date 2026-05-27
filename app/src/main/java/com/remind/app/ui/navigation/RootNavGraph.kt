package com.remind.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.remind.app.MainScreen
import com.remind.app.ui.screens.auth.LoginScreen
import com.remind.app.ui.screens.auth.LoginViewModel
import io.github.jan.supabase.auth.status.SessionStatus

@Composable
fun RootNavGraph() {
    val viewModel: LoginViewModel = viewModel()
    val sessionStatus by viewModel.sessionStatus.collectAsState()

    // While session is initializing, don't show any UI (Splash Screen is visible)
    if (sessionStatus is SessionStatus.Initializing) return

    val navController = rememberNavController()
    val startRoute = remember(sessionStatus) {
        when (sessionStatus) {
            is SessionStatus.Authenticated -> Routes.MAIN
            else -> Routes.LOGIN
        }
    }
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    NavHost(
        navController = navController,
        startDestination = startRoute
    ) {
        composable(Routes.LOGIN) {
            LoginScreen(
                isLoading    = isLoading,
                onLoginClick = { viewModel.signInWithGoogle() }
            )
        }

        composable(Routes.MAIN) {
            MainScreen()
        }
    }

    // Handle session changes AFTER the initial load (e.g. Logout or Login success)
    LaunchedEffect(sessionStatus) {
        val currentRoute = navController.currentDestination?.route ?: return@LaunchedEffect
        
        when (sessionStatus) {
            is SessionStatus.Authenticated -> {
                if (currentRoute == Routes.LOGIN) {
                    navController.navigate(Routes.MAIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
            is SessionStatus.NotAuthenticated -> {
                if (currentRoute == Routes.MAIN) {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
            else -> {}
        }
    }
}
