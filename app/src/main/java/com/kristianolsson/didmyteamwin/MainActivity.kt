package com.kristianolsson.didmyteamwin

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.kristianolsson.didmyteamwin.notification.NotificationHelper
import com.kristianolsson.didmyteamwin.ui.ResultScreen
import com.kristianolsson.didmyteamwin.ui.TeamListScreen
import com.kristianolsson.didmyteamwin.ui.TeamListViewModel
import com.kristianolsson.didmyteamwin.ui.TeamSearchScreen
import com.kristianolsson.didmyteamwin.ui.TeamSearchViewModel
import com.kristianolsson.didmyteamwin.ui.theme.DidMyTeamWinTheme

class MainActivity : ComponentActivity() {

    private val teamListViewModel: TeamListViewModel by viewModels()
    private val teamSearchViewModel: TeamSearchViewModel by viewModels()

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { _ ->
            // Permission result handled — app continues regardless
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Create notification channel
        NotificationHelper.createChannel(this)

        // Request notification permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // Check if launched from a notification with a team result to show
        val navigateToResult = intent?.getStringExtra("navigate_to_result")

        setContent {
            DidMyTeamWinTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    val navController = rememberNavController()

                    NavHost(
                        navController = navController,
                        startDestination = if (navigateToResult != null) {
                            "result/$navigateToResult"
                        } else {
                            "teams"
                        },
                    ) {
                        composable("teams") {
                            TeamListScreen(
                                viewModel = teamListViewModel,
                                onAddTeam = {
                                    teamSearchViewModel.updateQuery("")
                                    navController.navigate("search")
                                },
                                onTeamClick = { teamId ->
                                    navController.navigate("result/$teamId")
                                },
                            )
                        }

                        composable("search") {
                            TeamSearchScreen(
                                viewModel = teamSearchViewModel,
                                onTeamSelected = { team ->
                                    teamListViewModel.addTeam(team)
                                    navController.popBackStack()
                                },
                                onBack = { navController.popBackStack() },
                            )
                        }

                        composable(
                            "result/{teamId}",
                            arguments = listOf(navArgument("teamId") { type = NavType.StringType }),
                        ) { backStackEntry ->
                            val teamId = backStackEntry.arguments?.getString("teamId") ?: return@composable
                            ResultScreen(
                                viewModel = teamListViewModel,
                                teamId = teamId,
                                onBack = {
                                    if (navController.previousBackStackEntry != null) {
                                        navController.popBackStack()
                                    } else {
                                        navController.navigate("teams") {
                                            popUpTo(0) { inclusive = true }
                                        }
                                    }
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}
