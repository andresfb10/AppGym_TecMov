package com.example.fittrack.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.fittrack.data.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import androidx.compose.material3.CardDefaults
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    onNavigateBack: () -> Unit,
    onSignOut: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    var user by remember { mutableStateOf<User?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var isEditing by remember { mutableStateOf(false) }

    var editedName by remember { mutableStateOf("") }
    var editedAge by remember { mutableStateOf("") }
    var editedBirthday by remember { mutableStateOf("") }

    // Define Spotify colors
    val spotifyGreen = Color(0xFF1DB954)
    val spotifyBlack = Color(0xFF191414)
    val darkGray = Color(0xFF282828)

    // Load user data
    LaunchedEffect(Unit) {
        isLoading = true
        try {
            val userId = auth.currentUser?.uid
            if (userId != null) {
                db.collection("users").document(userId).get()
                    .addOnSuccessListener { document ->
                        user = document.toObject(User::class.java)
                        user?.let {
                            editedName = it.name
                            editedAge = it.age.toString()
                            editedBirthday = it.birthDate.toString()
                        }
                    }
                    .addOnFailureListener {
                        error = it.message
                    }
            }
        } catch (e: Exception) {
            error = e.message
        } finally {
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
                    title = { Text("Profile", color = Color.White) },
                    actions = {
                        IconButton(onClick = { isEditing = !isEditing }) {
                            Icon(Icons.Default.Edit, "Edit profile", tint = Color.White)
                        }
                        IconButton(onClick = onSignOut) {
                            Icon(Icons.Default.ExitToApp, "Sign out", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = spotifyBlack,
                        titleContentColor = Color.White,
                        actionIconContentColor = Color.White
                    )
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = spotifyGreen)
                    }
                } else {
                    if (isEditing) {
                        // Edit mode
                        OutlinedTextField(
                            value = editedName,
                            onValueChange = { editedName = it },
                            label = { Text("Name", color = Color.Gray) },
                            modifier = Modifier.fillMaxWidth(),
                            isError = editedName.isBlank(),
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

                        OutlinedTextField(
                            value = editedAge,
                            onValueChange = { editedAge = it },
                            label = { Text("Age", color = Color.Gray) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            isError = editedAge.isBlank() || editedAge.toIntOrNull() == null,
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

                        OutlinedTextField(
                            value = editedBirthday,
                            onValueChange = { editedBirthday = it },
                            label = { Text("Birthday (DD/MM/YYYY)", color = Color.Gray) },
                            modifier = Modifier.fillMaxWidth(),
                            isError = editedBirthday.isBlank(),
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

                        Button(
                            onClick = {
                                scope.launch {
                                    if (editedName.isBlank() || editedAge.isBlank() || editedBirthday.isBlank()) {
                                        error = "All fields must be filled."
                                        return@launch
                                    }

                                    val age = editedAge.toIntOrNull()
                                    if (age == null || age <= 0) {
                                        error = "Please enter a valid age."
                                        return@launch
                                    }

                                    try {
                                        val userId = auth.currentUser?.uid
                                        if (userId != null) {
                                            val updates = hashMapOf<String, Any>(
                                                "name" to editedName,
                                                "age" to age,
                                                "birthDate" to editedBirthday
                                            )

                                            db.collection("users").document(userId)
                                                .update(updates)
                                                .addOnSuccessListener {
                                                    isEditing = false
                                                    db.collection("users").document(userId).get()
                                                        .addOnSuccessListener { document ->
                                                            user = document.toObject(User::class.java)
                                                        }
                                                        .addOnFailureListener { e ->
                                                            error = "Error reloading data: ${e.message}"
                                                        }
                                                }
                                                .addOnFailureListener { e ->
                                                    error = "Error saving changes: ${e.message}"
                                                }
                                        }
                                    } catch (e: Exception) {
                                        error = "Error updating data: ${e.message}"
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = spotifyGreen,
                                contentColor = Color.White
                            )
                        ) {
                            Text("Save Changes")
                        }
                    } else {
                        user?.let { currentUser ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = darkGray,
                                    contentColor = Color.White
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Text("Name", color = Color.Gray)
                                    Text(currentUser.name, style = MaterialTheme.typography.bodyLarge)
                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text("Email", color = Color.Gray)
                                    Text(currentUser.email, style = MaterialTheme.typography.bodyLarge)
                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text("Age", color = Color.Gray)
                                    Text("${currentUser.age} years", style = MaterialTheme.typography.bodyLarge)
                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text("Birthday", color = Color.Gray)
                                    Text(currentUser.birthDate.toString(), style = MaterialTheme.typography.bodyLarge)
                                }
                            }
                        }
                    }
                }

                if (error != null) {
                    Snackbar(
                        modifier = Modifier.padding(16.dp),
                        containerColor = darkGray
                    ) {
                        Text(error ?: "Unknown error", color = Color.White)
                    }
                }
            }
        }
    }
}

