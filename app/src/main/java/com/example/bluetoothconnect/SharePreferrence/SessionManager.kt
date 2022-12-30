package com.example.bluetoothconnect.SharePreferrence

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.preference.PreferenceManager





class SessionManager(activity: Context){
    // Storing data into SharedPreferences
    val sharedPreferences: SharedPreferences =activity.getSharedPreferences("LocalStore", MODE_PRIVATE)

    val editor :SharedPreferences.Editor = sharedPreferences.edit()


    fun setconnectedDevices(deviceList : String){
        editor.putString("connectedDevices",deviceList)
        editor.apply()
    }

    fun getconnectedDevices() : String?{
        return sharedPreferences.getString("connectedDevices","")
    }


}