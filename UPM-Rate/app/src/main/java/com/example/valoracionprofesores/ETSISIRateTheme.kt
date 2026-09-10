package com.example.valoracionprofesores

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun ETSISIRateTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Color(0xFF2196F3),
            secondary = Color(0xFF1976D2),
            background = Color.White,
            surface = Color.White
        ),
        content = content
    )
}