package com.example.valoracionprofesores  // <--- Fíjate que ahora pone 'example'

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.lazy

object RetrofitClient {
    // 10.0.2.2 es la dirección especial para que el Emulador acceda a tu PC
    private const val BASE_URL = "http://10.0.2.2:3000/"

    val instance: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}