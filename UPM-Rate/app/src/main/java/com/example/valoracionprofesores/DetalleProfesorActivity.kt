package com.example.valoracionprofesores

import android.content.Intent
import android.graphics.Color as AndroidColor
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.RadarChart
import com.github.mikephil.charting.data.RadarData
import com.github.mikephil.charting.data.RadarDataSet
import com.github.mikephil.charting.data.RadarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.math.roundToInt

class DetalleProfesorActivity : AppCompatActivity() {

    private var datosRadar by mutableStateOf<RadarResponse?>(null)

    private var notaHorario by mutableIntStateOf(0)
    private var notaMaterial by mutableIntStateOf(0)
    private var notaAtencion by mutableIntStateOf(0)
    private var notaTutorias by mutableIntStateOf(0)
    private var notaGuia by mutableIntStateOf(0)

    private var comentario by mutableStateOf("")
    private var guardando by mutableStateOf(false)
    private var yaValorado by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()

        val profesor = intent.getSerializableExtra("profesor") as? Profesor

        if (profesor == null) {
            mostrarErrorConCierre(
                "Error al cargar profesor",
                "No se ha podido obtener la información del profesor seleccionado."
            )
            return
        }

        setContent {
            ETSISIRateTheme {
                DetalleProfesorScreen(
                    profesor = profesor,
                    datosRadar = datosRadar,
                    notaHorario = notaHorario,
                    notaMaterial = notaMaterial,
                    notaAtencion = notaAtencion,
                    notaTutorias = notaTutorias,
                    notaGuia = notaGuia,
                    comentario = comentario,
                    guardando = guardando,
                    yaValorado = yaValorado,
                    onVolver = { finish() },
                    onVerResenas = {
                        val intent = Intent(this, ListaResenasActivity::class.java)
                        intent.putExtra("PROFESOR_ID", profesor.id)
                        startActivity(intent)
                    },
                    onNotaHorarioChange = { notaHorario = it },
                    onNotaMaterialChange = { notaMaterial = it },
                    onNotaAtencionChange = { notaAtencion = it },
                    onNotaTutoriasChange = { notaTutorias = it },
                    onNotaGuiaChange = { notaGuia = it },
                    onComentarioChange = { comentario = it },
                    onEnviarValoracion = {
                        enviarValoracion(
                            profeId = profesor.id,
                            coment = comentario,
                            n1 = notaHorario * 2f,
                            n2 = notaMaterial * 2f,
                            n3 = notaAtencion * 2f,
                            n4 = notaTutorias * 2f,
                            n5 = notaGuia * 2f
                        )
                    }
                )
            }
        }

