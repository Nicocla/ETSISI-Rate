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
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MisResenasActivity : AppCompatActivity() {

    private lateinit var emailAlumno: String

    private var listaResenas by mutableStateOf<List<MiResenaItem>>(emptyList())
    private var cargando by mutableStateOf(true)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("MisPreferencias", MODE_PRIVATE)
        emailAlumno = prefs.getString("EMAIL_USUARIO", "") ?: ""

        if (emailAlumno.isEmpty()) {
            mostrarErrorConCierre(
                "Sesión no encontrada",
                "No se ha encontrado tu correo. Vuelve a iniciar sesión."
            )
            return
        }

        setContent {
            ETSISIRateTheme {
                MisResenasScreen(
                    lista = listaResenas,
                    cargando = cargando,
                    onVolver = { finish() },
                    onBorrarResena = { resena ->
                        borrarResenaDeVerdad(resena)
                    }
                )
            }
        }

        cargarMisResenas()
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

    private fun cargarMisResenas() {
        cargando = true

        val api = RetrofitClient.instance.create(ApiService::class.java)

        api.obtenerMisResenas(emailAlumno).enqueue(object : Callback<List<MiResenaItem>> {

            override fun onResponse(
                call: Call<List<MiResenaItem>>,
                response: Response<List<MiResenaItem>>
            ) {
                cargando = false

                if (response.isSuccessful) {
                    listaResenas = response.body() ?: emptyList()
                } else {
                    mostrarError(
                        "Error al cargar reseñas",
                        "No se han podido obtener tus reseñas. Código: ${response.code()}."
                    )
                }
            }

            override fun onFailure(call: Call<List<MiResenaItem>>, t: Throwable) {
                cargando = false

                mostrarError(
                    "Error de conexión",
                    "No se ha podido conectar con el servidor. Comprueba tu conexión e inténtalo de nuevo."
                )
            }
        })
    }

    private fun borrarResenaDeVerdad(item: MiResenaItem) {
        val api = RetrofitClient.instance.create(ApiService::class.java)

        api.borrarResena(item.profesor_id, emailAlumno).enqueue(object : Callback<Void> {

            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    cargarMisResenas()
                } else {
                    mostrarError(
                        "Error al borrar",
                        "No se ha podido eliminar la reseña. Código: ${response.code()}."
                    )
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                mostrarError(
                    "Fallo de red",
                    "No se ha podido conectar con el servidor para eliminar la reseña."
                )
            }
        })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MisResenasScreen(
    lista: List<MiResenaItem>,
    cargando: Boolean,
    onVolver: () -> Unit,
    onBorrarResena: (MiResenaItem) -> Unit
) {
    var resenaSeleccionada by remember { mutableStateOf<MiResenaItem?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Mis reseñas",
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
                    text = "Cargando tus reseñas...",
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
                    text = "Todavía no has publicado ninguna valoración",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Cuando valores a un profesor, aparecerá aquí tu historial.",
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
                    MiResenaCard(
                        resena = resena,
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
                Text("Borrar valoración")
            },
            text = {
                Text("¿Quieres eliminar tu reseña de ${resenaSeleccionada?.nombre_profesor}?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        resenaSeleccionada?.let { onBorrarResena(it) }
                        resenaSeleccionada = null
                    }
                ) {
                    Text("Sí, borrar")
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
fun MiResenaCard(
    resena: MiResenaItem,
    onBorrarClick: () -> Unit
) {
    val puntuacionSobre5 = resena.puntuacion / 2f

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
                text = resena.nombre_profesor,
                fontSize = 18.sp,
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

fun obtenerEstrellas(valor: Float): String {
    val valorRedondeado = kotlin.math.round(valor).toInt()

    return buildString {
        repeat(valorRedondeado) {
            append("★")
        }

        repeat(5 - valorRedondeado) {
            append("☆")
        }
    }
}