package com.example.valoracionprofesores
import java.io.Serializable // <--- 1. Importar esto
data class Profesor(
    val id: Int,
    val nombre: String,
    val departamento: String?,
    val media: Double // <--- ¡NUEVO! (Es Double porque puede tener decimales, ej: 4.5)
) : Serializable