package com.example.fittrack.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.fittrack.data.*
import java.util.*
import com.example.fittrack.data.Exercise
import com.example.fittrack.data.ExerciseRecord
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import kotlinx.coroutines.launch
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxWidth
import android.util.Log
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackWorkoutScreen(
    routineId: String,
    navController: NavController
) {
    // Variables de estado para manejar la carga de datos, errores y el estado del diálogo
    var routine by remember { mutableStateOf<Routine?>(null) } // Rutina cargada desde Firebase
    var exercisesWithSets by remember { mutableStateOf<List<ExerciseWithSets>>(emptyList()) } // Lista de ejercicios con sets
    var isLoading by remember { mutableStateOf(true) } // Estado de carga
    var error by remember { mutableStateOf<String?>(null) } // Mensajes de error
    var showFinishDialog by remember { mutableStateOf(false) } // Estado para mostrar el diálogo de finalizar entrenamiento
    var workoutNotes by remember { mutableStateOf("") } // Notas del entrenamiento ingresadas por el usuario


    val scope = rememberCoroutineScope() // Coroutines para realizar operaciones en segundo plano
    val db = FirebaseFirestore.getInstance() // Instancia de Firestore para interactuar con la base de datos
    val auth = FirebaseAuth.getInstance() // Instancia de FirebaseAuth para obtener el ID del usuario actual
    // Función para obtener el día actual
    fun getCurrentDay(): String {
        val calendar = Calendar.getInstance()
        return when(calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> "Lunes"
            Calendar.TUESDAY -> "Martes"
            Calendar.WEDNESDAY -> "Miércoles"
            Calendar.THURSDAY -> "Jueves"
            Calendar.FRIDAY -> "Viernes"
            Calendar.SATURDAY -> "Sábado"
            Calendar.SUNDAY -> "Domingo"
            else -> ""
        }
    }

    LaunchedEffect(routineId) {
        try {
            Log.d("TrackWorkoutScreen", "Loading routine with ID: $routineId")

            db.collection("routines")
                .document(routineId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        routine = document.toObject(Routine::class.java)
                        Log.d("TrackWorkoutScreen", "Routine loaded: ${routine?.name}")
                        Log.d("TrackWorkoutScreen", "ExercisesByDay: ${routine?.exercisesByDay}")

                        val currentDay = getCurrentDay()
                        Log.d("TrackWorkoutScreen", "Current day: $currentDay")

                        routine?.exercisesByDay?.get(currentDay)?.let { exerciseConfigs ->
                            Log.d("TrackWorkoutScreen", "Found ${exerciseConfigs.size} exercises for $currentDay")

                            if (exerciseConfigs.isNotEmpty()) {
                                val exerciseIds = exerciseConfigs.map { it.exerciseId }

                                db.collection("exercises")
                                    .whereIn("id", exerciseIds)
                                    .get()
                                    .addOnSuccessListener { snapshot ->
                                        val loadedExercises = snapshot.documents.mapNotNull { doc ->
                                            doc.toObject(Exercise::class.java)?.let { exercise ->
                                                val config = exerciseConfigs.find { it.exerciseId == exercise.id }
                                                ExerciseWithSets(
                                                    exercise = exercise,
                                                    sets = List(config?.sets ?: 0) {
                                                        com.example.fittrack.data.Set(
                                                            reps = config?.repsPerSet ?: 0
                                                        )
                                                    }
                                                )
                                            }
                                        }
                                        exercisesWithSets = loadedExercises
                                        Log.d("TrackWorkoutScreen", "Loaded ${loadedExercises.size} exercises with sets")
                                        isLoading = false
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e("TrackWorkoutScreen", "Error loading exercises", e)
                                        error = "Error al cargar ejercicios: ${e.message}"
                                        isLoading = false
                                    }
                            } else {
                                Log.d("TrackWorkoutScreen", "No exercises found for $currentDay")
                                isLoading = false
                            }
                        } ?: run {
                            Log.d("TrackWorkoutScreen", "No exercise configuration found for $currentDay")
                            isLoading = false
                        }
                    } else {
                        Log.e("TrackWorkoutScreen", "Routine document doesn't exist")
                        error = "No se encontró la rutina"
                        isLoading = false
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("TrackWorkoutScreen", "Error loading routine", e)
                    error = "Error al cargar la rutina: ${e.message}"
                    isLoading = false
                }
        } catch (e: Exception) {
            Log.e("TrackWorkoutScreen", "Exception in LaunchedEffect", e)
            error = e.message
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registrar Entrenamiento") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showFinishDialog = true }
                    ) {
                        Icon(Icons.Default.Check, "Finalizar entrenamiento")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    items(exercisesWithSets) { exerciseWithSets ->
                        val currentDay = getCurrentDay()
                        val exerciseConfig = routine?.exercisesByDay?.get(currentDay)
                            ?.find { it.exerciseId == exerciseWithSets.exercise.id }
                            ?: ExerciseConfig(exerciseId = exerciseWithSets.exercise.id)

                        ExerciseTrackCard(
                            exercise = exerciseWithSets.exercise,
                            exerciseConfig = exerciseConfig,
                            sets = exerciseWithSets.sets,
                            onSetsUpdated = { updatedSets ->
                                exercisesWithSets = exercisesWithSets.map {
                                    if (it.exercise.id == exerciseWithSets.exercise.id) {
                                        it.copy(sets = updatedSets)
                                    } else {
                                        it
                                    }
                                }
                            }
                        )
                    }
                }
            }

            if (error != null) {
                Snackbar(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(error ?: "Error desconocido")
                }
            }
        }

        if (showFinishDialog) {
            AlertDialog(
                onDismissRequest = { showFinishDialog = false },
                title = { Text("Finalizar entrenamiento") },
                text = {
                    Column {
                        Text("¿Deseas guardar este entrenamiento?")
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = workoutNotes,
                            onValueChange = { workoutNotes = it },
                            label = { Text("Notas del entrenamiento") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            scope.launch {
                                try {
                                    val userId = auth.currentUser?.uid
                                    if (userId != null) {
                                        val exerciseRecords = exercisesWithSets.map { exerciseWithSets ->
                                            ExerciseRecord(
                                                id = UUID.randomUUID().toString(),
                                                exerciseId = exerciseWithSets.exercise.id,
                                                sets = exerciseWithSets.sets,
                                                date = System.currentTimeMillis()
                                            )
                                        }

                                        exerciseRecords.forEach { record ->
                                            db.collection("exercise_records")
                                                .document(record.id)
                                                .set(record)
                                        }

                                        val workoutRecord = WorkoutRecord(
                                            id = UUID.randomUUID().toString(),
                                            userId = userId,
                                            routineId = routineId,
                                            exerciseRecords = exerciseRecords.map { it.id },
                                            date = System.currentTimeMillis(),
                                            notes = workoutNotes
                                        )

                                        db.collection("workout_records")
                                            .document(workoutRecord.id)
                                            .set(workoutRecord)
                                            .addOnSuccessListener {
                                                navController.navigate("home") {
                                                    popUpTo("home") { inclusive = true }
                                                }
                                            }
                                    }
                                } catch (e: Exception) {
                                    error = e.message
                                }
                            }
                            showFinishDialog = false
                        }
                    ) {
                        Text("Guardar")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showFinishDialog = false }
                    ) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}

@Composable
private fun ExerciseTrackCard(
    exercise: Exercise,
    exerciseConfig: ExerciseConfig,
    sets: List<com.example.fittrack.data.Set>,
    onSetsUpdated: (List<com.example.fittrack.data.Set>) -> Unit
) {
    var currentSets by remember {
        mutableStateOf(
            if (sets.isEmpty()) {
                List(exerciseConfig.sets) {
                    com.example.fittrack.data.Set(reps = exerciseConfig.repsPerSet)
                }
            } else sets
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = exercise.name,
                style = MaterialTheme.typography.titleMedium
            )

            if (exerciseConfig.rir > 0) {
                Text(
                    text = "RIR objetivo: ${exerciseConfig.rir}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            currentSets.forEachIndexed { index, set ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Serie ${index + 1}",
                        modifier = Modifier.width(60.dp)
                    )
                    OutlinedTextField(
                        value = set.reps.toString(),
                        onValueChange = { newReps ->
                            currentSets = currentSets.mapIndexed { i, s ->
                                if (i == index) s.copy(reps = newReps.toIntOrNull() ?: s.reps) else s
                            }
                            onSetsUpdated(currentSets)
                        },
                        label = { Text("Reps") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}