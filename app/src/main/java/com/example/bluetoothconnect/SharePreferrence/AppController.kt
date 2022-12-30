package com.example.bluetoothconnect.SharePreferrence

import android.app.Application
import android.content.Intent
import android.widget.Toast
import com.example.bluetoothconnect.MessageService

class AppController() : Application() {
var text="sunita"
    override fun onCreate() {
        super.onCreate()

        SessionManager(applicationContext)
        var intent=Intent(this,MessageService::class.java)
        intent.setAction(text)
        startService(intent)
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
    }
}