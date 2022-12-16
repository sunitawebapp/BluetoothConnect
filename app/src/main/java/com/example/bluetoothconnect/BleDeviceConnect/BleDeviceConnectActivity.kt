package com.example.bluetoothconnect.BleDeviceConnect

import android.Manifest
import android.app.Activity
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.ParcelUuid
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.bluetoothconnect.R
import java.util.*


//https://stackoverflow.com/questions/21916775/auto-connecting-to-a-ble-device

class BleDeviceConnectActivity : Activity() {
   lateinit var leScanCallback: ScanCallback
   lateinit var lefilterScanCallback :ScanCallback
   val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
   var devices: ArrayList<String> = ArrayList()
    var devicesaddress: ArrayList<String> = ArrayList()
    var tempdevices: ArrayList<String> = ArrayList()
    val bluetoothLeScanner = mBluetoothAdapter.bluetoothLeScanner
    var scanning = false
    val handler = Handler()
    private lateinit var adapter: BluetoothDevicesAdapter
    // Stops scanning after 10 seconds.
    val SCAN_PERIOD: Long = 10000
    var deviceuuid=""
    private var bluetoothGatt: BluetoothGatt? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ble_device_connect)
        val out = findViewById<View>(R.id.out) as TextView
        val button1 = findViewById<View>(R.id.button1) as Button
        val button2 = findViewById<View>(R.id.button2) as Button
        val button3 = findViewById<View>(R.id.button3) as Button
        val button4 =findViewById<View>(R.id.button4) as Button
        val button5 =findViewById<View>(R.id.button5) as Button
        val  printers=findViewById<View>(R.id.printers) as ListView

        adapter=BluetoothDevicesAdapter(this)

        if (mBluetoothAdapter == null) {
            out.append("device not supported")
        }
        button1.setOnClickListener {
            if (!mBluetoothAdapter!!.isEnabled) {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(
                    enableBtIntent,
                    REQUEST_ENABLE_BT
                )
            }
        }
        button2.setOnClickListener {
            if (!mBluetoothAdapter!!.isDiscovering) {
                //                   out.append("MAKING YOUR DEVICE DISCOVERABLE");
                val context = applicationContext
                val text: CharSequence = "MAKING YOUR DEVICE DISCOVERABLE"
                val duration = Toast.LENGTH_SHORT
                val toast = Toast.makeText(context, text, duration)
                toast.show()

                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    val devices: Set<BluetoothDevice> = mBluetoothAdapter.getBondedDevices()
                    for (device in devices) {
                        if (device.uuids[0] !=null){
                            deviceuuid=device.uuids[0].toString()
                        }
                        Log.d("deviceuuids", "onCreate: "+" Device: ${device.uuids[0]}       ${device.name}")

                    }



                }

                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE)
                startActivityForResult(
                    enableBtIntent,
                    REQUEST_DISCOVERABLE_BT
                )
            }
        }
        button3.setOnClickListener {
            mBluetoothAdapter!!.disable()
            //            out.append("TURN_OFF BLUETOOTH");
            val context = applicationContext
            val text: CharSequence = "TURNING_OFF BLUETOOTH"
            val duration = Toast.LENGTH_LONG
            val toast = Toast.makeText(context, text, Toast.LENGTH_SHORT)
            toast.show()
        }
    //    val leDeviceListAdapter = LeDeviceListAdapter()
        // Device scan callback.


         var connectionState = STATE_DISCONNECTED
         val bluetoothGattCallback: BluetoothGattCallback = object : BluetoothGattCallback() {
             override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                 if (newState == BluetoothProfile.STATE_CONNECTED) {
                     // successfully connected to the GATT Server
                     connectionState = STATE_CONNECTED
                     Log.d("connected", "onConnectionStateChange: "+connectionState)
                    // broadcastUpdate(ACTION_GATT_CONNECTED)
                 } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                     // disconnected from the GATT Server
                     connectionState = STATE_DISCONNECTED
                    // broadcastUpdate(ACTION_GATT_DISCONNECTED)
                 }
             }
         }

        printers.setOnItemClickListener { _, _, i, _ ->
            val device = devicesaddress[i]

            bluetoothGatt = mBluetoothAdapter.getRemoteDevice(device).connectGatt(this, true, bluetoothGattCallback)
          if (connectionState==2){
              Toast.makeText(this@BleDeviceConnectActivity, "successfully connected ${devicesaddress[i]}", Toast.LENGTH_SHORT).show()
          }
        }



        button4.setOnClickListener {

            leScanCallback= object : ScanCallback() {
                override fun onScanResult(callbackType: Int, result: ScanResult) {
                    super.onScanResult(callbackType, result)
                    val scanRecord = result.scanRecord
                    val advertisementData = scanRecord!!.bytes

                    if (result.device.name != null) {
                        if (!devices.contains(result.device.name)){
                            devicesaddress.add(result.device.address)
                            //  devices.add(result.device.name)

                            if (scanRecord.serviceUuids!=null){
                                tempdevices.add(scanRecord.serviceUuids.get(0).toString())
                                devices.add(result.device.name)
                            }
                            Log.d("advertisementData", "advertisement data: " + scanRecord.serviceUuids   +"  "+ scanRecord.deviceName)

                            Log.d("device", "onCreate: "+" Device: ${result.device.name}")
                        }

                    }

                    printers.adapter=adapter
                }
            }
            var   filters :MutableList<ScanFilter> =ArrayList()
            if (!scanning) { // Stops scanning after a pre-defined scan period.
                handler.postDelayed({
                    scanning = false

                  bluetoothLeScanner.stopScan(leScanCallback)
                }, SCAN_PERIOD)
                scanning = true


                val settings = ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
                    .build()

           //     for (i in tempdevices.indices){
                    filters.add(
                     //   ScanFilter.Builder().setServiceUuid(ParcelUuid(UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb")),convertFromInteger(0x1800)).build()
                                ScanFilter.Builder().setServiceUuid(ParcelUuid(UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb"))).build()

                    )
              //  }


               // bluetoothLeScanner.startScan(leScanCallback)
               bluetoothLeScanner.startScan(filters,settings,leScanCallback)
            } else {
                scanning = false
                bluetoothLeScanner.stopScan(leScanCallback)
            }

            Log.d("filters", "onCreate: "+filters)
        }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
     //   menuInflater.inflate(R.menu.activity_ble_device_connect, menu)
        return true
    }

    companion object {
        private const val REQUEST_ENABLE_BT = 0
        private const val REQUEST_DISCOVERABLE_BT = 0
            const val ACTION_GATT_CONNECTED =
                "com.example.bluetooth.le.ACTION_GATT_CONNECTED"
            const val ACTION_GATT_DISCONNECTED =
                "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED"

            private const val STATE_DISCONNECTED = 0
            private const val STATE_CONNECTED = 2
    }

    override fun onStart() {
        super.onStart()
        if (ContextCompat.checkSelfPermission(this@BleDeviceConnectActivity, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this@BleDeviceConnectActivity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        }
    }
   /* override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            FINE_LOCATION_PERMISSION_REQUEST -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    scanLeDevice(true)
                } else {
//notify the user to allow location detection otherwise the scaning won't work
                }
                return
            }
        }

    }*/
   inner class BluetoothDevicesAdapter(context: Context) : ArrayAdapter<BluetoothDevice>(context, android.R.layout.simple_list_item_1) {
       override fun getCount(): Int {
           return devices.size
       }

       override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
           return LayoutInflater.from(context)
               .inflate(R.layout.bluetooth_device_row, parent, false).apply {
                //   findViewById<TextView>(R.id.name).text = if (devices[position].name.isNullOrEmpty()) devices[position].address else devices[position].name
                   findViewById<TextView>(R.id.name).text = devices.get(position).toString()
                  /* findViewById<TextView>(R.id.pairStatus).visibility = if (devices[position].bondState != BluetoothDevice.BOND_NONE) View.VISIBLE else View.INVISIBLE
                   findViewById<TextView>(R.id.pairStatus).text = when (devices[position].bondState) {
                       BluetoothDevice.BOND_BONDED -> "Paired"
                       BluetoothDevice.BOND_BONDING -> "Pairing.."
                       else -> ""*/
                   }
               }
       }

    fun convertFromInteger(i: Int): ParcelUuid? {
        val MSB = 0x0000000000001000L
        val LSB = -0x7fffff7fa064cb05L
        val value = (i and -0x1).toLong()

        return ParcelUuid(  UUID(MSB or (value shl 32), LSB))
    }
}