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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    onNavigateToProfile: () -> Unit,
    onNavigateToTrackWorkout: (String) -> Unit
) {
    val scope = rememberCoroutineScope()
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    var routines by remember { mutableStateOf<List<Routine>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var isDeleting by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    // Define Spotify colors
    val spotifyGreen = Color(0xFF1DB954)
    val spotifyBlack = Color(0xFF191414)
    val darkGray = Color(0xFF282828)

    // Load routines
    LaunchedEffect(Unit) {
        isLoading = true
        try {
            val userId = auth.currentUser?.uid
            if (userId != null) {
                db.collection("routines")
                    .whereEqualTo("userId", userId)
                    .addSnapshotListener { snapshot, e ->
                        if (e != null) {
                            error = e.message
                            return@addSnapshotListener
                        }

                        routines = snapshot?.documents?.mapNotNull { doc ->
                            doc.toObject(Routine::class.java)
                        } ?: emptyList()

                        isLoading = false
                    }
            }
        } catch (e: Exception) {
            error = e.message
            isLoading = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(spotifyBlack)
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("My Routines", color = Color.White) },
                    actions = {
                        IconButton(
                            onClick = { navController.navigate("profile") }
                        ) {
                            Icon(Icons.Default.Person, "Profile", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = spotifyBlack,
                        titleContentColor = Color.White,
                        actionIconContentColor = Color.White
                    )
                )
            },
            floatingActionButton = {
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    placeholder = { Text("Search routines", color = Color.Gray) },
                    leadingIcon = { Icon(Icons.Default.Search, "Search", tint = Color.Gray) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = spotifyGreen,
                        unfocusedBorderColor = Color.Gray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.Gray,
                        cursorColor = spotifyGreen,
                        focusedContainerColor = darkGray,
                        unfocusedContainerColor = darkGray
                    )
                )

                // Global loading state
                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = spotifyGreen)
                    }
                } else if (routines.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("You don't have any routines yet", style = MaterialTheme.typography.bodyLarge, color = Color.White)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(
                            routines.filter {
                                it.name.contains(searchQuery, ignoreCase = true)
                            }
                        ) { routine ->
                            RoutineCard(
                                routine = routine,
                                isDeleting = isDeleting,
                                onDelete = {
                                    isDeleting = true
                                    scope.launch {
                                        try {
                                            db.collection("routines")
                                                .document(routine.id)
                                                .delete()
                                            isDeleting = false
                                        } catch (e: Exception) {
                                            error = e.message
                                            isDeleting = false
                                        }
                                    }
                                },
                                onClick = {
                                    // Navegar a TrackWorkoutScreen pasando el ID de la rutina
                                    navController.navigate("track_workout/${routine.id}")  // Aquí cambiamos la ruta
                                }
                            )
                        }
                    }
                }

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


@Composable
private fun RoutineCard(
    routine: Routine,
    isDeleting: Boolean,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    val spotifyGreen = Color(0xFF1DB954)
    val darkGray = Color(0xFF282828)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(4.dp),
        elevation = CardDefaults.cardElevation(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = darkGray,
            contentColor = Color.White
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

                IconButton(
                    onClick = onDelete,
                    enabled = !isDeleting
                ) {
                    if (isDeleting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = spotifyGreen
                        )
                    } else {
                        Icon(Icons.Default.Delete, "Delete routine", tint = spotifyGreen)
                    }
                }
            }
        }
    }
}




