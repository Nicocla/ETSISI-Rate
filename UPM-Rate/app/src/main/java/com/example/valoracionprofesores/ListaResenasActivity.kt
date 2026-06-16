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

class ListaResenasActivity : AppCompatActivity() {

    private var profesorId: Int = 0
    private var esAdmin: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista_resenas)

        // 1. RECIBIR DATOS
        profesorId = intent.getIntExtra("PROFESOR_ID", 0)
        val prefs = getSharedPreferences("MisPreferencias", MODE_PRIVATE)
        esAdmin = prefs.getBoolean("ES_ADMIN", false)

        // Titulo de la ventana
        title = "Reseñas del Profesor"

        // 2. CARGAR LISTA
        cargarResenas()
    }

    private fun cargarResenas() {
        val rv = findViewById<RecyclerView>(R.id.rvResenas)
        rv.layoutManager = LinearLayoutManager(this)

        val api = RetrofitClient.instance.create(ApiService::class.java)
        api.obtenerResenas(profesorId).enqueue(object : Callback<List<Resena>> {
            override fun onResponse(call: Call<List<Resena>>, response: Response<List<Resena>>) {
                val lista = response.body() ?: emptyList()

                // Si la lista está vacía
                if (lista.isEmpty()) {
                    Toast.makeText(this@ListaResenasActivity, "Aún no hay reseñas", Toast.LENGTH_SHORT).show()
                }

                // Configurar adaptador
                rv.adapter = ResenaAdapter(lista) { resena ->
                    // ESTO SE EJECUTA AL MANTENER PULSADO
                    if (esAdmin) {
                        confirmarBorrado(resena)
                    }
                }
            }
            override fun onFailure(call: Call<List<Resena>>, t: Throwable) {
                Toast.makeText(this@ListaResenasActivity, "Error de conexión", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun confirmarBorrado(resena: Resena) {
        AlertDialog.Builder(this)
            .setTitle("Moderar Comentario")
            // CAMBIO: Quitamos el email del mensaje
            .setMessage("¿Estás seguro de que quieres borrar esta reseña?\nEsta acción es irreversible.")
            .setPositiveButton("Borrar") { _, _ ->
                borrarResenaAPI(resena)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun borrarResenaAPI(resena: Resena) {
        val api = RetrofitClient.instance.create(ApiService::class.java)
        // Llamamos a la API enviando ID del profe y EMAIL del alumno (claves únicas)
        api.borrarResena(profesorId, resena.email_alumno).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@ListaResenasActivity, "Reseña eliminada", Toast.LENGTH_SHORT).show()
                    cargarResenas() // Recargamos la lista
                } else {
                    Toast.makeText(this@ListaResenasActivity, "Error al borrar", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@ListaResenasActivity, "Fallo de conexión", Toast.LENGTH_SHORT).show()
            }
        })
    }
}