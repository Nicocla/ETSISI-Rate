package com.example.valoracionprofesores

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class VerificarCorreoActivity : AppCompatActivity() {

    private lateinit var etEmail: TextInputEditText
    private lateinit var etCodigo: TextInputEditText
    private lateinit var layoutCodigo: TextInputLayout
    private lateinit var btnEnviar: Button
    private lateinit var btnVerificar: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_verificar_correo)

        etEmail = findViewById(R.id.etEmailVerificacion)
        etCodigo = findViewById(R.id.etCodigoSecreto)
        layoutCodigo = findViewById(R.id.layoutCodigo)
        btnEnviar = findViewById(R.id.btnEnviarCodigo)
        btnVerificar = findViewById(R.id.btnVerificarEntrar)

        btnEnviar.setOnClickListener {
            val email = etEmail.text.toString().trim()

            etEmail.error = null

            if (email.isEmpty()) {
                etEmail.error = "Introduce tu correo institucional"
                return@setOnClickListener
            }

            if (email.endsWith("@alumnos.upm.es") || email.endsWith("@upm.es")) {
                pedirCodigoAlServidor(email)
            } else {
                etEmail.error = "Usa un correo institucional de la UPM"
            }
        }

        btnVerificar.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val codigo = etCodigo.text.toString().trim()

            etCodigo.error = null

            if (codigo.length == 4) {
                comprobarCodigo(email, codigo)
            } else {
                etCodigo.error = "El código debe tener 4 números"
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

    private fun pedirCodigoAlServidor(email: String) {
        btnEnviar.isEnabled = false
        btnEnviar.text = "Enviando..."

        val apiService = RetrofitClient.instance.create(ApiService::class.java)

        apiService.enviarCodigo(email).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    layoutCodigo.visibility = View.VISIBLE
                    btnVerificar.visibility = View.VISIBLE

                    etEmail.isEnabled = false
                    btnEnviar.visibility = View.GONE
                } else {
                    btnEnviar.isEnabled = true
                    btnEnviar.text = "Enviar Código"

                    mostrarError(
                        "Error al enviar el código",
                        "No se ha podido enviar el código de verificación. Inténtalo de nuevo."
                    )
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                btnEnviar.isEnabled = true
                btnEnviar.text = "Enviar Código"

                mostrarError(
                    "Fallo de conexión",
                    "No se ha podido conectar con el servidor. Comprueba tu conexión e inténtalo de nuevo."
                )
            }
        })
    }

    private fun comprobarCodigo(email: String, codigo: String) {
        btnVerificar.isEnabled = false
        btnVerificar.text = "Verificando..."

        val apiService = RetrofitClient.instance.create(ApiService::class.java)

        apiService.verificarCodigo(email, codigo).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(
                call: Call<LoginResponse>,
                response: Response<LoginResponse>
            ) {
                btnVerificar.isEnabled = true
                btnVerificar.text = "Verificar y Entrar"

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

                    val intent = Intent(this@VerificarCorreoActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    etCodigo.error = "Código incorrecto"
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                btnVerificar.isEnabled = true
                btnVerificar.text = "Verificar y Entrar"

                mostrarError(
                    "Fallo de conexión",
                    "No se ha podido verificar el código porque no hay conexión con el servidor."
                )
            }
        })
    }
}