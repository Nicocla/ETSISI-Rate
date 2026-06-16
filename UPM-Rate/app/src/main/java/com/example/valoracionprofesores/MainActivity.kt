package com.example.valoracionprofesores

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var rvProfesores: RecyclerView
    private lateinit var shimmerContainer: ShimmerFrameLayout
    private lateinit var adapter: ProfesorAdapter
    private var listaOriginal: List<Profesor> = emptyList()
    private var esAdmin: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. OCULTAR LA BARRA MORADA POR DEFECTO
        supportActionBar?.hide()
        setContentView(R.layout.activity_main)

        // 2. LEER MEMORIA (SABER SI SOMOS ADMIN)
        val prefs = getSharedPreferences("MisPreferencias", MODE_PRIVATE)
        esAdmin = prefs.getBoolean("ES_ADMIN", false)

        // 3. VINCULAR VISTAS
        rvProfesores = findViewById(R.id.rvProfesores)
        shimmerContainer = findViewById(R.id.shimmerViewContainer)
        rvProfesores.layoutManager = LinearLayoutManager(this)

        val btnLogout = findViewById<ImageView>(R.id.btnLogout)
        val etBuscador = findViewById<EditText>(R.id.etBuscador)
        val btnMisResenas = findViewById<View>(R.id.btnMisResenas)
        val fabAddProfesor = findViewById<View>(R.id.fabAddProfesor)

        // 4. LÓGICA DE PRIVILEGIOS: ADMIN VS ALUMNO
        if (esAdmin) {
            btnMisResenas.visibility = View.GONE
            fabAddProfesor.visibility = View.VISIBLE
        } else {
            btnMisResenas.visibility = View.VISIBLE
            fabAddProfesor.visibility = View.GONE
        }

        // 5. CONFIGURAR BOTONES (CLICS)
        btnLogout.setOnClickListener {
            cerrarSesion()
        }

        btnMisResenas.setOnClickListener {
            val intent = Intent(this, MisResenasActivity::class.java)
            startActivity(intent)
        }

        fabAddProfesor.setOnClickListener {
            val intent = Intent(this, AddProfesorActivity::class.java)
            startActivity(intent)
        }

        // 6. CONFIGURAR EL BUSCADOR EN TIEMPO REAL
        etBuscador.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                filtrarLista(s.toString())
            }
        })

        // 7. CARGAR DATOS DEL SERVIDOR
        cargarProfesores()
    }

    // --- FUNCIONES AUXILIARES ---

    private fun cargarProfesores() {
        shimmerContainer.visibility = View.VISIBLE
        shimmerContainer.startShimmer()
        rvProfesores.visibility = View.GONE

        val apiService = RetrofitClient.instance.create(ApiService::class.java)
        apiService.obtenerProfesores().enqueue(object : Callback<List<Profesor>> {
            override fun onResponse(call: Call<List<Profesor>>, response: Response<List<Profesor>>) {

                Handler(Looper.getMainLooper()).postDelayed({
                    shimmerContainer.stopShimmer()
                    shimmerContainer.visibility = View.GONE

                    rvProfesores.alpha = 0f
                    rvProfesores.visibility = View.VISIBLE
                    rvProfesores.animate().alpha(1f).setDuration(300).start()

                    if (response.isSuccessful) {
                        listaOriginal = response.body() ?: emptyList()

                        adapter = ProfesorAdapter(
                            listaOriginal,
                            esAdmin,
                            { profesor ->
                                // Llamamos a la función de confirmación al pulsar la papelera
                                confirmarBorrado(profesor)
                            },
                            { profesor ->
                                val intent = Intent(this@MainActivity, DetalleProfesorActivity::class.java)
                                intent.putExtra("profesor", profesor)
                                startActivity(intent)
                            }
                        )
                        rvProfesores.adapter = adapter
                    }
                }, 1500)
            }

            override fun onFailure(call: Call<List<Profesor>>, t: Throwable) {
                shimmerContainer.stopShimmer()
                shimmerContainer.visibility = View.GONE
                Toast.makeText(this@MainActivity, "Error de conexión", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun filtrarLista(textoBuscado: String) {
        val textoLimpio = textoBuscado.lowercase().trim()
        val listaFiltrada = listaOriginal.filter {
            it.nombre.lowercase().contains(textoLimpio) ||
                    (it.departamento ?: "").lowercase().contains(textoLimpio)
        }

        adapter = ProfesorAdapter(
            listaFiltrada,
            esAdmin,
            { profesor ->
                confirmarBorrado(profesor)
            },
            { profesor ->
                val intent = Intent(this, DetalleProfesorActivity::class.java)
                intent.putExtra("profesor", profesor)
                startActivity(intent)
            }
        )
        rvProfesores.adapter = adapter
    }

    // --- FUNCIÓN PARA BORRAR EN BASE DE DATOS ---
    private fun confirmarBorrado(profesor: Profesor) {
        AlertDialog.Builder(this)
            .setTitle("Borrar Profesor")
            .setMessage("¿Estás seguro de que quieres borrar a ${profesor.nombre}? Esta acción no se puede deshacer.")
            .setPositiveButton("Sí, borrar") { _, _ ->

                val apiService = RetrofitClient.instance.create(ApiService::class.java)
                apiService.borrarProfesor(profesor.id).enqueue(object : Callback<Void> {

                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        if (response.isSuccessful) {
                            Toast.makeText(this@MainActivity, "Profesor borrado", Toast.LENGTH_SHORT).show()
                            // Volvemos a cargar la lista para que desaparezca
                            cargarProfesores()
                        } else {
                            Toast.makeText(this@MainActivity, "Error al borrar", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        Toast.makeText(this@MainActivity, "Fallo de conexión", Toast.LENGTH_SHORT).show()
                    }
                })
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    // --- FUNCIÓN PARA SALIR ---
    private fun cerrarSesion() {
        AlertDialog.Builder(this)
            .setTitle("Cerrar Sesión")
            .setMessage("¿Quieres salir de tu cuenta?")
            .setPositiveButton("Sí, salir") { _, _ ->
                val prefs = getSharedPreferences("MisPreferencias", MODE_PRIVATE)
                prefs.edit().clear().apply()

                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onResume() {
        super.onResume()

        cargarProfesores()
    }

}