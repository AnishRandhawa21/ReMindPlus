package com.anish.remindplus.ui.navigation

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.anish.remindplus.MainScreen
import com.anish.remindplus.ui.screens.auth.LoginScreen
import com.anish.remindplus.ui.screens.auth.LoginViewModel
import io.github.jan.supabase.auth.status.SessionStatus

@Composable
fun RootNavGraph() {
    val viewModel: LoginViewModel = viewModel()
    val sessionStatus by viewModel.sessionStatus.collectAsStateWithLifecycle()

    // track if we've ever successfully loaded the session state
    var isAuthInitialized by rememberSaveable { mutableStateOf(false) }
    
    LaunchedEffect(sessionStatus) {
        if (sessionStatus !is SessionStatus.Initializing) {
            isAuthInitialized = true
        }
    }

    // Only show nothing (keep Splash) until the very first auth state is known.
    // After that, we keep the NavHost in composition even if status briefly 
    // changes to Initializing (e.g. during a refresh or background resume).
    if (!isAuthInitialized && sessionStatus is SessionStatus.Initializing) return

    val navController = rememberNavController()

    // Determine the start destination ONLY ONCE when the NavHost is first created.
    // This prevents the NavHost from resetting its backstack during status flickers.
    val startRoute = remember {
        if (sessionStatus is SessionStatus.Authenticated) Routes.MAIN else Routes.LOGIN
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
