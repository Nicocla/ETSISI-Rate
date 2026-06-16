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

class LoginActivity : AppCompatActivity() {

    private lateinit var etEmail: TextInputEditText
    private lateinit var etCodigo: TextInputEditText
    private lateinit var layoutCodigo: TextInputLayout
    private lateinit var btnEnviar: Button
    private lateinit var btnVerificar: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()

        // Usamos el diseño que creamos en el mensaje anterior
        setContentView(R.layout.activity_verificar_correo)

        // 1. COMPROBAR SI YA ESTÁ LOGUEADO
        // Si ya inició sesión antes, lo mandamos directo a la app sin pedir código
        val prefs = getSharedPreferences("MisPreferencias", MODE_PRIVATE)
        val emailGuardado = prefs.getString("EMAIL_USUARIO", null)
        if (emailGuardado != null) {
            irAMainActivity()
            return // Detiene la ejecución de esta pantalla
        }

        // 2. VINCULAR VISTAS
        etEmail = findViewById(R.id.etEmailVerificacion)
        etCodigo = findViewById(R.id.etCodigoSecreto)
        layoutCodigo = findViewById(R.id.layoutCodigo)
        btnEnviar = findViewById(R.id.btnEnviarCodigo)
        btnVerificar = findViewById(R.id.btnVerificarEntrar)

        // 3. CLIC EN ENVIAR CÓDIGO
        btnEnviar.setOnClickListener {
            val email = etEmail.text.toString().trim()

            if (email.endsWith("@alumnos.upm.es") || email.endsWith("@upm.es") || email == "admin") {
                pedirCodigoAlServidor(email)
            } else {
                Toast.makeText(this, "Usa un correo de la UPM", Toast.LENGTH_SHORT).show()
            }
        }

        // 4. CLIC EN VERIFICAR Y ENTRAR
        btnVerificar.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val codigo = etCodigo.text.toString().trim()

            if (codigo.length == 4) {
                comprobarCodigo(email, codigo)
            } else {
                Toast.makeText(this, "El código es de 4 números", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun pedirCodigoAlServidor(email: String) {
        btnEnviar.isEnabled = false
        btnEnviar.text = "Enviando..."

        val apiService = RetrofitClient.instance.create(ApiService::class.java)
        apiService.enviarCodigo(email).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@LoginActivity, "Código enviado", Toast.LENGTH_SHORT).show()

                    layoutCodigo.visibility = View.VISIBLE
                    btnVerificar.visibility = View.VISIBLE

                    etEmail.isEnabled = false
                    btnEnviar.visibility = View.GONE
                } else {
                    btnEnviar.isEnabled = true
                    btnEnviar.text = "Enviar Código"
                    Toast.makeText(this@LoginActivity, "Error al enviar el correo", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                btnEnviar.isEnabled = true
                btnEnviar.text = "Enviar Código"
                Toast.makeText(this@LoginActivity, "Fallo de conexión", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun comprobarCodigo(email: String, codigo: String) {
        val apiService = RetrofitClient.instance.create(ApiService::class.java)
        apiService.verificarCodigo(email, codigo).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    // GUARDAR SESIÓN Y ROL
                    val prefs = getSharedPreferences("MisPreferencias", MODE_PRIVATE)
                    val editor = prefs.edit()
                    editor.putString("EMAIL_USUARIO", email)

                    // Lógica sencilla: si el email contiene "admin", es administrador
                    if (email.contains("admin")) {
                        editor.putBoolean("ES_ADMIN", true)
                    } else {
                        editor.putBoolean("ES_ADMIN", false)
                    }
                    editor.apply()

                    irAMainActivity()
                } else {
                    Toast.makeText(this@LoginActivity, "Código incorrecto", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@LoginActivity, "Fallo de conexión", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun irAMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}