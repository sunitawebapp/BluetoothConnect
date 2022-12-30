package com.example.bluetoothconnect

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.ParcelUuid
import android.util.Log
import android.widget.Button
import android.widget.Toast
import com.example.bluetoothconnect.BleDeviceConnect.BleDeviceConnectActivity
import com.example.bluetoothconnect.SharePreferrence.SessionManager
import java.util.*


class MainActivity : AppCompatActivity() {
    lateinit var btnBleDevice : Button
    val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnBleDevice=findViewById(R.id.btnBleDevice)

        btnBleDevice.setOnClickListener {
        var intent = Intent(this@MainActivity , BleDeviceConnectActivity::class.java)
            startActivity(intent)
        }
/*
        val bluetoothGattCallback: BluetoothGattCallback = object : BluetoothGattCallback() {
            override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {

                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {

                }
            }
        }

        if (  !SessionManager(this).getconnectedDevices()!!.equals("")){
            for (i in SessionManager(this).getconnectedDevices()!!.split(",").indices){
               if (!SessionManager(this).getconnectedDevices()!!.split(",")[i].equals("")){
                   mBluetoothAdapter.getRemoteDevice(SessionManager(this).getconnectedDevices()!!.split(",")[i])
                       .connectGatt(this, true, bluetoothGattCallback)
               }
            }
        }*/

    }
}