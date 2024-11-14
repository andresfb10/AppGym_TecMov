package com.example.fittrack.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.fittrack.data.Routine
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import androidx.compose.material3.CardDefaults
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.ui.graphics.Color


@OptIn(ExperimentalMaterial3Api::class) // Utiliza una API experimental de Jetpack Compose para Material3
@Composable
fun HomeScreen(
    navController: NavController, // Navegación entre pantallas
    onNavigateToProfile: () -> Unit, // Acción para navegar al perfil
    onNavigateToTrackWorkout: (String) -> Unit // Acción para navegar al seguimiento de entrenamiento
) {
    val scope = rememberCoroutineScope() // Crea un alcance de corrutina para manejar tareas asincrónicas
    val auth = FirebaseAuth.getInstance() // Instancia de Firebase Authentication
    val db = FirebaseFirestore.getInstance() // Instancia de Firestore para acceder a la base de datos

    // Estado para manejar la lista de rutinas, si está cargando, si está eliminando y si hay errores
    var routines by remember { mutableStateOf<List<Routine>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var isDeleting by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    // Colores personalizados (para simular el esquema de colores de Spotify)
    val spotifyGreen = Color(0xFF1DB954)
    val spotifyBlack = Color(0xFF191414)
    val darkGray = Color(0xFF282828)

    // Este efecto se ejecuta al iniciarse la pantalla (LaunchedEffect con Unit significa que se ejecutará una sola vez)
    LaunchedEffect(Unit) {
        isLoading = true
        try {
            val userId = auth.currentUser?.uid // Obtiene el ID del usuario actual desde Firebase Authentication
            if (userId != null) {
                // Realiza una consulta en Firestore para obtener las rutinas del usuario
                db.collection("routines")
                    .whereEqualTo("userId", userId) // Filtra las rutinas por el ID del usuario
                    .addSnapshotListener { snapshot, e -> // Escucha cambios en la base de datos
                        if (e != null) {
                            error = e.message // Si hay error, lo muestra
                            return@addSnapshotListener
                        }

                        // Mapea los documentos de la base de datos a objetos de tipo Routine
                        routines = snapshot?.documents?.mapNotNull { doc ->
                            doc.toObject(Routine::class.java)
                        } ?: emptyList()

                        isLoading = false // Finaliza el estado de carga
                    }
            }
        } catch (e: Exception) {
            error = e.message // Si ocurre una excepción, muestra el mensaje de error
            isLoading = false
        }
    }

    // Layout principal de la pantalla
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(spotifyBlack) // Fondo de la pantalla con color negro de Spotify
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent, // El fondo de la caja del Scaffold es transparente
            topBar = { // Barra superior
                TopAppBar(
                    title = { Text("My Routines", color = Color.White) },
                    actions = { // Botón para ir al perfil
                        IconButton(
                            onClick = { navController.navigate("profile") }
                        ) {
                            Icon(Icons.Default.Person, "Profile", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = spotifyBlack, // Color del fondo de la barra
                        titleContentColor = Color.White, // Color del título
                        actionIconContentColor = Color.White // Color de los íconos de acción
                    )
                )
            },
            floatingActionButton = { // Botón flotante para crear una nueva rutina
                FloatingActionButton(
                    onClick = { navController.navigate("create_routine") },
                    modifier = Modifier
                        .size(56.dp)
                        .background(spotifyGreen, CircleShape),
                    elevation = FloatingActionButtonDefaults.elevation(8.dp)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Create Routine",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        ) { padding ->
            // Contenedor principal de la pantalla
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Campo de búsqueda
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it }, // Cambia el valor de la búsqueda
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    placeholder = { Text("Search routines", color = Color.Gray) },
                    leadingIcon = { Icon(Icons.Default.Search, "Search", tint = Color.Gray) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = spotifyGreen, // Color del borde cuando está enfocado
                        unfocusedBorderColor = Color.Gray, // Color del borde cuando no está enfocado
                        focusedTextColor = Color.White, // Color del texto cuando está enfocado
                        unfocusedTextColor = Color.Gray, // Color del texto cuando no está enfocado
                        cursorColor = spotifyGreen, // Color del cursor
                        focusedContainerColor = darkGray, // Color del fondo cuando está enfocado
                        unfocusedContainerColor = darkGray // Color del fondo cuando no está enfocado
                    )
                )

                // Muestra un indicador de carga si está cargando las rutinas
                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = spotifyGreen)
                    }
                } else if (routines.isEmpty()) {
                    // Si no tiene rutinas, muestra un mensaje informativo
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("You don't have any routines yet",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White)
                    }
                } else {
                    // Si ya tiene rutinas, muestra la lista de rutinas
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(
                            routines.filter {
                                it.name.contains(searchQuery, ignoreCase = true) // Filtra las rutinas por la búsqueda
                            }
                        ) { routine ->
                            // Muestra cada rutina en una tarjeta
                            RoutineCard(
                                routine = routine,
                                isDeleting = isDeleting,
                                onDelete = {
                                    isDeleting = true
                                    scope.launch { // Inicia una corrutina para eliminar la rutina
                                        try {
                                            db.collection("routines")
                                                .document(routine.id)
                                                .delete() // Elimina la rutina de la base de datos
                                            isDeleting = false
                                        } catch (e: Exception) {
                                            error = e.message // Si hay error, muestra el mensaje
                                            isDeleting = false
                                        }
                                    }
                                },
                                onClick = {
                                    navController.navigate("track_workout/${routine.id}") // Navega a la pantalla de seguimiento del entrenamiento
                                },
                                onViewDetails = {
                                    navController.navigate("view_routine/${routine.id}") // Navega a la pantalla de detalles de la rutina
                                }
                            )
                        }
                    }
                }

                // Si ocurre un error, muestra un Snackbar con el mensaje
                if (error != null) {
                    Snackbar(
                        modifier = Modifier.padding(16.dp),
                        content = { Text(error ?: "Unknown error", color = Color.White) },
                        action = {
                            TextButton(
                                onClick = { error = null },
                                colors = ButtonDefaults.textButtonColors(contentColor = spotifyGreen)
                            ) {
                                Text("Close")
                            }
                        },
                        containerColor = darkGray
                    )
                }
            }
        }
    }
}

