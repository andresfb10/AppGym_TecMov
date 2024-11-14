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
        Exercise("dips_chest", "Fondos para Pecho", false),
        Exercise("cable_crossover", "Cruce de Cables", false),
        Exercise("chest_dips", "Fondos en Paralelas", false),
        Exercise("machine_chest_press", "Press de Pecho en Máquina", false),

        // Espalda
        Exercise("pull_ups", "Dominadas", false),
        Exercise("lat_pulldown", "Jalón al Pecho", false),
        Exercise("barbell_row", "Remo con Barra", false),
        Exercise("dumbbell_row", "Remo con Mancuerna", false),
        Exercise("face_pull", "Face Pull", false),
        Exercise("deadlift", "Peso Muerto", false),
        Exercise("t-bar_row", "Remo en T", false),
        Exercise("single_arm_row", "Remo Unilateral con Mancuerna", false),
        Exercise("chin_ups", "Dominadas con Agarre Supino", false),
        Exercise("machine_lat_pulldown", "Jalón al Pecho en Máquina", false),

        // Hombros
        Exercise("military_press", "Press Militar", false),
        Exercise("lateral_raise", "Elevaciones Laterales", false),
        Exercise("front_raise", "Elevaciones Frontales", false),
        Exercise("rear_delt_fly", "Aperturas Posteriores", false),
        Exercise("upright_row", "Remo al Mentón", false),
        Exercise("arnold_press", "Press Arnold", false),
        Exercise("reverse_fly", "Aperturas Invertidas", false),
        Exercise("dumbbell_shrug", "Encogimientos con Mancuernas", false),
        Exercise("barbell_shrug", "Encogimientos con Barra", false),
        Exercise("machine_shoulder_press", "Press de Hombros en Máquina", false),

        // Piernas
        Exercise("squat", "Sentadilla", false),
        Exercise("leg_press", "Prensa", false),
        Exercise("lunges", "Zancadas", false),
        Exercise("leg_extension", "Extensión de Cuádriceps", false),
        Exercise("leg_curl", "Curl de Isquiotibiales", false),
        Exercise("calf_raise", "Elevación de Gemelos", false),
        Exercise("bulgarian_split_squat", "Sentadilla Búlgara", false),
        Exercise("glute_bridge", "Puente de Glúteos", false),
        Exercise("hip_thrust", "Hip Thrust", false),
        Exercise("step_up", "Paso en Banco", false),
        Exercise("hack_squat", "Sentadilla Hack", false),

        // Bíceps
        Exercise("barbell_curl", "Curl con Barra", false),
        Exercise("dumbbell_curl", "Curl con Mancuernas", false),
        Exercise("hammer_curl", "Curl Martillo", false),
        Exercise("preacher_curl", "Curl Scott", false),
        Exercise("concentration_curl", "Curl Concentrado", false),
        Exercise("incline_dumbbell_curl", "Curl Inclinado con Mancuernas", false),
        Exercise("zottman_curl", "Curl Zottman", false),
        Exercise("cable_curl", "Curl en Cable", false),
        Exercise("alternating_dumbbell_curl", "Curl Alternado con Mancuernas", false),
        Exercise("standing_dumbbell_curl", "Curl de Pie con Mancuernas", false),

        // Tríceps
        Exercise("tricep_pushdown", "Extensión de Tríceps", false),
        Exercise("tricep_extension", "Extensión sobre Cabeza", false),
        Exercise("skull_crusher", "Press Francés", false),
        Exercise("dips", "Fondos", false),
        Exercise("close_grip_bench_press", "Press de Banca con Agarre Cerrado", false),
        Exercise("overhead_tricep_extension", "Extensión de Tríceps sobre la Cabeza", false),
        Exercise("tricep_kickback", "Patada de Tríceps", false),
        Exercise("diamond_pushups", "Flexiones en Diamante", false),
        Exercise("tricep_dip_machine", "Fondos en Máquina para Tríceps", false),
        Exercise("cable_tricep_extension", "Extensión de Tríceps en Cable", false),

        // Abdominales
        Exercise("crunches", "Abdominales", false),
        Exercise("leg_raise", "Elevación de Piernas", false),
        Exercise("plank", "Plancha", false),
        Exercise("russian_twist", "Giro Ruso", false),
        Exercise("bicycle_crunch", "Crunch de Bicicleta", false),
        Exercise("v_sit_up", "Elevación V", false),
        Exercise("hanging_leg_raise", "Elevación de Piernas Colgado", false),
        Exercise("cable_crunch", "Crunch con Cable", false),
        Exercise("side_plank", "Plancha Lateral", false),
        Exercise("mountain_climbers", "Escaladores", false),
        Exercise("woodchoppers", "Cortadores de Madera", false),

        // Cardio
        Exercise("treadmill", "Cinta de Correr", true),
        Exercise("stationary_bike", "Bicicleta Estática", true),
        Exercise("rowing_machine", "Máquina de Remo", true),
        Exercise("elliptical", "Elíptica", true),
        Exercise("jump_rope", "Saltar la Cuerda", true),
        Exercise("battle_ropes", "Cuerdas de Batalla", true),
        Exercise("sled_push", "Empuje de Trineo", true),
        Exercise("high_knees", "Rodillas Altas", true),
        Exercise("burpees", "Burpees", true),
        Exercise("jumping_jacks", "Saltos de Tijera", true),

        // Funcionales
        Exercise("kettlebell_swing", "Swing con Kettlebell", false),
        Exercise("battle_ropes", "Cuerdas de Batalla", true),
        Exercise("box_jump", "Salto al Caja", false),
        Exercise("burpees", "Burpees", false),
        Exercise("medicine_ball_slam", "Lanzamiento de balón medicinal", false),
        Exercise("sled_pull", "Tirón de Trineo", false),
        Exercise("sandbag_clean_and_press", "Clean and Press con saco de arena", false),
        Exercise("thrusters", "Thrusters", false),
        Exercise("wall_balls", "Wall Balls", false),
        Exercise("agility_ladder", "Escalera de agilidad", false)
    )
}
