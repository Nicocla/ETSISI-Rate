package com.example.valoracionprofesores

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MisResenasActivity : AppCompatActivity() {

    private lateinit var emailAlumno: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mis_resenas)

        // Recuperar el email del alumno
        val prefs = getSharedPreferences("MisPreferencias", MODE_PRIVATE)

        // CORRECCIÓN AQUÍ: Usamos "EMAIL_USUARIO", que es la clave exacta
        // que guardamos cuando el alumno inició sesión en LoginActivity
        emailAlumno = prefs.getString("EMAIL_USUARIO", "") ?: ""

        // DEBUG: Imprimir en el log de Android qué email estamos usando
        println("📱 Android buscando reseñas para: $emailAlumno")

        // Si por algún casual no hay email, avisamos. Si lo hay, cargamos las reseñas.
        if (emailAlumno.isNotEmpty()) {
            cargarMisResenas()
        } else {
            Toast.makeText(this, "Error: No se encontró tu correo. Vuelve a iniciar sesión.", Toast.LENGTH_LONG).show()
        }
    }

    private fun cargarMisResenas() {
        val rv = findViewById<RecyclerView>(R.id.rvMisResenas)
        rv.layoutManager = LinearLayoutManager(this)

        val api = RetrofitClient.instance.create(ApiService::class.java)
        api.obtenerMisResenas(emailAlumno).enqueue(object : Callback<List<MiResenaItem>> {
            override fun onResponse(call: Call<List<MiResenaItem>>, response: Response<List<MiResenaItem>>) {
                val lista = response.body() ?: emptyList()

                if (lista.isEmpty()) {
                    Toast.makeText(this@MisResenasActivity, "No has valorado a nadie aún", Toast.LENGTH_SHORT).show()
                }

                rv.adapter = MisResenasAdapter(lista) { resena ->
                    // AL PULSAR LARGO: BORRAR
                    confirmarBorrado(resena)
                }
            }
            override fun onFailure(call: Call<List<MiResenaItem>>, t: Throwable) {
                Toast.makeText(this@MisResenasActivity, "Error de conexión", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun confirmarBorrado(item: MiResenaItem) {
        AlertDialog.Builder(this)
            .setTitle("Borrar valoración")
            .setMessage("¿Quieres eliminar tu reseña de ${item.nombre_profesor}?")
            .setPositiveButton("Sí, borrar") { _, _ ->
                borrarResenaDeVerdad(item)
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun borrarResenaDeVerdad(item: MiResenaItem) {
        val api = RetrofitClient.instance.create(ApiService::class.java)
        api.borrarResena(item.profesor_id, emailAlumno).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@MisResenasActivity, "Eliminada correctamente", Toast.LENGTH_SHORT).show()
                    cargarMisResenas() // Refrescar lista
                } else {
                    Toast.makeText(this@MisResenasActivity, "Error al borrar", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@MisResenasActivity, "Fallo de red", Toast.LENGTH_SHORT).show()
            }
        })
    }
}