// Función composable para mostrar cada tarjeta de rutina
@Composable
private fun RoutineCard(
    routine: Routine,
    isDeleting: Boolean,
    onDelete: () -> Unit,
    onClick: () -> Unit,
    onViewDetails: () -> Unit
) {
    val spotifyGreen = Color(0xFF1DB954)
    val darkGray = Color(0xFF282828)

    // Card que contiene la información de la rutina
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(4.dp),
        elevation = CardDefaults.cardElevation(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = darkGray, // Fondo oscuro de la tarjeta
            contentColor = Color.White // Color del texto dentro de la tarjeta
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = routine.name, // Nombre de la rutina
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White
                    )
                    Text(
                        text = "Training days: ${routine.daysOfWeek.size}", // Muestra los días de entrenamiento
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                    Text(
                        text = "Total exercises: ${routine.exercisesByDay.values.sumBy { it.size }}", // Número total de ejercicios
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }

                // Icono de borrar si está cargando el estado de eliminación
                if (isDeleting) {
                    CircularProgressIndicator(color = spotifyGreen, modifier = Modifier.size(24.dp))
                } else {
                    IconButton(
                        onClick = { onDelete() },
                        modifier = Modifier
                            .size(36.dp)
                            .background(spotifyGreen, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Routine",
                            tint = Color.White
                        )
                    }
                }
            }

            // Ver detalles
            TextButton(
                onClick = { onViewDetails() },
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text("View Details", color = spotifyGreen)
            }
        }
    }
}




