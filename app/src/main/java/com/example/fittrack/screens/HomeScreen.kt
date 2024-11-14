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
import androidx.compose.ui.graphics.Color


@OptIn(ExperimentalMaterial3Api::class)  // Opt-in para usar API experimental de Material3
@Composable
fun HomeScreen(
    navController: NavController,  // Controlador de navegación para movernos entre pantallas
    onNavigateToProfile: () -> Unit,  // Función para navegar a la pantalla de perfil
    onNavigateToTrackWorkout: (String) -> Unit  // Función para navegar a la pantalla de seguimiento de entrenamientos
) {
    val scope = rememberCoroutineScope()  // Creación de un ámbito para lanzar corutinas
    val auth = FirebaseAuth.getInstance()  // Obtener instancia de FirebaseAuth
    val db = FirebaseFirestore.getInstance()  // Obtener instancia de FirebaseFirestore

    // Definir variables de estado para gestionar la UI
    var routines by remember { mutableStateOf<List<Routine>>(emptyList()) }  // Lista de rutinas
    var isLoading by remember { mutableStateOf(false) }  // Estado de carga
    var isDeleting by remember { mutableStateOf(false) }  // Estado para indicar si se está eliminando una rutina
    var error by remember { mutableStateOf<String?>(null) }  // Estado de error
    var searchQuery by remember { mutableStateOf("") }  // Consulta de búsqueda

    // Definir colores personalizados
    val spotifyGreen = Color(0xFF1DB954)  // Verde de Spotify
    val spotifyBlack = Color(0xFF191414)  // Negro de fondo de Spotify
    val darkGray = Color(0xFF282828)  // Gris oscuro para algunos fondos

    // Cargar las rutinas desde Firebase
    LaunchedEffect(Unit) {  // LaunchedEffect ejecuta el bloque una vez al inicio
        isLoading = true  // Iniciar carga
        try {
            val userId = auth.currentUser?.uid  // Obtener el ID del usuario actual
            if (userId != null) {
                // Realizar consulta en Firebase Firestore para obtener rutinas del usuario
                db.collection("routines")
                    .whereEqualTo("userId", userId)  // Filtrar rutinas por ID de usuario
                    .addSnapshotListener { snapshot, e ->  // Escuchar cambios en tiempo real
                        if (e != null) {
                            error = e.message  // Si hay un error, mostrar mensaje
                            return@addSnapshotListener
                        }

                        // Mapeo de los documentos obtenidos a objetos de tipo Routine
                        routines = snapshot?.documents?.mapNotNull { doc ->
                            doc.toObject(Routine::class.java)
                        } ?: emptyList()

                        isLoading = false  // Finalizar carga
                    }
            }
        } catch (e: Exception) {
            error = e.message  // Si ocurre un error en la consulta, mostrar mensaje
            isLoading = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()  // Asegura que el Box ocupe todo el espacio disponible
            .background(spotifyBlack)  // Fondo negro de Spotify
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,  // Fondo transparente
            topBar = {
                TopAppBar(
                    title = { Text("My Routines", color = Color.White) },  // Título de la barra superior
                    actions = {
                        IconButton(
                            onClick = { navController.navigate("profile") }  // Navegar a la pantalla de perfil
                        ) {
                            Icon(Icons.Default.Person, "Profile", tint = Color.White)  // Ícono de perfil
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = spotifyBlack,  // Fondo de la barra superior
                        titleContentColor = Color.White,  // Color del título
                        actionIconContentColor = Color.White  // Color de los íconos de acción
                    )
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { navController.navigate("create_routine") },  // Navegar a la pantalla de crear rutina
                    modifier = Modifier
                        .size(56.dp)  // Tamaño del botón flotante
                        .background(spotifyGreen, CircleShape),  // Fondo verde en forma de círculo
                    elevation = FloatingActionButtonDefaults.elevation(8.dp)  // Elevación del botón
                ) {
                    Icon(
                        Icons.Default.Add,  // Ícono de agregar
                        contentDescription = "Create Routine",
                        tint = Color.White,  // Color blanco para el ícono
                        modifier = Modifier.size(24.dp)  // Tamaño del ícono
                    )
                }
            }
        ) { padding ->  // Content padding para la pantalla principal
            Column(
                modifier = Modifier
                    .fillMaxSize()  // Asegura que el Column ocupe todo el espacio disponible
                    .padding(padding)  // Agregar el padding proporcionado por Scaffold
            ) {
                // Campo de búsqueda para filtrar rutinas
                OutlinedTextField(
                    value = searchQuery,  // Valor del campo de búsqueda
                    onValueChange = { searchQuery = it },  // Actualizar valor de búsqueda
                    modifier = Modifier
                        .fillMaxWidth()  // Asegura que el campo de búsqueda ocupe todo el ancho
                        .padding(16.dp),
                    placeholder = { Text("Search routines", color = Color.Gray) },  // Placeholder
                    leadingIcon = { Icon(Icons.Default.Search, "Search", tint = Color.Gray) },  // Ícono de búsqueda
                    singleLine = true,  // Campo de una sola línea
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = spotifyGreen,  // Color del borde cuando está enfocado
                        unfocusedBorderColor = Color.Gray,  // Color del borde cuando no está enfocado
                        focusedTextColor = Color.White,  // Color del texto cuando está enfocado
                        unfocusedTextColor = Color.Gray,  // Color del texto cuando no está enfocado
                        cursorColor = spotifyGreen,  // Color del cursor
                        focusedContainerColor = darkGray,  // Color del fondo cuando está enfocado
                        unfocusedContainerColor = darkGray  // Color del fondo cuando no está enfocado
                    )
                )

                // Estado de carga global
                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center  // Centramos el indicador de carga
                    ) {
                        CircularProgressIndicator(color = spotifyGreen)  // Indicador de carga
                    }
                } else if (routines.isEmpty()) {
                    // Si no hay rutinas, mostrar mensaje
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("You don't have any routines yet", style = MaterialTheme.typography.bodyLarge, color = Color.White)
                    }
                } else {
                    // Si hay rutinas, mostrarlas en una lista
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(
                            routines.filter {
                                it.name.contains(searchQuery, ignoreCase = true)  // Filtrar por nombre según la búsqueda
                            }
                        ) { routine ->  // Para cada rutina, mostrar una tarjeta
                            RoutineCard(
                                routine = routine,
                                isDeleting = isDeleting,
                                onDelete = {
                                    isDeleting = true  // Cambiar estado a eliminando
                                    scope.launch {
                                        try {
                                            // Eliminar rutina de Firebase
                                            db.collection("routines")
                                                .document(routine.id)
                                                .delete()
                                            isDeleting = false  // Finalizar eliminación
                                        } catch (e: Exception) {
                                            error = e.message  // Si hay error, mostrar mensaje
                                            isDeleting = false
                                        }
                                    }
                                },
                                onClick = {
                                    // Navegar a la pantalla de seguimiento de entrenamiento pasando el ID de la rutina
                                    navController.navigate("track_workout/${routine.id}")
                                }
                            )
                        }
                    }
                }

                // Mostrar mensajes de error si existen
                if (error != null) {
                    Snackbar(
                        modifier = Modifier.padding(16.dp),
                        content = { Text(error ?: "Unknown error", color = Color.White) },
                        action = {
                            TextButton(
                                onClick = { error = null },  // Cerrar el mensaje de error
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

// Componente para mostrar una tarjeta de rutina
@Composable
private fun RoutineCard(
    routine: Routine,
    isDeleting: Boolean,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    val spotifyGreen = Color(0xFF1DB954)
    val darkGray = Color(0xFF282828)

    // Tarjeta que muestra la información de una rutina
    Card(
        modifier = Modifier
            .fillMaxWidth()  // Ocupa todo el ancho disponible
            .clickable(onClick = onClick)  // Al hacer clic, ejecutar onClick
            .padding(4.dp),  // Añadir padding
        elevation = CardDefaults.cardElevation(8.dp),  // Elevación de la tarjeta
        colors = CardDefaults.cardColors(
            containerColor = darkGray,  // Fondo de la tarjeta
            contentColor = Color.White  // Color del texto
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)  // Padding dentro de la tarjeta
        ) {
            // Fila que contiene el nombre de la rutina y la acción de eliminar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)  // El nombre y detalles de la rutina ocuparán el espacio restante
                ) {
                    Text(
                        text = routine.name,
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White
                    )
                    Text(
                        text = "Training days: ${routine.daysOfWeek.size}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                    Text(
                        text = "Total exercises: ${routine.exercisesByDay.values.flatten().size}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                    if (routine.description.isNotEmpty()) {
                        Text(
                            text = routine.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }

                // Ícono de eliminar
                IconButton(
                    onClick = onDelete,  // Al hacer clic, ejecutar onDelete
                    enabled = !isDeleting  // Deshabilitar si se está eliminando
                ) {
                    if (isDeleting) {
                        CircularProgressIndicator(  // Mostrar indicador de carga mientras se elimina
                            modifier = Modifier.size(24.dp),
                            color = spotifyGreen
                        )
                    } else {
                        Icon(Icons.Default.Delete, "Delete routine", tint = spotifyGreen)  // Mostrar ícono de eliminar
                    }
                }
            }
        }
    }
}




