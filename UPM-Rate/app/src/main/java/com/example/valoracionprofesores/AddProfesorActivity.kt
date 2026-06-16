package com.example.valoracionprofesores

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AddProfesorActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Ocultar barra morada (La que sale repetida en tu segunda captura 😉)
        supportActionBar?.hide()

        // ¡OJO! Aquí usamos el nombre exacto de tu archivo XML
        setContentView(R.layout.activity_add_profesor)

        val etNombre = findViewById<TextInputEditText>(R.id.etNombreProfesor)
        val etDepartamento = findViewById<TextInputEditText>(R.id.etDepartamentoProfesor)
        val btnGuardar = findViewById<Button>(R.id.btnGuardarProfesor)

        btnGuardar.setOnClickListener {
            val nombre = etNombre.text.toString().trim()
            val departamento = etDepartamento.text.toString().trim()

            if (nombre.isNotEmpty() && departamento.isNotEmpty()) {
                guardarEnServidor(nombre, departamento)
            } else {
                Toast.makeText(this, "Por favor, rellena todos los campos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun guardarEnServidor(nombre: String, departamento: String) {
        val apiService = RetrofitClient.instance.create(ApiService::class.java)

        apiService.anadirProfesor(nombre, departamento).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@AddProfesorActivity, "Profesor añadido", Toast.LENGTH_LONG).show()
                    Toast.makeText(this@AddProfesorActivity, "Profesor añadido", Toast.LENGTH_LONG).show()
                    finish() // Cierra esta pantalla y vuelve al MainActivity
                } else {
                    // CÁMBIALO POR ESTO PARA VER EL NÚMERO DE ERROR:
                    Toast.makeText(this@AddProfesorActivity, "Error del servidor: ${response.code()}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@AddProfesorActivity, "Fallo de conexión", Toast.LENGTH_SHORT).show()
            }
        })
    }
}