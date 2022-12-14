package com.example.bluetoothconnect

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.example.bluetoothconnect.BleDeviceConnect.BleDeviceConnectActivity


class MainActivity : AppCompatActivity() {
    lateinit var btnBleDevice : Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnBleDevice=findViewById(R.id.btnBleDevice)

        btnBleDevice.setOnClickListener {
        var intent = Intent(this@MainActivity , BleDeviceConnectActivity::class.java)
            startActivity(intent)
        }
    }
}