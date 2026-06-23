package com.anish.remindplus

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.anish.remindplus.ui.components.BottomNavigationBar
import com.anish.remindplus.ui.navigation.MainNavGraph
import com.anish.remindplus.ui.navigation.Routes
import com.anish.remindplus.ui.navigation.swipeToNavigate
import com.anish.remindplus.utils.PreferenceManager

// Routes where the bottom bar should be hidden
private val bottomBarHiddenRoutes = setOf(
    Routes.NOTE_EDITOR,
    Routes.NOTE_EDITOR_WITH_ID
)

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun MainScreen() {
    val context = LocalContext.current
    val preferenceManager = remember { PreferenceManager.getInstance(context) }
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Hide bottom bar on editor screens
    val showBottomBar = currentRoute !in bottomBarHiddenRoutes

    val notificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ ->
        preferenceManager.hasAskedNotificationPermission = true
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!hasPermission && !preferenceManager.hasAskedNotificationPermission) {
                notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    SharedTransitionLayout {
        Scaffold(
            modifier       = Modifier
                .fillMaxSize()
                .swipeToNavigate(navController),
            containerColor = MaterialTheme.colorScheme.background,
            contentWindowInsets = WindowInsets(0, 0, 0, 0), // Disable auto insets to prevent jitter
            bottomBar      = {
                AnimatedVisibility(
                    visible = showBottomBar,
                    enter = slideInVertically(tween(400)) { it } + fadeIn(),
                    exit = slideOutVertically(tween(400)) { it } + fadeOut(),
                ) {
                    BottomNavigationBar(navController)
                }
            }
        ) { paddingValues ->
            // Pass the padding values but screens can decide how to use them
            MainNavGraph(
                navController = navController,
                paddingValues = paddingValues,
                showBottomBar = showBottomBar,
                sharedTransitionScope = this@SharedTransitionLayout
            )
        }
    }
}
