package com.example.valoracionprofesores

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// --- IMPORTS PARA LA GRÁFICA DE RADAR ---
import com.github.mikephil.charting.charts.RadarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.RadarData
import com.github.mikephil.charting.data.RadarDataSet
import com.github.mikephil.charting.data.RadarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

class DetalleProfesorActivity : AppCompatActivity() {

    // Referencias a los 5 bloques desplegables
    private lateinit var incHorario: View
    private lateinit var incMaterial: View
    private lateinit var incAtencion: View
    private lateinit var incTutorias: View
    private lateinit var incGuia: View

    // Referencias a elementos de la UI
    private lateinit var etComentario: EditText
    private lateinit var btnEnviar: Button
    private lateinit var btnVerResenas: Button

    // Referencia a la nueva Gráfica
    private lateinit var radarChart: RadarChart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle_profesor)

        // 1. RECUPERAR DATOS DEL INTENT
        val profesor = intent.getSerializableExtra("profesor") as? Profesor
        if (profesor == null) {
            Toast.makeText(this, "Error al cargar profesor", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // 2. VINCULAR VISTAS
        radarChart = findViewById(R.id.radarChart) // La nueva gráfica
        val tvNombre = findViewById<TextView>(R.id.tvDetalleNombre)
        val tvDept = findViewById<TextView>(R.id.tvDetalleDept)

        incHorario = findViewById(R.id.incHorario)
        incMaterial = findViewById(R.id.incMaterial)
        incAtencion = findViewById(R.id.incAtencion)
        incTutorias = findViewById(R.id.incTutorias)
        incGuia = findViewById(R.id.incGuia)

        etComentario = findViewById(R.id.etComentario)
        btnEnviar = findViewById(R.id.btnEnviar)
        btnVerResenas = findViewById(R.id.btnVerResenas)

        // 3. PINTAR TEXTOS BÁSICOS
        tvNombre.text = profesor.nombre
        tvDept.text = profesor.departamento

        // 4. CONFIGURAR LOS DESPLEGABLES (TEXTOS Y CLICS)
        configurarAcordeon(incHorario, "Seguimiento Horario", "Puntualidad y cumplimiento de horas.")
        configurarAcordeon(incMaterial, "Material Extra", "Diapositivas, charlas, actividades prácticas...")
        configurarAcordeon(incAtencion, "Atención al Alumno", "Responde dudas en clase y descansos.")
        configurarAcordeon(incTutorias, "Cumplimiento Tutorías", "Respeta los horarios de tutoría.")
        configurarAcordeon(incGuia, "Guía Docente", "Sigue el método de calificación oficial.")

        // 5. INICIALIZAR GRÁFICA Y CARGAR DATOS
        setupRadarChart()           // Configuración visual (colores, ejes)
        cargarDatosRadar(profesor.id) // Petición a la API

        // 6. CARGAR MI VOTO ANTERIOR (SI EXISTE)
        cargarMiValoracion(profesor.id)

        // 7. LISTENERS DE BOTONES
        btnVerResenas.setOnClickListener {
            val intent = Intent(this, ListaResenasActivity::class.java)
            intent.putExtra("PROFESOR_ID", profesor.id)
            startActivity(intent)
        }

        btnEnviar.setOnClickListener {
            // Recoger notas de los acordeones
            val n1 = obtenerNota(incHorario)
            val n2 = obtenerNota(incMaterial)
            val n3 = obtenerNota(incAtencion)
            val n4 = obtenerNota(incTutorias)
            val n5 = obtenerNota(incGuia)
            val comentario = etComentario.text.toString()

            // Validación simple: Todas las notas deben ser mayor que 0
            if (n1 == 0f || n2 == 0f || n3 == 0f || n4 == 0f || n5 == 0f) {
                Toast.makeText(this, "Por favor, puntúa todos los apartados", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            enviarValoracion(profesor.id, comentario, n1, n2, n3, n4, n5)
        }
    }

    // ==========================================
    //      ZONA GRÁFICA RADAR (ARAÑA) 🕸️
    // ==========================================

    private fun setupRadarChart() {
        radarChart.description.isEnabled = false

        // Estilo de la telaraña
        radarChart.webLineWidth = 1f
        radarChart.webColor = Color.LTGRAY
        radarChart.webLineWidthInner = 1f
        radarChart.webColorInner = Color.LTGRAY
        radarChart.webAlpha = 100

        // Configurar EJE X (Etiquetas de las esquinas)
        val xAxis = radarChart.xAxis
        xAxis.textSize = 9f
        xAxis.yOffset = 0f
        xAxis.xOffset = 0f
        xAxis.textColor = Color.DKGRAY
        xAxis.valueFormatter = IndexAxisValueFormatter(arrayOf("Horario", "Material", "Atención", "Tutorías", "Guía"))

        // Configurar EJE Y (Los anillos concéntricos)
        val yAxis = radarChart.yAxis
        yAxis.setLabelCount(5, true)
        yAxis.textSize = 9f
        yAxis.axisMinimum = 0f
        yAxis.axisMaximum = 10f
        yAxis.setDrawLabels(false)
    }

    private fun cargarDatosRadar(idProfesor: Int) {
        val api = RetrofitClient.instance.create(ApiService::class.java)
        api.obtenerDatosRadar(idProfesor).enqueue(object : Callback<RadarResponse> {
            override fun onResponse(call: Call<RadarResponse>, response: Response<RadarResponse>) {
                if (response.isSuccessful) {
                    val datos = response.body()
                    if (datos != null) {
                        pintarRadar(datos)
                    }
                }
            }
            override fun onFailure(call: Call<RadarResponse>, t: Throwable) {}
        })
    }

    private fun pintarRadar(datos: RadarResponse) {
        val entradas = ArrayList<RadarEntry>()
        entradas.add(RadarEntry(datos.avg_horario))
        entradas.add(RadarEntry(datos.avg_material))
        entradas.add(RadarEntry(datos.avg_atencion))
        entradas.add(RadarEntry(datos.avg_tutorias))
        entradas.add(RadarEntry(datos.avg_guia))

        val dataSet = RadarDataSet(entradas, "Competencias")

        // Colores y Estilo
        val colorAzul = Color.rgb(33, 150, 243)
        dataSet.color = colorAzul
        dataSet.fillColor = colorAzul
        dataSet.setDrawFilled(true)
        dataSet.fillAlpha = 100
        dataSet.lineWidth = 2f
        dataSet.isDrawHighlightCircleEnabled = true
        dataSet.setDrawHighlightIndicators(false)

        val data = RadarData(dataSet)
        data.setValueTextSize(12f)
        data.setDrawValues(true)
        data.setValueTextColor(Color.DKGRAY)

        radarChart.data = data
        radarChart.invalidate()
        radarChart.animateXY(1400, 1400)
    }

    // ==========================================
    //      ZONA LÓGICA ACORDEÓN Y ENVÍO
    // ==========================================

    private fun configurarAcordeon(vista: View, titulo: String, desc: String) {
        val tvTitulo = vista.findViewById<TextView>(R.id.tvTituloAcordeon)
        val tvDesc = vista.findViewById<TextView>(R.id.tvDescripcion)
        val layoutContenido = vista.findViewById<LinearLayout>(R.id.layoutContenido)

        tvTitulo.text = "$titulo ▾"
        tvDesc.text = desc

        // Al pulsar, abrir o cerrar
        vista.setOnClickListener {
            if (layoutContenido.visibility == View.VISIBLE) {
                layoutContenido.visibility = View.GONE
                tvTitulo.text = "$titulo ▾"
            } else {
                layoutContenido.visibility = View.VISIBLE
                tvTitulo.text = "$titulo ▴"
            }
        }
    }

    private fun obtenerNota(vista: View): Float {
        val ratingBar = vista.findViewById<RatingBar>(R.id.rbCriterio)
        return (ratingBar?.rating ?: 0f) *2
    }

    private fun cargarMiValoracion(idProfesor: Int) {
        val prefs = getSharedPreferences("MisPreferencias", MODE_PRIVATE)
        // 🛠️ CORRECCIÓN 1: Usar EMAIL_USUARIO
        val email = prefs.getString("EMAIL_USUARIO", "") ?: ""

        if (email.isEmpty()) return

        val api = RetrofitClient.instance.create(ApiService::class.java)
        api.obtenerMiValoracion(idProfesor, email).enqueue(object : Callback<MiValoracionResponse?> {
            override fun onResponse(call: Call<MiValoracionResponse?>, response: Response<MiValoracionResponse?>) {
                val voto = response.body()
                if (voto != null) {
                    // Rellenar estrellas con lo que voté en el pasado
                    incHorario.findViewById<RatingBar>(R.id.rbCriterio).rating = voto.nota_horario
                    incMaterial.findViewById<RatingBar>(R.id.rbCriterio).rating = voto.nota_material
                    incAtencion.findViewById<RatingBar>(R.id.rbCriterio).rating = voto.nota_atencion
                    incTutorias.findViewById<RatingBar>(R.id.rbCriterio).rating = voto.nota_tutorias
                    incGuia.findViewById<RatingBar>(R.id.rbCriterio).rating = voto.nota_guia

                    etComentario.setText(voto.comentario)
                    btnEnviar.text = "ACTUALIZAR VALORACIÓN"
                }
            }
            override fun onFailure(call: Call<MiValoracionResponse?>, t: Throwable) {}
        })
    }

    private fun enviarValoracion(profeId: Int, coment: String, n1: Float, n2: Float, n3: Float, n4: Float, n5: Float) {
        val prefs = getSharedPreferences("MisPreferencias", MODE_PRIVATE)
        // 🛠️ CORRECCIÓN 2 y 3: Usar EMAIL_USUARIO y quitar el "anonimo" por defecto
        val email = prefs.getString("EMAIL_USUARIO", "") ?: ""

        if (email.isEmpty()) {
            Toast.makeText(this, "Error: No se encontró tu correo", Toast.LENGTH_SHORT).show()
            return
        }

        val api = RetrofitClient.instance.create(ApiService::class.java)
        api.insertarValoracion(profeId, email, coment, n1, n2, n3, n4, n5).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if(response.isSuccessful) {
                    Toast.makeText(this@DetalleProfesorActivity, "¡Valoración guardada!", Toast.LENGTH_SHORT).show()
                    cargarDatosRadar(profeId) // Refrescar gráfica inmediatamente
                    btnEnviar.text = "ACTUALIZAR VALORACIÓN"
                } else {
                    Toast.makeText(this@DetalleProfesorActivity, "Error al guardar", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@DetalleProfesorActivity, "Error de conexión", Toast.LENGTH_SHORT).show()
            }
        })
    }
}