package com.example.valoracionprofesores

import android.content.Intent
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()

        setContent {
            ETSISIRateTheme {
                LoginScreen(
                    onEnviarCodigo = { email, finalizarCarga, mostrarCampoCodigo ->
                        pedirCodigoAlServidor(email, finalizarCarga, mostrarCampoCodigo)
                    },
                    onVerificarCodigo = { email, codigo, finalizarCarga, mostrarErrorCodigo ->
                        comprobarCodigo(email, codigo, finalizarCarga, mostrarErrorCodigo)
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

    private fun pedirCodigoAlServidor(
        email: String,
        finalizarCarga: () -> Unit,
        mostrarCampoCodigo: () -> Unit
    ) {
        val apiService = RetrofitClient.instance.create(ApiService::class.java)

        apiService.enviarCodigo(email).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                finalizarCarga()

                if (response.isSuccessful) {
                    mostrarCampoCodigo()
                } else {
                    mostrarError(
                        "Error al enviar el código",
                        "No se ha podido enviar el código de verificación. Código: ${response.code()}."
                    )
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                finalizarCarga()

                mostrarError(
                    "Fallo de conexión",
                    "No se ha podido conectar con el servidor. Comprueba tu conexión e inténtalo de nuevo."
                )
            }
        })
    }

    private fun comprobarCodigo(
        email: String,
        codigo: String,
        finalizarCarga: () -> Unit,
        mostrarErrorCodigo: () -> Unit
    ) {
        val apiService = RetrofitClient.instance.create(ApiService::class.java)

        apiService.verificarCodigo(email, codigo).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                finalizarCarga()

                if (response.isSuccessful) {
                    val loginResponse = response.body()
                    val token = loginResponse?.token

                    if (token.isNullOrEmpty()) {
                        mostrarError(
                            "Error de sesión",
                            "El servidor no ha devuelto un token de acceso."
                        )
                        return
                    }

                    val prefs = getSharedPreferences("MisPreferencias", MODE_PRIVATE)

                    prefs.edit()
                        .putString("EMAIL_USUARIO", email)
                        .putString("TOKEN_USUARIO", token)
                        .putBoolean("ES_ADMIN", loginResponse.esAdmin)
                        .apply()

                    irAMainActivity()
                } else {
                    mostrarErrorCodigo()
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                finalizarCarga()

                mostrarError(
                    "Fallo de conexión",
                    "No se ha podido verificar el código porque no hay conexión con el servidor."
                )
            }
        })
    }

    private fun irAMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}

@Composable
fun LoginScreen(
    onEnviarCodigo: (
        email: String,
        finalizarCarga: () -> Unit,
        mostrarCampoCodigo: () -> Unit
    ) -> Unit,
    onVerificarCodigo: (
        email: String,
        codigo: String,
        finalizarCarga: () -> Unit,
        mostrarErrorCodigo: () -> Unit
    ) -> Unit
) {
    val azulPrincipal = Color(0xFF2196F3)
    val verdeVerificar = Color(0xFF4CAF50)

    var email by remember { mutableStateOf("") }
    var codigo by remember { mutableStateOf("") }

    var errorEmail by remember { mutableStateOf<String?>(null) }
    var errorCodigo by remember { mutableStateOf<String?>(null) }

    var codigoVisible by remember { mutableStateOf(false) }
    var emailBloqueado by remember { mutableStateOf(false) }

    var enviandoCodigo by remember { mutableStateOf(false) }
    var verificandoCodigo by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Verifica tu correo",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Usa tu correo @alumnos.upm.es o @upm.es para acceder a ETSISI Rate.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.DarkGray,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                errorEmail = null
            },
            enabled = !emailBloqueado,
            label = { Text("Correo universitario") },
            isError = errorEmail != null,
            supportingText = {
                if (errorEmail != null) {
                    Text(errorEmail ?: "")
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = azulPrincipal,
                focusedLabelColor = azulPrincipal
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (!codigoVisible) {
            Button(
                onClick = {
                    val emailLimpio = email.trim()
                    errorEmail = null

                    if (emailLimpio.isEmpty()) {
                        errorEmail = "Introduce tu correo institucional"
                        return@Button
                    }

                    val emailValido =
                        emailLimpio.endsWith("@alumnos.upm.es") ||
                                emailLimpio.endsWith("@upm.es") ||
                                emailLimpio == "admin"

                    if (!emailValido) {
                        errorEmail = "Usa un correo institucional de la UPM"
                        return@Button
                    }

                    enviandoCodigo = true

                    onEnviarCodigo(
                        emailLimpio,
                        {
                            enviandoCodigo = false
                        },
                        {
                            codigoVisible = true
                            emailBloqueado = true
                        }
                    )
                },
                enabled = !enviandoCodigo,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = azulPrincipal,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp)
            ) {
                if (enviandoCodigo) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(22.dp)
                    )
                } else {
                    Text("Enviar código")
                }
            }
        }

        if (codigoVisible) {
            OutlinedTextField(
                value = codigo,
                onValueChange = {
                    if (it.length <= 4) {
                        codigo = it
                        errorCodigo = null
                    }
                },
                label = { Text("Código de 4 cifras") },
                isError = errorCodigo != null,
                supportingText = {
                    if (errorCodigo != null) {
                        Text(errorCodigo ?: "")
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = azulPrincipal,
                    focusedLabelColor = azulPrincipal
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val codigoLimpio = codigo.trim()
                    errorCodigo = null

                    if (codigoLimpio.length != 4) {
                        errorCodigo = "El código debe tener 4 números"
                        return@Button
                    }

                    verificandoCodigo = true

                    onVerificarCodigo(
                        email.trim(),
                        codigoLimpio,
                        {
                            verificandoCodigo = false
                        },
                        {
                            errorCodigo = "Código incorrecto"
                        }
                    )
                },
                enabled = !verificandoCodigo,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = verdeVerificar,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp)
            ) {
                if (verificandoCodigo) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(22.dp)
                    )
                } else {
                    Text("Verificar y entrar")
                }
            }
        }
    }
}