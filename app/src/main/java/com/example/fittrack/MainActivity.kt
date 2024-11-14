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
        "home"  // Si el usuario ya está logueado, iniciar en "home"
    } else {
        "login"  // Si no, iniciar en "login"
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
                        popUpTo("login") { inclusive = true }  // Elimina la pantalla de login
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
                        popUpTo("login") { inclusive = true }  // Elimina la pantalla de login
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
                        launchSingleTop = true  // Evita duplicar la navegación a profile si ya estás allí
                    }
                },
                onNavigateToTrackWorkout = { routineId ->
                    navController.navigate("track_workout/$routineId") // Navegar a TrackWorkoutScreen con el routineId
                }
            )
        }

        // Pantalla de perfil
        composable("profile") {
            ProfileScreen(
                navController = navController,
                onNavigateBack = {
                    navController.navigateUp()  // Vuelve a la pantalla anterior
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

        // Pantalla para realizar el seguimiento del entrenamiento
        composable(
            route = "track_workout/{routineId}",
            arguments = listOf(navArgument("routineId") { type = NavType.StringType })
        ) { backStackEntry ->
            val routineId = backStackEntry.arguments?.getString("routineId")  // Obtiene el routineId
            routineId?.let {
                TrackWorkoutScreen(
                    routineId = it,  // Pasa el routineId a TrackWorkoutScreen
                    navController = navController
                )
            }
        }
    }
}
