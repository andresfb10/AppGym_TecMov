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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController, // Navegador para controlar la navegación
    onNavigateBack: () -> Unit, // Función para navegar hacia atrás
    onSignOut: () -> Unit // Función para cerrar sesión
) {
    val scope = rememberCoroutineScope() // Corutinas para manejar operaciones asíncronas
    val auth = FirebaseAuth.getInstance() // Instancia de FirebaseAuth para la autenticación
    val db = FirebaseFirestore.getInstance() // Instancia de FirebaseFirestore para acceder a la base de datos

    var user by remember { mutableStateOf<User?>(null) } // Estado para almacenar los datos del usuario
    var isLoading by remember { mutableStateOf(false) } // Estado para controlar la carga de datos
    var error by remember { mutableStateOf<String?>(null) } // Estado para almacenar errores
    var isEditing by remember { mutableStateOf(false) } // Estado para saber si estamos en modo de edición

    // Variables para almacenar los datos editados por el usuario
    var editedName by remember { mutableStateOf("") }
    var editedAge by remember { mutableStateOf("") }
    var editedBirthday by remember { mutableStateOf("") }

    // Definición de colores personalizados (similares a los de Spotify)
    val spotifyGreen = Color(0xFF1DB954)
    val spotifyBlack = Color(0xFF191414)
    val darkGray = Color(0xFF282828)

    // Cargar los datos del usuario al inicio
    LaunchedEffect(Unit) {
        isLoading = true // Comienza la carga de datos
        try {
            val userId = auth.currentUser?.uid // Obtiene el ID del usuario autenticado
            if (userId != null) {
                db.collection("users").document(userId).get() // Recupera los datos del usuario desde Firestore
                    .addOnSuccessListener { document ->
                        user = document.toObject(User::class.java) // Asigna los datos a la variable user
                        user?.let {
                            // Rellena los campos de edición con los valores actuales del usuario
                            editedName = it.name
                            editedAge = it.age.toString()
                            editedBirthday = it.birthDate.toString()
                        }
                    }
                    .addOnFailureListener {
                        error = it.message // Si ocurre un error, muestra el mensaje
                    }
            }
        } catch (e: Exception) {
            error = e.message // Captura cualquier excepción y la muestra
        } finally {
            isLoading = false // Termina el proceso de carga
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize() // Hace que el Box ocupe toda la pantalla
            .background(spotifyBlack) // Aplica el color de fondo negro
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent, // Fondo transparente
            topBar = {
                TopAppBar(
                    title = { Text("Profile", color = Color.White) }, // Título de la barra superior
                    actions = {
                        // Botón para alternar el modo de edición
                        IconButton(onClick = { isEditing = !isEditing }) {
                            Icon(Icons.Default.Edit, "Edit profile", tint = Color.White)
                        }
                        // Botón para cerrar sesión
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
                    .padding(padding) // Aplica los márgenes de la barra superior
                    .padding(16.dp), // Espaciado adicional
                horizontalAlignment = Alignment.CenterHorizontally, // Alineación horizontal centrada
                verticalArrangement = Arrangement.spacedBy(16.dp) // Espaciado vertical
            ) {
                if (isLoading) {
                    // Si estamos cargando datos, mostrar un indicador de progreso
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = spotifyGreen)
                    }
                } else {
                    // Si estamos en modo de edición, mostramos los campos editables
                    if (isEditing) {
                        OutlinedTextField(
                            value = editedName,
                            onValueChange = { editedName = it }, // Actualiza el nombre editado
                            label = { Text("Name", color = Color.Gray) }, // Etiqueta del campo
                            modifier = Modifier.fillMaxWidth(), // Hace que el campo ocupe todo el ancho
                            isError = editedName.isBlank(), // Muestra error si el campo está vacío
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = spotifyGreen, // Color de borde cuando el campo está enfocado
                                unfocusedBorderColor = Color.Gray, // Color de borde cuando no está enfocado
                                focusedTextColor = Color.White, // Color del texto cuando el campo está enfocado
                                unfocusedTextColor = Color.Gray, // Color del texto cuando no está enfocado
                                cursorColor = spotifyGreen, // Color del cursor
                                focusedContainerColor = darkGray, // Fondo cuando el campo está enfocado
                                unfocusedContainerColor = darkGray // Fondo cuando no está enfocado
                            )
                        )

                        // Campo para editar la edad
                        OutlinedTextField(
                            value = editedAge,
                            onValueChange = { editedAge = it },
                            label = { Text("Age", color = Color.Gray) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), // Solo números
                            modifier = Modifier.fillMaxWidth(),
                            isError = editedAge.isBlank() || editedAge.toIntOrNull() == null, // Valida que la edad sea válida
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

                        // Campo para editar la fecha de nacimiento
                        OutlinedTextField(
                            value = editedBirthday,
                            onValueChange = { editedBirthday = it },
                            label = { Text("Birthday (DD/MM/YYYY)", color = Color.Gray) },
                            modifier = Modifier.fillMaxWidth(),
                            isError = editedBirthday.isBlank(), // Muestra error si está vacío
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

                        // Botón para guardar los cambios
                        Button(
                            onClick = {
                                // Verificación de campos vacíos
                                scope.launch {
                                    if (editedName.isBlank() || editedAge.isBlank() || editedBirthday.isBlank()) {
                                        error = "All fields must be filled." // Error si hay campos vacíos
                                        return@launch
                                    }

                                    // Verificación de edad válida
                                    val age = editedAge.toIntOrNull()
                                    if (age == null || age <= 0) {
                                        error = "Please enter a valid age." // Error si la edad no es válida
                                        return@launch
                                    }

                                    try {
                                        val userId = auth.currentUser?.uid
                                        if (userId != null) {
                                            // Actualiza los datos del usuario en Firestore
                                            val updates = hashMapOf<String, Any>(
                                                "name" to editedName,
                                                "age" to age,
                                                "birthDate" to editedBirthday
                                            )

                                            db.collection("users").document(userId)
                                                .update(updates)
                                                .addOnSuccessListener {
                                                    isEditing = false // Sale del modo de edición
                                                    db.collection("users").document(userId).get()
                                                        .addOnSuccessListener { document ->
                                                            user = document.toObject(User::class.java) // Recarga los datos del usuario
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
                                        error = "Error updating data: ${e.message}" // Error si algo sale mal
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = spotifyGreen, // Color del fondo del botón
                                contentColor = Color.White // Color del texto del botón
                            )
                        ) {
                            Text("Save Changes") // Texto del botón
                        }
                    } else {
                        // Si no estamos en modo de edición, mostramos los datos del usuario
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
                                    // Muestra la información del usuario
                                    Text("Name", color = Color.Gray)
                                    Text(currentUser.name, fontWeight = FontWeight.Bold)

                                    Text("Age", color = Color.Gray)
                                    Text(currentUser.age.toString(), fontWeight = FontWeight.Bold)

                                    Text("Birthday", color = Color.Gray)
                                    Text(currentUser.birthDate.toString(), fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
                // Si hay un error, muestra el mensaje
                error?.let {
                    Text(
                        it, color = Color.Red, fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}


