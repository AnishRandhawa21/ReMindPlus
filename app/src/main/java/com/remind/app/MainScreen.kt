package com.remind.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.remind.app.ui.components.BottomNavigationBar
import com.remind.app.ui.navigation.MainNavGraph
import com.remind.app.ui.navigation.Routes

// Routes where the bottom bar should be hidden
private val bottomBarHiddenRoutes = setOf(
    Routes.NOTE_EDITOR,
    Routes.NOTE_EDITOR_WITH_ID
)

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Hide bottom bar on editor screens
    val showBottomBar = currentRoute !in bottomBarHiddenRoutes

    Scaffold(
        modifier       = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar      = {
            if (showBottomBar) {
                BottomNavigationBar(navController)
            }
        }
    ) { paddingValues ->
        MainNavGraph(
            navController = navController,
            paddingValues = paddingValues,
            showBottomBar = showBottomBar
        )
    }
}