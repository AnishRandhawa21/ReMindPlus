package com.remind.app

import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.remind.app.ui.components.BottomNavigationBar
import com.remind.app.ui.navigation.MainNavGraph

@Composable
fun MainScreen() {

    val navController = rememberNavController()

    Scaffold(

        bottomBar = {
            BottomNavigationBar(navController)
        }

    ) { paddingValues ->

        MainNavGraph(
            navController = navController,
            paddingValues = paddingValues
        )
    }
}