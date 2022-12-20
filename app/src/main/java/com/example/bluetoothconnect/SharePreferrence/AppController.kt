package com.example.bluetoothconnect.SharePreferrence

import android.app.Application

class AppController() : Application() {

    override fun onCreate() {
        super.onCreate()

        SessionManager(applicationContext)
    }
}