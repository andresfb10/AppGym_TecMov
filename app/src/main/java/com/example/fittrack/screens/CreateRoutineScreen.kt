package com.example.fittrack.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*

import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bed
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search


import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

// Imports de Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

// Imports de corrutinas
import kotlinx.coroutines.launch

// Imports de los modelos de datos
import com.example.fittrack.data.Exercise
import com.example.fittrack.data.ExerciseConfig
import com.example.fittrack.data.Routine
import com.example.fittrack.data.PredefinedExercises
import kotlinx.coroutines.CoroutineScope


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRoutineScreen(
    navController: NavController
) {
    // Estados y dependencias
    val scope = rememberCoroutineScope()
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val daysOfWeek = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo")

    // Estados de la rutina
    var currentStep by remember { mutableStateOf(0) }
    var routineName by remember { mutableStateOf("") }
    var routineDescription by remember { mutableStateOf("") }
    var selectedTrainingDays by remember { mutableStateOf(setOf<String>()) }
    var selectedRestDays by remember { mutableStateOf(setOf<String>()) }
    var dayNames by remember { mutableStateOf(mutableMapOf<String, String>()) }
    var exercisesByDay by remember { mutableStateOf(mutableMapOf<String, List<ExerciseConfig>>()) }

    // Estados UI
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var showExerciseDialog by remember { mutableStateOf(false) }
    var showConfigDialog by remember { mutableStateOf(false) }
    var selectedDayForExercises by remember { mutableStateOf<String?>(null) }
    var selectedExercise by remember { mutableStateOf<Exercise?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var availableExercises by remember { mutableStateOf(PredefinedExercises.exercises) }

    // Estados temporales para configuración de ejercicios
    var tempSets by remember { mutableStateOf("") }
    var tempReps by remember { mutableStateOf("") }
    var tempRir by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crear Rutina") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Indicador de progreso
            LinearProgressIndicator(
                progress = (currentStep + 1) / 4f,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            // Contenido principal basado en el paso actual
            when (currentStep) {
                0 -> BasicInformationStep(
                    routineName = routineName,
                    routineDescription = routineDescription,
                    onNameChange = { routineName = it },
                    onDescriptionChange = { routineDescription = it }
                )

                1 -> TrainingDaysStep(
                    daysOfWeek = daysOfWeek,
                    selectedDays = selectedTrainingDays,
                    onDaySelected = { day, selected ->
                        if (selected) {
                            selectedTrainingDays = selectedTrainingDays + day
                            selectedRestDays = selectedRestDays - day
                        } else {
                            selectedTrainingDays = selectedTrainingDays - day
                            selectedRestDays = selectedRestDays + day
                        }
                    }
                )

                2 -> RestDaysStep(
                    selectedRestDays = selectedRestDays.toList()
                )

                3 -> ExerciseConfigurationStep(
                    selectedTrainingDays = selectedTrainingDays.toList(),
                    dayNames = dayNames,
                    exercisesByDay = exercisesByDay,
                    onDayNameChange = { day, name ->
                        dayNames = dayNames.toMutableMap().apply {
                            put(day, name)
                        }
                    },
                    onAddExercise = { day ->
                        selectedDayForExercises = day
                        showExerciseDialog = true
                    },
                    onEditExercise = { day, exercise ->
                        selectedExercise = Exercise(id = exercise.exerciseId, name = exercise.name)
                        selectedDayForExercises = day
                        tempSets = exercise.sets.toString()
                        tempReps = exercise.repsPerSet.toString()
                        tempRir = exercise.rir.toString()
                        showConfigDialog = true
                    },
                    onDeleteExercise = { day, exerciseId ->
                        exercisesByDay = exercisesByDay.toMutableMap().apply {
                            put(day, exercisesByDay[day]?.filter { it.exerciseId != exerciseId } ?: emptyList())
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Navegación entre pasos
            NavigationButtons(
                currentStep = currentStep,
                onPreviousClick = { currentStep-- },
                onNextClick = { currentStep++ },
                onSaveClick = {
                    saveRoutine(
                        scope = scope,
                        auth = auth,
                        db = db,
                        routineName = routineName,
                        routineDescription = routineDescription,
                        selectedTrainingDays = selectedTrainingDays,
                        selectedRestDays = selectedRestDays,
                        dayNames = dayNames,
                        exercisesByDay = exercisesByDay,
                        onError = { error = it },
                        onSuccess = { navController.navigate("home") },
                        setLoading = { isLoading = it }
                    )
                },
                isLoading = isLoading,
                isLastStep = currentStep == 3,
                canSave = routineName.isNotBlank() && !isLoading
            )

            // Mostrar errores si existen
            if (error != null) {
                ErrorMessage(error = error ?: "")
            }
        }

        // Diálogos
        if (showExerciseDialog) {
            ExerciseSelectionDialog(
                exercises = availableExercises,
                searchQuery = searchQuery,
                selectedDayExercises = selectedDayForExercises?.let { exercisesByDay[it] } ?: emptyList(),
                onSearchQueryChange = { searchQuery = it },
                onExerciseSelected = { exercise ->
                    selectedExercise = exercise
                    showExerciseDialog = false
                    showConfigDialog = true
                    tempSets = ""
                    tempReps = ""
                    tempRir = ""
                },
                onDismiss = {
                    showExerciseDialog = false
                    selectedDayForExercises = null
                    searchQuery = ""
                }
            )
        }

        if (showConfigDialog && selectedExercise != null) {
            ExerciseConfigDialog(
                exercise = selectedExercise!!,
                sets = tempSets,
                reps = tempReps,
                rir = tempRir,
                onSetsChange = { tempSets = it },
                onRepsChange = { tempReps = it },
                onRirChange = { tempRir = it },
                onSave = { sets, reps, rir ->
                    selectedDayForExercises?.let { day ->
                        val exerciseConfig = ExerciseConfig(
                            exerciseId = selectedExercise?.id ?: "",
                            name = selectedExercise?.name ?: "",
                            sets = sets,
                            repsPerSet = reps,
                            rir = rir
                        )
                        exercisesByDay = exercisesByDay.toMutableMap().apply {
                            val currentExercises = (get(day) ?: emptyList()).filter {
                                it.exerciseId != exerciseConfig.exerciseId
                            }
                            put(day, currentExercises + exerciseConfig)
                        }
                    }
                    showConfigDialog = false
                    selectedExercise = null
                },
                onDismiss = {
                    showConfigDialog = false
                    selectedExercise = null
                }
            )
        }
    }
}


@Composable
fun BasicInformationStep(
    routineName: String,
    routineDescription: String,
    onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit
) {
    Text(
        "Paso 1: Información básica",
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.padding(bottom = 16.dp)
    )

    OutlinedTextField(
        value = routineName,
        onValueChange = onNameChange,
        label = { Text("Nombre de la rutina") },
        modifier = Modifier.fillMaxWidth(),
        isError = routineName.isBlank(),
        supportingText = {
            if (routineName.isBlank()) {
                Text("El nombre es obligatorio", color = MaterialTheme.colorScheme.error)
            }
        }
    )

    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
        value = routineDescription,
        onValueChange = onDescriptionChange,
        label = { Text("Descripción (opcional)") },
        modifier = Modifier.fillMaxWidth(),
        minLines = 3
    )
}

@Composable
fun TrainingDaysStep(
    daysOfWeek: List<String>,
    selectedDays: Set<String>,
    onDaySelected: (String, Boolean) -> Unit
) {
    Text(
        "Paso 2: Días de entrenamiento",
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.padding(bottom = 16.dp)
    )

    LazyColumn {
        items(daysOfWeek) { day ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp, horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(day)
                    Switch(
                        checked = selectedDays.contains(day),
                        onCheckedChange = { checked -> onDaySelected(day, checked) }
                    )
                }
            }
        }
    }
}

@Composable
fun RestDaysStep(
    selectedRestDays: List<String>
) {
    Text(
        "Paso 3: Días de descanso",
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.padding(bottom = 16.dp)
    )

    Text(
        "Los siguientes días han sido marcados como descanso:",
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.padding(bottom = 8.dp)
    )

    LazyColumn {
        items(selectedRestDays) { day ->
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
                    Text(day)
                    Icon(Icons.Default.Bed, "Día de descanso")
                }
            }
        }
    }
}

