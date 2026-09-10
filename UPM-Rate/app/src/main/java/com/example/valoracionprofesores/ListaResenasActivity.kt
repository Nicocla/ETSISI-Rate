package com.example.valoracionprofesores

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ListaResenasActivity : AppCompatActivity() {

    private var profesorId: Int = 0
    private var esAdmin: Boolean = false

    private var listaResenas by mutableStateOf<List<Resena>>(emptyList())
    private var cargando by mutableStateOf(true)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        profesorId = intent.getIntExtra("PROFESOR_ID", 0)

        val prefs = getSharedPreferences("MisPreferencias", MODE_PRIVATE)
        esAdmin = prefs.getBoolean("ES_ADMIN", false)

        if (profesorId == 0) {
            mostrarErrorConCierre(
                "Error al cargar reseñas",
                "No se ha podido identificar el profesor seleccionado."
            )
            return
        }

        setContent {
            ETSISIRateTheme {
                ListaResenasScreen(
                    lista = listaResenas,
                    cargando = cargando,
                    esAdmin = esAdmin,
                    onVolver = { finish() },
                    onBorrarResena = { resena ->
                        borrarResenaAPI(resena)
                    }
                )
            }
        }

        cargarResenas()
    }

    private fun mostrarError(titulo: String, mensaje: String) {
        AlertDialog.Builder(this)
            .setTitle(titulo)
            .setMessage(mensaje)
            .setPositiveButton("Aceptar", null)
            .show()
    }

    private fun mostrarErrorConCierre(titulo: String, mensaje: String) {
        AlertDialog.Builder(this)
            .setTitle(titulo)
            .setMessage(mensaje)
            .setPositiveButton("Aceptar") { _, _ ->
                finish()
            }
            .show()
    }

    private fun cargarResenas() {
        cargando = true

        val api = RetrofitClient.instance.create(ApiService::class.java)

        api.obtenerResenas(profesorId).enqueue(object : Callback<List<Resena>> {

            override fun onResponse(
                call: Call<List<Resena>>,
                response: Response<List<Resena>>
            ) {
                cargando = false

                if (response.isSuccessful) {
                    listaResenas = response.body() ?: emptyList()
                } else {
                    mostrarError(
                        "Error al cargar reseñas",
                        "No se han podido obtener las reseñas. Código: ${response.code()}."
                    )
                }
            }

            override fun onFailure(call: Call<List<Resena>>, t: Throwable) {
                cargando = false

                mostrarError(
                    "Error de conexión",
                    "No se ha podido conectar con el servidor. Comprueba tu conexión e inténtalo de nuevo."
                )
            }
        })
    }

    private fun borrarResenaAPI(resena: Resena) {
        val api = RetrofitClient.instance.create(ApiService::class.java)

        api.borrarResena(profesorId, resena.email_alumno).enqueue(object : Callback<Void> {

            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    cargarResenas()
                } else {
                    mostrarError(
                        "Error al borrar",
                        "No se ha podido eliminar la reseña. Código: ${response.code()}."
                    )
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                mostrarError(
                    "Fallo de conexión",
                    "No se ha podido conectar con el servidor para eliminar la reseña."
                )
            }
        })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaResenasScreen(
    lista: List<Resena>,
    cargando: Boolean,
    esAdmin: Boolean,
    onVolver: () -> Unit,
    onBorrarResena: (Resena) -> Unit
) {
    var resenaSeleccionada by remember { mutableStateOf<Resena?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Reseñas del profesor",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    TextButton(onClick = onVolver) {
                        Text(
                            text = "Volver",
                            color = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF2196F3),
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->

        if (cargando) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(
                    color = Color(0xFF2196F3)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Cargando reseñas...",
                    color = Color.DarkGray
                )
            }
        } else if (lista.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Aún no hay reseñas",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Cuando los alumnos valoren a este profesor, sus comentarios aparecerán aquí.",
                    color = Color.DarkGray
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(12.dp)
            ) {
                items(lista) { resena ->
                    ResenaCard(
                        resena = resena,
                        esAdmin = esAdmin,
                        onBorrarClick = {
                            resenaSeleccionada = resena
                        }
                    )
                }
            }
        }
    }

    if (resenaSeleccionada != null) {
        ComposeAlertDialog(
            onDismissRequest = {
                resenaSeleccionada = null
            },
            title = {
                Text("Moderar comentario")
            },
            text = {
                Text("¿Estás seguro de que quieres borrar esta reseña?\nEsta acción es irreversible.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        resenaSeleccionada?.let { onBorrarResena(it) }
                        resenaSeleccionada = null
                    }
                ) {
                    Text("Borrar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        resenaSeleccionada = null
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun ResenaCard(
    resena: Resena,
    esAdmin: Boolean,
    onBorrarClick: () -> Unit
) {
    val puntuacionSobre5 = (resena.puntuacion.toFloat() / 2f).coerceIn(0f, 5f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
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
                text = "Alumno anónimo",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2196F3)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = obtenerEstrellas(puntuacionSobre5),
                    fontSize = 18.sp,
                    color = Color(0xFFFFC107)
                )

                Spacer(modifier = Modifier.padding(horizontal = 6.dp))

                Text(
                    text = "%.1f/5".format(puntuacionSobre5),
                    fontSize = 13.sp,
                    color = Color.DarkGray
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = resena.comentario ?: "Sin comentario",
                fontSize = 14.sp,
                color = Color(0xFF555555)
            )

            if (esAdmin) {
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = onBorrarClick,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFFD32F2F)
                    ),
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Eliminar")
                }
            }
        }
    }
}