package com.example.fittrack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import com.example.fittrack.ui.theme.FitTrackTheme
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.fittrack.authscreens.LoginScreen
import com.example.fittrack.authscreens.RegisterScreen
import com.example.fittrack.screens.CreateRoutineScreen
import com.example.fittrack.screens.HomeScreen
import com.example.fittrack.screens.ProfileScreen
import com.example.fittrack.screens.TrackWorkoutScreen
import com.example.fittrack.screens.ViewRoutineScreen
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FitTrackTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val startDestination = if (FirebaseAuth.getInstance().currentUser != null) {
        "home"
    } else {
        "login"
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Pantalla de Login
        composable("login") {
            LoginScreen(
                onNavigateToRegister = {
                    navController.navigate("register")
                },
                onLoginSuccess = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                navController = navController
            )
        }

        // Pantalla de Registro
        composable("register") {
            RegisterScreen(
                onNavigateBack = {
                    navController.navigateUp()
                },
                onRegisterSuccess = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                navController = navController
            )
        }

        // Pantalla principal - Home
        composable("home") {
            HomeScreen(
                navController = navController,
                onNavigateToProfile = {
                    navController.navigate("profile") {
                        launchSingleTop = true
                    }
                },
                onNavigateToTrackWorkout = { routineId ->
                    navController.navigate("track_workout/$routineId")
                }
            )
        }

        // Pantalla de perfil
        composable("profile") {
            ProfileScreen(
                navController = navController,
                onNavigateBack = {
                    navController.navigateUp()
                },
                onSignOut = {
                    FirebaseAuth.getInstance().signOut()
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // Pantalla de creación de rutina
        composable("create_routine") {
            CreateRoutineScreen(navController = navController)
        }

        // Nueva ruta - Visualización de rutina
        composable(
            route = "view_routine/{routineId}",
            arguments = listOf(
                navArgument("routineId") {
                    type = NavType.StringType
                    nullable = false
                }
            )
        ) { backStackEntry ->
            val routineId = backStackEntry.arguments?.getString("routineId")
                ?: return@composable
            ViewRoutineScreen(
                navController = navController,
                routineId = routineId
            )
        }

        // Pantalla de seguimiento de entrenamiento
        composable(
            route = "track_workout/{routineId}",
            arguments = listOf(
                navArgument("routineId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val routineId = backStackEntry.arguments?.getString("routineId") ?: ""
            TrackWorkoutScreen(
                routineId = routineId,
                navController = navController
            )
        }
    }
}