@Composable
fun ExerciseConfigurationStep(
    selectedTrainingDays: List<String>,
    dayNames: Map<String, String>,
    exercisesByDay: Map<String, List<ExerciseConfig>>,
    onDayNameChange: (String, String) -> Unit,
    onAddExercise: (String) -> Unit,
    onEditExercise: (String, ExerciseConfig) -> Unit,
    onDeleteExercise: (String, String) -> Unit
) {
    Text(
        "Paso 4: Configurar días de entrenamiento",
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.padding(bottom = 16.dp)
    )

    LazyColumn(
        //modifier = Modifier.weight(1f)
    ) {
        items(selectedTrainingDays) { day ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = dayNames[day] ?: "",
                        onValueChange = { onDayNameChange(day, it) },
                        label = { Text("Nombre para $day") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Lista de ejercicios configurados
                    exercisesByDay[day]?.forEach { exerciseConfig ->
                        ExerciseItem(
                            exerciseConfig = exerciseConfig,
                            onEdit = { onEditExercise(day, exerciseConfig) },
                            onDelete = { onDeleteExercise(day, exerciseConfig.exerciseId) }
                        )
                    }

                    Button(
                        onClick = { onAddExercise(day) },
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(top = 8.dp)
                    ) {
                        Icon(Icons.Default.Add, "Añadir ejercicio")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Añadir ejercicios")
                    }
                }
            }
        }
    }
}

@Composable
private fun ExerciseItem(
    exerciseConfig: ExerciseConfig,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = exerciseConfig.name,
                    style = MaterialTheme.typography.bodyLarge
                )
                if (exerciseConfig.sets > 0) {
                    Text(
                        text = "${exerciseConfig.sets} series x ${exerciseConfig.repsPerSet} reps" +
                                if (exerciseConfig.rir > 0) " (RIR: ${exerciseConfig.rir})" else "",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, "Editar ejercicio")
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, "Eliminar ejercicio")
                }
            }
        }
    }
}

