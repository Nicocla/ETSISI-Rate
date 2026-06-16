package com.example.valoracionprofesores

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // 1. Ocultar la barra de arriba (ActionBar) para que quede más limpio
        supportActionBar?.hide()

        // 2. Crear el temporizador de 3 segundos (3000ms)
        Handler(Looper.getMainLooper()).postDelayed({

            // 3. Viajar a la LoginActivity
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()

            // 4. Cerrar esta actividad para que no se pueda volver atrás
            finish()

        }, 3000) // Cambia 3000 por el tiempo que quieras
    }
}