package com.example.valoracionprofesores

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog as ComposeAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private var listaProfesores by mutableStateOf<List<Profesor>>(emptyList())
    private var cargando by mutableStateOf(true)
    private var esAdmin: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()

        val prefs = getSharedPreferences("MisPreferencias", MODE_PRIVATE)
        esAdmin = prefs.getBoolean("ES_ADMIN", false)

        setContent {
            ETSISIRateTheme {
                MainScreen(
                    lista = listaProfesores,
                    cargando = cargando,
                    esAdmin = esAdmin,
                    onCerrarSesion = {
                        cerrarSesion()
                    },
                    onMisResenas = {
                        val intent = Intent(this, MisResenasActivity::class.java)
                        startActivity(intent)
                    },
                    onAddProfesor = {
                        val intent = Intent(this, AddProfesorActivity::class.java)
                        startActivity(intent)
                    },
                    onProfesorClick = { profesor ->
                        val intent = Intent(this, DetalleProfesorActivity::class.java)
                        intent.putExtra("profesor", profesor)
                        startActivity(intent)
                    },
                    onBorrarProfesor = { profesor ->
                        borrarProfesor(profesor)
                    }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        cargarProfesores()
    }

    private fun mostrarError(titulo: String, mensaje: String) {
        AlertDialog.Builder(this)
            .setTitle(titulo)
            .setMessage(mensaje)
            .setPositiveButton("Aceptar", null)
            .show()
    }

    private fun cargarProfesores() {
        cargando = true

        val apiService = RetrofitClient.instance.create(ApiService::class.java)

        apiService.obtenerProfesores().enqueue(object : Callback<List<Profesor>> {
            override fun onResponse(
                call: Call<List<Profesor>>,
                response: Response<List<Profesor>>
            ) {
                cargando = false

                if (response.isSuccessful) {
                    listaProfesores = response.body() ?: emptyList()
                } else {
                    mostrarError(
                        "Error al cargar profesores",
                        "No se ha podido obtener la lista de profesores. Código: ${response.code()}."
                    )
                }
            }

            override fun onFailure(call: Call<List<Profesor>>, t: Throwable) {
                cargando = false

                mostrarError(
                    "Error de conexión",
                    "No se ha podido conectar con el servidor. Comprueba tu conexión e inténtalo de nuevo."
                )
            }
        })
    }

    private fun borrarProfesor(profesor: Profesor) {
        val apiService = RetrofitClient.instance.create(ApiService::class.java)

        apiService.borrarProfesor(profesor.id).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    cargarProfesores()
                } else {
                    mostrarError(
                        "Error al borrar",
                        "No se ha podido borrar el profesor. Código: ${response.code()}."
                    )
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                mostrarError(
                    "Fallo de conexión",
                    "No se ha podido conectar con el servidor para borrar el profesor."
                )
            }
        })
    }

    private fun cerrarSesion() {
        AlertDialog.Builder(this)
            .setTitle("Cerrar sesión")
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    lista: List<Profesor>,
    cargando: Boolean,
    esAdmin: Boolean,
    onCerrarSesion: () -> Unit,
    onMisResenas: () -> Unit,
    onAddProfesor: () -> Unit,
    onProfesorClick: (Profesor) -> Unit,
    onBorrarProfesor: (Profesor) -> Unit
) {
    var textoBuscado by remember { mutableStateOf("") }
    var profesorSeleccionado by remember { mutableStateOf<Profesor?>(null) }

    val azulPrincipal = Color(0xFF2196F3)

    val listaFiltrada = lista.filter { profesor ->
        profesor.nombre.lowercase().contains(textoBuscado.lowercase().trim()) ||
                (profesor.departamento ?: "").lowercase().contains(textoBuscado.lowercase().trim())
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "ETSISI Rate",
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    TextButton(onClick = onCerrarSesion) {
                        Text(
                            text = "Salir",
                            color = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = azulPrincipal,
                    titleContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            if (esAdmin) {
                FloatingActionButton(
                    onClick = onAddProfesor,
                    containerColor = azulPrincipal,
                    contentColor = Color.White
                ) {
                    Text("+")
                }
            }
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            if (!esAdmin) {
                Button(
                    onClick = onMisResenas,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = azulPrincipal,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text("Mis reseñas")
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            OutlinedTextField(
                value = textoBuscado,
                onValueChange = {
                    textoBuscado = it
                },
                label = {
                    Text("Buscar profesor o departamento")
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = azulPrincipal,
                    focusedLabelColor = azulPrincipal
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            when {
                cargando -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = azulPrincipal
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Cargando profesores...",
                            color = Color.DarkGray
                        )
                    }
                }

                listaFiltrada.isEmpty() -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No hay profesores para mostrar",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF333333)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Prueba con otra búsqueda o añade un profesor si eres administrador.",
                            color = Color.DarkGray
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(listaFiltrada) { profesor ->
                            ProfesorCard(
                                profesor = profesor,
                                esAdmin = esAdmin,
                                onClick = {
                                    onProfesorClick(profesor)
                                },
                                onBorrarClick = {
                                    profesorSeleccionado = profesor
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (profesorSeleccionado != null) {
        ComposeAlertDialog(
            onDismissRequest = {
                profesorSeleccionado = null
            },
            title = {
                Text("Borrar profesor")
            },
            text = {
                Text(
                    "¿Estás seguro de que quieres borrar a ${profesorSeleccionado?.nombre}? " +
                            "Esta acción no se puede deshacer."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        profesorSeleccionado?.let { onBorrarProfesor(it) }
                        profesorSeleccionado = null
                    }
                ) {
                    Text("Sí, borrar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        profesorSeleccionado = null
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun ProfesorCard(
    profesor: Profesor,
    esAdmin: Boolean,
    onClick: () -> Unit,
    onBorrarClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable {
                onClick()
            },
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = profesor.nombre,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2196F3)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = profesor.departamento ?: "Sin departamento",
                fontSize = 14.sp,
                color = Color.DarkGray
            )

            if (esAdmin) {
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onBorrarClick,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFD32F2F)
                        )
                    ) {
                        Text("Eliminar")
                    }
                }
            }
        }
    }
}