package com.example.fittrack.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.fittrack.data.*
import java.util.*
import com.example.fittrack.data.Exercise
import com.example.fittrack.data.ExerciseRecord
import com.example.fittrack.data.Set
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxWidth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackWorkoutScreen(
    routineId: String,
    navController: NavController
) {
    var routine by remember { mutableStateOf<Routine?>(null) }
    var currentExercises by remember { mutableStateOf<List<Exercise>>(emptyList()) }
    var exerciseRecords by remember { mutableStateOf<Map<String, ExerciseRecord>>(emptyMap()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var showFinishDialog by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()

    // Cargar rutina y ejercicios
    LaunchedEffect(routineId) {
        try {
            // Cargar la rutina
            db.collection("routines")
                .document(routineId)
                .get()
                .addOnSuccessListener { document ->
                    routine = document.toObject(Routine::class.java)

                    // Obtener el día actual
                    val calendar = Calendar.getInstance()
                    val dayOfWeek = when(calendar.get(Calendar.DAY_OF_WEEK)) {
                        Calendar.MONDAY -> "Lunes"
                        Calendar.TUESDAY -> "Martes"
                        Calendar.WEDNESDAY -> "Miércoles"
                        Calendar.THURSDAY -> "Jueves"
                        Calendar.FRIDAY -> "Viernes"
                        Calendar.SATURDAY -> "Sábado"
                        Calendar.SUNDAY -> "Domingo"
                        else -> ""
                    }

                    // Cargar los ejercicios del día
                    val exerciseIds = routine?.exercisesByDay?.get(dayOfWeek) ?: emptyList()
                    if (exerciseIds.isNotEmpty()) {
                        db.collection("exercises")
                            .whereIn("id", exerciseIds)
                            .get()
                            .addOnSuccessListener { snapshot ->
                                currentExercises = snapshot.documents.mapNotNull {
                                    it.toObject(Exercise::class.java)
                                }
                                isLoading = false
                            }
                    } else {
                        isLoading = false
                    }
                }
                .addOnFailureListener {
                    error = it.message
                    isLoading = false
                }
        } catch (e: Exception) {
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
                    items(currentExercises) { exercise ->
                        ExerciseTrackCard(
                            exercise = exercise,
                            exerciseRecord = exerciseRecords[exercise.id],
                            onSetsUpdated = { sets ->
                                val record = ExerciseRecord(
                                    id = UUID.randomUUID().toString(),
                                    exerciseId = exercise.id,
                                    sets = sets,
                                    date = System.currentTimeMillis()
                                )
                                exerciseRecords = exerciseRecords + (exercise.id to record)
                            }
                        )
                    }
                }
            }
        }

        if (showFinishDialog) {
            AlertDialog(
                onDismissRequest = { showFinishDialog = false },
                title = { Text("Finalizar entrenamiento") },
                text = { Text("¿Deseas guardar este entrenamiento?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            scope.launch {
                                try {
                                    val userId = auth.currentUser?.uid
                                    if (userId != null) {
                                        // Guardar cada registro de ejercicio
                                        exerciseRecords.values.forEach { record ->
                                            db.collection("exercise_records")
                                                .document(record.id)
                                                .set(record)
                                        }

                                        // Crear registro de entrenamiento
                                        val workoutRecord = WorkoutRecord(
                                            id = UUID.randomUUID().toString(),
                                            userId = userId,
                                            routineId = routineId,
                                            exerciseRecords = exerciseRecords.values.map { it.id },
                                            date = System.currentTimeMillis()
                                        )

                                        db.collection("workout_records")
                                            .document(workoutRecord.id)
                                            .set(workoutRecord)
                                            .addOnSuccessListener {
                                                navController.navigate("workout_summary/${workoutRecord.id}")
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

        if (error != null) {
            Snackbar(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(error ?: "Error desconocido")
            }
        }
    }
}


@Composable
private fun ExerciseTrackCard(
    exercise: Exercise,
    exerciseRecord: ExerciseRecord?,
    onSetsUpdated: (List<Set<Any?>>) -> Unit
) {

    val sets = remember { mutableStateOf(exerciseRecord?.sets?.toMutableList() ?: mutableListOf(Set())) }

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

            Spacer(modifier = Modifier.height(8.dp))

            sets.value.forEachIndexed { index, set ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Serie ${index + 1}",
                        modifier = Modifier.width(60.dp)
                    )

                    // Campo de repeticiones
                    OutlinedTextField(
                        value = if (set.reps == 0) "" else set.reps.toString(),
                        onValueChange = { value ->
                            val newSets = sets.value.toMutableList() // Convertimos a lista mutable
                            val newReps = value.toIntOrNull() ?: 0
                            newSets[index] = set.copy(reps = newReps)
                            sets.value = newSets // Actualizamos el valor del estado
                            onSetsUpdated(sets.value) // Llamamos al callback
                        },
                        label = { Text("Reps") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )

                    // Campo de peso
                    OutlinedTextField(
                        value = if (set.weight == 0f) "" else set.weight.toString(),
                        onValueChange = { value ->
                            val newSets = sets.value.toMutableList() // Convertimos a lista mutable
                            val newWeight = value.toFloatOrNull() ?: 0f
                            newSets[index] = set.copy(weight = newWeight)
                            sets.value = newSets // Actualizamos el valor del estado
                            onSetsUpdated(sets.value) // Llamamos al callback
                        },
                        label = { Text("Kg") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )

                    // Botón para eliminar la serie
                    IconButton(
                        onClick = {
                            if (sets.value.size > 1) {
                                val newSets = sets.value.toMutableList()
                                newSets.removeAt(index) // Eliminamos la serie
                                sets.value = newSets // Actualizamos el estado
                                onSetsUpdated(sets.value) // Llamamos al callback
                            }
                        }
                    ) {
                        Icon(Icons.Default.Delete, "Eliminar serie")
                    }
                }
            }

            // Botón para añadir una nueva serie
            TextButton(
                onClick = {
                    val newSets = sets.value.toMutableList()
                    newSets.add(Set()) // Añadimos una nueva serie vacía
                    sets.value = newSets // Actualizamos el estado
                    onSetsUpdated(sets.value) // Llamamos al callback
                },
                modifier = Modifier.align(Alignment.End)
            ) {
                Icon(Icons.Default.Add, "Añadir serie")
                Spacer(modifier = Modifier.width(4.dp))
                Text("Añadir serie")
            }
        }
    }
}







