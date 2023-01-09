package com.example.bluetoothconnect.SharePreferrence

import android.app.Application
import android.content.Intent
import android.widget.Toast

class AppController() : Application() {

    override fun onCreate() {
        super.onCreate()

        SessionManager(applicationContext)

    }
}