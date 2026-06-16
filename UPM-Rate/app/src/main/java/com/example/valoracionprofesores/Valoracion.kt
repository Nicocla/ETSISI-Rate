package com.example.valoracionprofesores

// Esta clase DEBE tener estos nombres exactos para que funcione con el servidor
data class Valoracion(
    val profesor_id: Int,
    val puntuacion: Int,
    val comentario: String,
)