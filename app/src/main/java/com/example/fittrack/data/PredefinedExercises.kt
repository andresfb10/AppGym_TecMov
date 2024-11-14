package com.example.fittrack.data

// Lista predefinida de ejercicios
object PredefinedExercises {
    val exercises = listOf(
        // Pecho
        Exercise("bench_press", "Press de Banca", false),
        Exercise("incline_bench", "Press Inclinado", false),
        Exercise("decline_bench", "Press Declinado", false),
        Exercise("dumbbell_press", "Press con Mancuernas", false),
        Exercise("chest_flyes", "Aperturas", false),
        Exercise("push_ups", "Flexiones", false),

        // Espalda
        Exercise("pull_ups", "Dominadas", false),
        Exercise("lat_pulldown", "Jalón al Pecho", false),
        Exercise("barbell_row", "Remo con Barra", false),
        Exercise("dumbbell_row", "Remo con Mancuerna", false),
        Exercise("face_pull", "Face Pull", false),
        Exercise("deadlift", "Peso Muerto", false),

        // Hombros
        Exercise("military_press", "Press Militar", false),
        Exercise("lateral_raise", "Elevaciones Laterales", false),
        Exercise("front_raise", "Elevaciones Frontales", false),
        Exercise("rear_delt_fly", "Aperturas Posteriores", false),
        Exercise("upright_row", "Remo al Mentón", false),

        // Piernas
        Exercise("squat", "Sentadilla", false),
        Exercise("leg_press", "Prensa", false),
        Exercise("lunges", "Zancadas", false),
        Exercise("leg_extension", "Extensión de Cuádriceps", false),
        Exercise("leg_curl", "Curl de Isquiotibiales", false),
        Exercise("calf_raise", "Elevación de Gemelos", false),

        // Bíceps
        Exercise("barbell_curl", "Curl con Barra", false),
        Exercise("dumbbell_curl", "Curl con Mancuernas", false),
        Exercise("hammer_curl", "Curl Martillo", false),
        Exercise("preacher_curl", "Curl Scott", false),

        // Tríceps
        Exercise("tricep_pushdown", "Extensión de Tríceps", false),
        Exercise("tricep_extension", "Extensión sobre Cabeza", false),
        Exercise("skull_crusher", "Press Francés", false),
        Exercise("dips", "Fondos", false),

        // Abdominales
        Exercise("crunches", "Abdominales", false),
        Exercise("leg_raise", "Elevación de Piernas", false),
        Exercise("plank", "Plancha", false),
        Exercise("russian_twist", "Giro Ruso", false)
    )
}