        cargarDatosRadar(profesor.id)
        cargarMiValoracion(profesor.id)
    }

    private fun mostrarError(titulo: String, mensaje: String) {
        AlertDialog.Builder(this)
            .setTitle(titulo)
            .setMessage(mensaje)
            .setPositiveButton("Aceptar", null)
            .show()
    }

    private fun mostrarInfo(titulo: String, mensaje: String) {
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

    private fun cargarDatosRadar(idProfesor: Int) {
        val api = RetrofitClient.instance.create(ApiService::class.java)

        api.obtenerDatosRadar(idProfesor).enqueue(object : Callback<RadarResponse> {
            override fun onResponse(call: Call<RadarResponse>, response: Response<RadarResponse>) {
                if (response.isSuccessful) {
                    datosRadar = response.body()
                }
            }

            override fun onFailure(call: Call<RadarResponse>, t: Throwable) {
                // No se interrumpe al usuario si solo falla la gráfica.
            }
        })
    }

    private fun cargarMiValoracion(idProfesor: Int) {
        val prefs = getSharedPreferences("MisPreferencias", MODE_PRIVATE)
        val email = prefs.getString("EMAIL_USUARIO", "") ?: ""

        if (email.isEmpty()) return

        val api = RetrofitClient.instance.create(ApiService::class.java)

        api.obtenerMiValoracion(idProfesor, email).enqueue(object : Callback<MiValoracionResponse?> {
            override fun onResponse(
                call: Call<MiValoracionResponse?>,
                response: Response<MiValoracionResponse?>
            ) {
                val voto = response.body()

                if (voto != null) {
                    notaHorario = (voto.nota_horario / 2f).roundToInt().coerceIn(0, 5)
                    notaMaterial = (voto.nota_material / 2f).roundToInt().coerceIn(0, 5)
                    notaAtencion = (voto.nota_atencion / 2f).roundToInt().coerceIn(0, 5)
                    notaTutorias = (voto.nota_tutorias / 2f).roundToInt().coerceIn(0, 5)
                    notaGuia = (voto.nota_guia / 2f).roundToInt().coerceIn(0, 5)

                    comentario = voto.comentario ?: ""
                    yaValorado = true
                }
            }

            override fun onFailure(call: Call<MiValoracionResponse?>, t: Throwable) {
                // No se interrumpe al usuario si solo falla la carga de su valoración previa.
            }
        })
    }

    private fun enviarValoracion(
        profeId: Int,
        coment: String,
        n1: Float,
        n2: Float,
        n3: Float,
        n4: Float,
        n5: Float
    ) {
        val prefs = getSharedPreferences("MisPreferencias", MODE_PRIVATE)
        val email = prefs.getString("EMAIL_USUARIO", "") ?: ""

        if (email.isEmpty()) {
            mostrarError(
                "Sesión no encontrada",
                "No se ha encontrado tu correo. Vuelve a iniciar sesión."
            )
            return
        }

        if (n1 == 0f || n2 == 0f || n3 == 0f || n4 == 0f || n5 == 0f) {
            mostrarError(
                "Valoración incompleta",
                "Debes puntuar todos los apartados antes de enviar la valoración."
            )
            return
        }

        guardando = true

        val api = RetrofitClient.instance.create(ApiService::class.java)

        api.insertarValoracion(profeId, email, coment, n1, n2, n3, n4, n5)
            .enqueue(object : Callback<Void> {

                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    guardando = false

                    if (response.isSuccessful) {
                        yaValorado = true
                        cargarDatosRadar(profeId)

                        mostrarInfo(
                            "Valoración guardada",
                            "Tu valoración se ha guardado correctamente."
                        )
                    } else {
                        mostrarError(
                            "Error al guardar",
                            "No se ha podido guardar la valoración. Código: ${response.code()}."
                        )
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    guardando = false

                    mostrarError(
                        "Error de conexión",
                        "No se ha podido conectar con el servidor. Comprueba tu conexión e inténtalo de nuevo."
                    )
                }
            })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleProfesorScreen(
    profesor: Profesor,
    datosRadar: RadarResponse?,
    notaHorario: Int,
    notaMaterial: Int,
    notaAtencion: Int,
    notaTutorias: Int,
    notaGuia: Int,
    comentario: String,
    guardando: Boolean,
    yaValorado: Boolean,
    onVolver: () -> Unit,
    onVerResenas: () -> Unit,
    onNotaHorarioChange: (Int) -> Unit,
    onNotaMaterialChange: (Int) -> Unit,
    onNotaAtencionChange: (Int) -> Unit,
    onNotaTutoriasChange: (Int) -> Unit,
    onNotaGuiaChange: (Int) -> Unit,
    onComentarioChange: (String) -> Unit,
    onEnviarValoracion: () -> Unit
) {
    val azulPrincipal = Color(0xFF2196F3)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Detalle profesor",
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
                    containerColor = azulPrincipal,
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            item {
                Text(
                    text = profesor.nombre,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = azulPrincipal
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = profesor.departamento ?: "Sin departamento",
                    fontSize = 15.sp,
                    color = Color.DarkGray
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Estadísticas del profesor",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333)
                )

                Spacer(modifier = Modifier.height(8.dp))

                RadarChartCompose(datosRadar = datosRadar)

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = onVerResenas,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Ver reseñas")
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Realizar valoración",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333)
                )

                Spacer(modifier = Modifier.height(12.dp))

                CriterioValoracionCard(
                    titulo = "Seguimiento horario",
                    descripcion = "Puntualidad y cumplimiento de horas.",
                    valor = notaHorario,
                    onValorChange = onNotaHorarioChange
                )

                CriterioValoracionCard(
                    titulo = "Material extra",
                    descripcion = "Diapositivas, charlas, actividades prácticas...",
                    valor = notaMaterial,
                    onValorChange = onNotaMaterialChange
                )

                CriterioValoracionCard(
                    titulo = "Atención al alumno",
                    descripcion = "Responde dudas en clase y descansos.",
                    valor = notaAtencion,
                    onValorChange = onNotaAtencionChange
                )

                CriterioValoracionCard(
                    titulo = "Cumplimiento tutorías",
                    descripcion = "Respeta los horarios de tutoría.",
                    valor = notaTutorias,
                    onValorChange = onNotaTutoriasChange
                )

                CriterioValoracionCard(
                    titulo = "Guía docente",
                    descripcion = "Sigue el método de calificación oficial.",
                    valor = notaGuia,
                    onValorChange = onNotaGuiaChange
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = comentario,
                    onValueChange = onComentarioChange,
                    label = {
                        Text("Comentario")
                    },
                    minLines = 3,
                    maxLines = 5,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = azulPrincipal,
                        focusedLabelColor = azulPrincipal
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onEnviarValoracion,
                    enabled = !guardando,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = azulPrincipal,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    if (guardando) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = if (yaValorado) "Actualizar valoración" else "Guardar valoración"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun CriterioValoracionCard(
    titulo: String,
    descripcion: String,
    valor: Int,
    onValorChange: (Int) -> Unit
) {
    var expandido by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 7.dp),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        expandido = !expandido
                    },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = titulo,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333)
                )

                Text(
                    text = if (expandido) "▴" else "▾",
                    fontSize = 20.sp,
                    color = Color(0xFF2196F3)
                )
            }

            if (expandido) {
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = descripcion,
                    fontSize = 14.sp,
                    color = Color.DarkGray
                )

                Spacer(modifier = Modifier.height(10.dp))

                SelectorEstrellas(
                    valor = valor,
                    onValorChange = onValorChange
                )
            }
        }
    }
}

