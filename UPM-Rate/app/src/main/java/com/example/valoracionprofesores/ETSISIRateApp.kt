package com.example.valoracionprofesores

import android.app.Application

class ETSISIRateApp : Application() {

    override fun onCreate() {
        super.onCreate()
        RetrofitClient.init(this)
    }
}