package com.example.fittrack.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import com.example.fittrack.data.Routine
import com.example.fittrack.data.ExerciseConfig
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

// Para los íconos específicos
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Bed


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewRoutineScreen(
    navController: NavController, // Navegación para volver a la pantalla anterior
    routineId: String // ID de la rutina a mostrar
) {
    val scope = rememberCoroutineScope() // Scope para ejecutar coroutines
    val db = FirebaseFirestore.getInstance() // Instancia de Firebase Firestore

    var routine by remember { mutableStateOf<Routine?>(null) } // Estado para almacenar la rutina
    var isLoading by remember { mutableStateOf(true) } // Estado para mostrar el cargando
    var error by remember { mutableStateOf<String?>(null) } // Estado para manejar errores

    // LaunchedEffect se ejecuta cuando cambia el ID de la rutina
    LaunchedEffect(routineId) {
        scope.launch {
            try {
                // Carga la rutina desde Firebase Firestore usando el ID de la rutina
                db.collection("routines")
                    .document(routineId)
                    .get()
                    .addOnSuccessListener { document ->
                        // Si la carga es exitosa, convierte el documento en objeto de tipo Routine
                        routine = document.toObject(Routine::class.java)
                        isLoading = false // Termina el estado de carga
                    }
                    .addOnFailureListener {
                        // Si ocurre un error, guarda el mensaje de error y termina la carga
                        error = it.message
                        isLoading = false
                    }
            } catch (e: Exception) {
                // Si ocurre una excepción, guarda el mensaje de error
                error = e.message
                isLoading = false
            }
        }
    }

    // Scaffold es el contenedor principal de la pantalla
    Scaffold(
        topBar = {
            // TopAppBar muestra el título y los botones de acción
            TopAppBar(
                title = { Text(routine?.name ?: "Detalles de Rutina") }, // Muestra el nombre de la rutina o un mensaje por defecto
                navigationIcon = {
                    // Botón para volver a la pantalla anterior
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                },
                actions = {
                    // Botón para editar la rutina (aún no implementado)
                    IconButton(onClick = { /* Implementar edición */ }) {
                        Icon(Icons.Default.Edit, "Editar rutina")
                    }
                }
            )
        }
    ) { padding -> // Padding para la zona de contenido
        when {
            isLoading -> {
                // Si la rutina aún está cargando, muestra un indicador de carga
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator() // Indicador de carga
                }
            }

            routine != null -> {
                // Si la rutina está disponible, muestra la lista de detalles
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp)
                ) {
                    // Descripción de la rutina
                    item {
                        if (!routine!!.description.isNullOrBlank()) {
                            // Muestra la descripción de la rutina si no está vacía
                            Text(
                                text = "Descripción",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Text(
                                text = routine!!.description!!,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                            Divider(modifier = Modifier.padding(vertical = 8.dp)) // Separa la sección
                        }
                    }

                    // Días de entrenamiento
                    item {
                        Text(
                            text = "Días de Entrenamiento",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    // Muestra los días de entrenamiento en una lista
                    items(routine!!.daysOfWeek) { day ->
                        TrainingDayCard(
                            day = day,
                            dayName = routine!!.dayNames[day], // Nombre del día (Lunes, Martes, etc.)
                            exercises = routine!!.exercisesByDay[day] ?: emptyList() // Ejercicios asignados al día
                        )
                    }

                    // Días de descanso
                    item {
                        Text(
                            text = "Días de Descanso",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    // Muestra los días de descanso en una lista
                    items(routine!!.restDays) { day ->
                        RestDayCard(day = day)
                    }
                }
            }
        }
    }
}

@Composable
fun TrainingDayCard(
    day: String, // Día de la semana
    dayName: String?, // Nombre completo del día (si existe)
    exercises: List<ExerciseConfig> // Lista de ejercicios para el día
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            // Fila que muestra el día de la semana y su nombre
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = day,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                if (!dayName.isNullOrBlank()) {
                    Text(
                        text = dayName,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Si hay ejercicios asignados, se muestran en una lista
            if (exercises.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                exercises.forEach { exercise ->
                    ExerciseItem(exercise = exercise) // Muestra cada ejercicio en un ítem
                    Spacer(modifier = Modifier.height(4.dp)) // Espacio entre ejercicios
                }
            }
        }
    }
}

@Composable
fun ExerciseItem(exercise: ExerciseConfig) {
    // Fila que muestra el nombre del ejercicio y sus detalles (series, repeticiones, RIR)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = exercise.name,
            style = MaterialTheme.typography.bodyMedium
        )
        // Muestra el formato "sets x reps" y RIR si está presente
        Text(
            text = "${exercise.sets}x${exercise.repsPerSet}" +
                    if (exercise.rir > 0) " RIR:${exercise.rir}" else "",
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
fun RestDayCard(day: String) {
    // Tarjeta que muestra un día de descanso
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = day,
                style = MaterialTheme.typography.bodyLarge
            )
            // Icono de cama que indica descanso
            Icon(
                imageVector = Icons.Default.Bed,
                contentDescription = "Día de descanso"
            )
        }
    }
}