@Composable
fun SelectorEstrellas(
    valor: Int,
    onValorChange: (Int) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 1..5) {
            Text(
                text = if (i <= valor) "★" else "☆",
                fontSize = 34.sp,
                color = Color(0xFFFFC107),
                modifier = Modifier
                    .padding(end = 6.dp)
                    .clickable {
                        onValorChange(i)
                    }
            )
        }

        Spacer(modifier = Modifier.padding(horizontal = 6.dp))

        Text(
            text = if (valor == 0) "Sin puntuar" else "$valor/5",
            fontSize = 14.sp,
            color = Color.DarkGray
        )
    }
}

@Composable
fun RadarChartCompose(datosRadar: RadarResponse?) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        if (datosRadar == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(
                    color = Color(0xFF2196F3)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Cargando estadísticas...",
                    color = Color.DarkGray
                )
            }
        } else {
            AndroidView(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                factory = { context ->
                    RadarChart(context).apply {
                        configurarRadarChart(this)
                    }
                },
                update = { chart ->
                    pintarRadarChart(chart, datosRadar)
                }
            )
        }
    }
}

fun configurarRadarChart(radarChart: RadarChart) {
    radarChart.description.isEnabled = false

    radarChart.webLineWidth = 1f
    radarChart.webColor = AndroidColor.LTGRAY
    radarChart.webLineWidthInner = 1f
    radarChart.webColorInner = AndroidColor.LTGRAY
    radarChart.webAlpha = 100

    val xAxis = radarChart.xAxis
    xAxis.textSize = 9f
    xAxis.yOffset = 0f
    xAxis.xOffset = 0f
    xAxis.textColor = AndroidColor.DKGRAY
    xAxis.valueFormatter = IndexAxisValueFormatter(
        arrayOf("Horario", "Material", "Atención", "Tutorías", "Guía")
    )

    val yAxis = radarChart.yAxis
    yAxis.setLabelCount(5, true)
    yAxis.textSize = 9f
    yAxis.axisMinimum = 0f
    yAxis.axisMaximum = 10f
    yAxis.setDrawLabels(false)
}

fun pintarRadarChart(radarChart: RadarChart, datos: RadarResponse) {
    val entradas = ArrayList<RadarEntry>()

    entradas.add(RadarEntry(datos.avg_horario))
    entradas.add(RadarEntry(datos.avg_material))
    entradas.add(RadarEntry(datos.avg_atencion))
    entradas.add(RadarEntry(datos.avg_tutorias))
    entradas.add(RadarEntry(datos.avg_guia))

    val dataSet = RadarDataSet(entradas, "Competencias")

    val colorAzul = AndroidColor.rgb(33, 150, 243)
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
    data.setValueTextColor(AndroidColor.DKGRAY)

    radarChart.data = data
    radarChart.invalidate()
}