@Composable
fun ExerciseSelectionDialog(
    exercises: List<Exercise>,
    searchQuery: String,
    selectedDayExercises: List<ExerciseConfig>,
    onSearchQueryChange: (String) -> Unit,
    onExerciseSelected: (Exercise) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Seleccionar ejercicios") },
        text = {
            Column {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    label = { Text("Buscar ejercicio") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    singleLine = true,
                    leadingIcon = {
                        Icon(Icons.Default.Search, "Buscar")
                    }
                )

                LazyColumn(
                    modifier = Modifier.height(400.dp)
                ) {
                    items(
                        exercises.filter {
                            it.name.contains(searchQuery, ignoreCase = true)
                        }
                    ) { exercise ->
                        val isSelected = selectedDayExercises.any {
                            it.exerciseId == exercise.id
                        }

                        ExerciseSelectionItem(
                            exercise = exercise,
                            isSelected = isSelected,
                            onClick = { onExerciseSelected(exercise) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        }
    )
}

@Composable
fun ExerciseConfigDialog(
    exercise: Exercise,
    sets: String,
    reps: String,
    rir: String,
    onSetsChange: (String) -> Unit,
    onRepsChange: (String) -> Unit,
    onRirChange: (String) -> Unit,
    onSave: (sets: Int, reps: Int, rir: Int) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Configurar ${exercise.name}") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = sets,
                    onValueChange = { onSetsChange(it.filter { it.isDigit() }) },
                    label = { Text("Número de series") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    isError = sets.toIntOrNull()?.let { it <= 0 } ?: true,
                    supportingText = {
                        if (sets.toIntOrNull()?.let { it <= 0 } ?: true) {
                            Text("Ingresa un número válido de series", color = MaterialTheme.colorScheme.error)
                        }
                    }
                )

                OutlinedTextField(
                    value = reps,
                    onValueChange = { onRepsChange(it.filter { it.isDigit() }) },
                    label = { Text("Repeticiones por serie") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    isError = reps.toIntOrNull()?.let { it <= 0 } ?: true,
                    supportingText = {
                        if (reps.toIntOrNull()?.let { it <= 0 } ?: true) {
                            Text("Ingresa un número válido de repeticiones", color = MaterialTheme.colorScheme.error)
                        }
                    }
                )

                OutlinedTextField(
                    value = rir,
                    onValueChange = { onRirChange(it.filter { it.isDigit() }) },
                    label = { Text("RIR (Repeticiones en Reserva)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            val setsValue = sets.toIntOrNull() ?: 0
            val repsValue = reps.toIntOrNull() ?: 0
            val rirValue = rir.toIntOrNull() ?: 0

            Button(
                onClick = { onSave(setsValue, repsValue, rirValue) },
                enabled = setsValue > 0 && repsValue > 0
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
private fun ExerciseSelectionItem(
    exercise: Exercise,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = exercise.name,
            style = MaterialTheme.typography.bodyLarge
        )
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Seleccionado",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
    Divider()
}

@Composable
fun NavigationButtons(
    currentStep: Int,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    onSaveClick: () -> Unit,
    isLoading: Boolean,
    isLastStep: Boolean,
    canSave: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        if (currentStep > 0) {
            Button(onClick = onPreviousClick) {
                Text("Anterior")
            }
        }

        if (!isLastStep) {
            Button(onClick = onNextClick) {
                Text("Siguiente")
            }
        } else {
            Button(
                onClick = onSaveClick,
                enabled = canSave
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Guardar Rutina")
                }
            }
        }
    }
}

@Composable
fun ErrorMessage(error: String) {
    Text(
        text = error,
        color = MaterialTheme.colorScheme.error,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    )
}
private fun saveRoutine(
    scope: CoroutineScope,
    auth: FirebaseAuth,
    db: FirebaseFirestore,
    routineName: String,
    routineDescription: String,
    selectedTrainingDays: Set<String>,
    selectedRestDays: Set<String>,
    dayNames: Map<String, String>,
    exercisesByDay: Map<String, List<ExerciseConfig>>,
    onError: (String) -> Unit,
    onSuccess: () -> Unit,
    setLoading: (Boolean) -> Unit
) {
    scope.launch {
        try {
            setLoading(true)
            val userId = auth.currentUser?.uid
            if (userId != null) {
                val routine = Routine(
                    id = db.collection("routines").document().id,
                    name = routineName,
                    description = routineDescription,
                    daysOfWeek = selectedTrainingDays.toList(),
                    restDays = selectedRestDays.toList(),
                    exercisesByDay = exercisesByDay,
                    dayNames = dayNames,
                    userId = userId,
                    createdAt = System.currentTimeMillis()
                )

                db.collection("routines")
                    .document(routine.id)
                    .set(routine)
                    .addOnSuccessListener {
                        onSuccess()
                    }
                    .addOnFailureListener {
                        onError(it.message ?: "Error desconocido")
                    }
            }
        } catch (e: Exception) {
            onError(e.message ?: "Error desconocido")
        } finally {
            setLoading(false)
        }
    }
}