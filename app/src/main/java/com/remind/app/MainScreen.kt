package com.remind.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.remind.app.ui.components.BottomNavigationBar
import com.remind.app.ui.navigation.MainNavGraph

@Composable
fun MainScreen() {
    val navController = rememberNavController()

    Scaffold(
        modifier       = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background, // Cream in light, SurfaceDark in dark
        bottomBar      = { BottomNavigationBar(navController) }
    ) { paddingValues ->
        MainNavGraph(
            navController = navController,
            paddingValues = paddingValues
        )
    }
}