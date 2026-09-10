package com.example.valoracionprofesores

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AddProfesorActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()

        setContent {
            ETSISIRateTheme {
                AddProfesorScreen(
                    onGuardarProfesor = { nombre, departamento, finalizarCarga ->
                        guardarEnServidor(nombre, departamento, finalizarCarga)
                    }
                )
            }
        }
    }

    private fun mostrarError(titulo: String, mensaje: String) {
        AlertDialog.Builder(this)
            .setTitle(titulo)
            .setMessage(mensaje)
            .setPositiveButton("Aceptar", null)
            .show()
    }

    private fun guardarEnServidor(
        nombre: String,
        departamento: String,
        finalizarCarga: () -> Unit
    ) {
        val apiService = RetrofitClient.instance.create(ApiService::class.java)

        apiService.anadirProfesor(nombre, departamento).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                finalizarCarga()

                if (response.isSuccessful) {
                    finish()
                } else {
                    mostrarError(
                        "Error del servidor",
                        "No se ha podido añadir el profesor. Código: ${response.code()}."
                    )
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                finalizarCarga()

                mostrarError(
                    "Fallo de conexión",
                    "No se ha podido conectar con el servidor. Comprueba la conexión e inténtalo de nuevo."
                )
            }
        })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProfesorScreen(
    onGuardarProfesor: (
        nombre: String,
        departamento: String,
        finalizarCarga: () -> Unit
    ) -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var departamento by remember { mutableStateOf("") }

    var errorNombre by remember { mutableStateOf<String?>(null) }
    var errorDepartamento by remember { mutableStateOf<String?>(null) }

    var cargando by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Añadir profesor",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF2196F3),
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Nuevo profesor",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Introduce los datos del profesor que quieres registrar en ETSISI Rate.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.DarkGray,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = nombre,
                onValueChange = {
                    nombre = it
                    errorNombre = null
                },
                label = { Text("Nombre del profesor") },
                isError = errorNombre != null,
                supportingText = {
                    if (errorNombre != null) {
                        Text(errorNombre ?: "")
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = departamento,
                onValueChange = {
                    departamento = it
                    errorDepartamento = null
                },
                label = { Text("Departamento") },
                isError = errorDepartamento != null,
                supportingText = {
                    if (errorDepartamento != null) {
                        Text(errorDepartamento ?: "")
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = {
                    val nombreLimpio = nombre.trim()
                    val departamentoLimpio = departamento.trim()

                    var valido = true

                    if (nombreLimpio.isEmpty()) {
                        errorNombre = "Introduce el nombre del profesor"
                        valido = false
                    }

                    if (departamentoLimpio.isEmpty()) {
                        errorDepartamento = "Introduce el departamento"
                        valido = false
                    }

                    if (valido) {
                        cargando = true

                        onGuardarProfesor(
                            nombreLimpio,
                            departamentoLimpio
                        ) {
                            cargando = false
                        }
                    }
                },
                enabled = !cargando,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2196F3),
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                if (cargando) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Guardar profesor")
                }
            }
        }
    }
}
