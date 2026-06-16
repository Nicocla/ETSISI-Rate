package com.example.valoracionprofesores

import retrofit2.Call
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    // 1. OBTENER LISTA DE PROFESORES
    @GET("profesores")
    fun obtenerProfesores(): Call<List<Profesor>>

    // 2. INSERTAR VALORACIÓN DETALLADA (MODIFICADO)
    // Ahora enviamos las 5 notas por separado
    @FormUrlEncoded
    @POST("insertar_valoracion")
    fun insertarValoracion(
        @Field("profesor_id") profesorId: Int,
        @Field("email_alumno") email: String,
        @Field("comentario") comentario: String,
        @Field("n1") n1: Float, // Horario
        @Field("n2") n2: Float, // Material
        @Field("n3") n3: Float, // Atención
        @Field("n4") n4: Float, // Tutorías
        @Field("n5") n5: Float  // Guía
    ): Call<Void>

    // 3. AÑADIR PROFESOR (SOLO ADMIN)
    @FormUrlEncoded
    @POST("anadir_profesor")
    fun anadirProfesor(
        @Field("nombre") nombre: String,
        @Field("departamento") departamento: String
    ): Call<Void>

    // 4. BORRAR PROFESOR (SOLO ADMIN)
    @DELETE("borrar_profesor/{id}")
    fun borrarProfesor(@Path("id") id: Int): Call<Void>

    // 5. PEDIR ESTADÍSTICAS (GRÁFICA)
    @GET("estadisticas/{id}")
    fun obtenerEstadisticas(@Path("id") id: Int): Call<List<Estadistica>>

    // 6. OBTENER RESEÑAS DE UN PROFE (MODERACIÓN)
    @GET("resenas/{id}")
    fun obtenerResenas(@Path("id") id: Int): Call<List<Resena>>

    // 7. BORRAR RESEÑA (MODERACIÓN O PROPIA)
    @DELETE("borrar_resena")
    fun borrarResena(
        @Query("profesor_id") profesorId: Int,
        @Query("email_alumno") emailAlumno: String
    ): Call<Void>

    // 8. OBTENER MI VALORACIÓN INDIVIDUAL (PARA EDITAR)
    @GET("mi_valoracion")
    fun obtenerMiValoracion(
        @Query("profesor_id") profesorId: Int,
        @Query("email_alumno") email: String
    ): Call<MiValoracionResponse?>

    // 9. OBTENER HISTORIAL DE RESEÑAS (MIS RESEÑAS)
    @GET("mis_resenas")
    fun obtenerMisResenas(@Query("email") email: String): Call<List<MiResenaItem>>

    @GET("estadisticas_radar/{id}")
    fun obtenerDatosRadar(@Path("id") id: Int): Call<RadarResponse>

    // Enviar el correo para recibir el código
    @FormUrlEncoded
    @POST("enviar_codigo")
    fun enviarCodigo(
        @Field("email") email: String
    ): Call<Void>

    // Comprobar si el código de 4 números es correcto
    @FormUrlEncoded
    @POST("verificar_codigo")
    fun verificarCodigo(
        @Field("email") email: String,
        @Field("codigo") codigo: String
    ): Call<Void>
}

// --- CLASES DE DATOS ---

data class Estadistica(
    val puntuacion: Int,
    val cantidad: Int
)

data class Resena(
    val email_alumno: String,
    val puntuacion: Float,
    val comentario: String?,
    val nota_horario: Float,
    val nota_material: Float,
    val nota_atencion: Float,
    val nota_tutorias: Float,
    val nota_guia: Float
)

// MODIFICADO: Añadimos los campos detallados para rellenar los desplegables
data class MiValoracionResponse(
    val puntuacion: Float,      // La media
    val comentario: String?,
    val nota_horario: Float,    // n1
    val nota_material: Float,   // n2
    val nota_atencion: Float,   // n3
    val nota_tutorias: Float,   // n4
    val nota_guia: Float        // n5
)

data class MiResenaItem(
    val profesor_id: Int,
    val nombre_profesor: String,
    val puntuacion: Float,
    val comentario: String?
)
// --- AL FINAL DEL ARCHIVO, CON LAS OTRAS DATA CLASSES ---
data class RadarResponse(
    val avg_horario: Float,
    val avg_material: Float,
    val avg_atencion: Float,
    val avg_tutorias: Float,
    val avg_guia: Float
)