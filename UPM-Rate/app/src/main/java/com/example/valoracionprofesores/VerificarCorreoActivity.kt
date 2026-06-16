package com.example.valoracionprofesores

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
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
        supportActionBar?.hide() // Quitar barra superior
        setContentView(R.layout.activity_verificar_correo)

        // Vincular vistas
        etEmail = findViewById(R.id.etEmailVerificacion)
        etCodigo = findViewById(R.id.etCodigoSecreto)
        layoutCodigo = findViewById(R.id.layoutCodigo)
        btnEnviar = findViewById(R.id.btnEnviarCodigo)
        btnVerificar = findViewById(R.id.btnVerificarEntrar)

        // CLIC EN ENVIAR CÓDIGO
        btnEnviar.setOnClickListener {
            val email = etEmail.text.toString().trim()

            if (email.endsWith("@alumnos.upm.es") || email.endsWith("@upm.es")) {
                pedirCodigoAlServidor(email)
            } else {
                Toast.makeText(this, "Por favor, usa un correo de la UPM", Toast.LENGTH_SHORT).show()
            }
        }

        // CLIC EN VERIFICAR Y ENTRAR
        btnVerificar.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val codigo = etCodigo.text.toString().trim()

            if (codigo.length == 4) {
                comprobarCodigo(email, codigo)
            } else {
                Toast.makeText(this, "El código debe tener 4 números", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun pedirCodigoAlServidor(email: String) {
        // Desactivamos el botón para que no le den 20 veces
        btnEnviar.isEnabled = false
        btnEnviar.text = "Enviando..."

        val apiService = RetrofitClient.instance.create(ApiService::class.java)
        apiService.enviarCodigo(email).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@VerificarCorreoActivity, "Revisa tu correo (o la consola)", Toast.LENGTH_LONG).show()

                    // Mostramos la caja del código y el botón verde
                    layoutCodigo.visibility = View.VISIBLE
                    btnVerificar.visibility = View.VISIBLE

                    // Bloqueamos el email para que no lo cambien
                    etEmail.isEnabled = false
                    btnEnviar.visibility = View.GONE
                } else {
                    btnEnviar.isEnabled = true
                    btnEnviar.text = "Enviar Código"
                    Toast.makeText(this@VerificarCorreoActivity, "Error al enviar el correo", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                btnEnviar.isEnabled = true
                btnEnviar.text = "Enviar Código"
                Toast.makeText(this@VerificarCorreoActivity, "Fallo de conexión con el servidor", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun comprobarCodigo(email: String, codigo: String) {
        val apiService = RetrofitClient.instance.create(ApiService::class.java)
        apiService.verificarCodigo(email, codigo).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@VerificarCorreoActivity, "¡Bienvenido!", Toast.LENGTH_SHORT).show()

                    // Guardamos que el usuario ya está logueado en la memoria
                    val prefs = getSharedPreferences("MisPreferencias", MODE_PRIVATE)
                    prefs.edit().putString("EMAIL_USUARIO", email).apply()

                    // Viajamos a la pantalla principal
                    val intent = Intent(this@VerificarCorreoActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish() // Cerramos la pantalla de verificación
                } else {
                    Toast.makeText(this@VerificarCorreoActivity, "Código incorrecto", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@VerificarCorreoActivity, "Fallo de conexión", Toast.LENGTH_SHORT).show()
            }
        })
    }
}