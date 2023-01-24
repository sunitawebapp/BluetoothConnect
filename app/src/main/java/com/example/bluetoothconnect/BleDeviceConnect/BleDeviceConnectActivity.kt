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
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.ParcelUuid
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.bluetoothconnect.R
import com.example.bluetoothconnect.databinding.ActivityBleDeviceConnectBinding
import java.util.*


//https://stackoverflow.com/questions/21916775/auto-connecting-to-a-ble-device

class BleDeviceConnectActivity : Activity() ,OnClickListener{
    lateinit var binding : ActivityBleDeviceConnectBinding
    var wcharacteristic: BluetoothGattCharacteristic? = null
    var bletoothGatt: BluetoothGatt? = null
    var bluetoothResponse: String=""
    var bluetoothResponse2: String=""
    var bluetoothResponse3 : String=""

   lateinit var leScanCallback: ScanCallback

   val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
   var devices: ArrayList<String> = ArrayList()
    var devicesaddress: ArrayList<String> = ArrayList()
    var tempdevices: ArrayList<String> = ArrayList()
    val bluetoothLeScanner = mBluetoothAdapter.bluetoothLeScanner
    var scanning = false
    val handler = Handler()
    lateinit var  printers : ListView
    private lateinit var adapter: BluetoothDevicesAdapter
    // Stops scanning after 10 seconds.
    val SCAN_PERIOD: Long = 10000
    var deviceuuid=""
    lateinit var bluetoothGattCallback: BluetoothGattCallback

    private var bluetoothGatt: BluetoothGatt? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBleDeviceConnectBinding.inflate(layoutInflater)
        val view = binding.root
        binding.apply {
            TestingCMDSend.setOnClickListener(this@BleDeviceConnectActivity)
            BatterySend.setOnClickListener(this@BleDeviceConnectActivity)
            HomeSend.setOnClickListener(this@BleDeviceConnectActivity)
            CurrentPositionSend.setOnClickListener(this@BleDeviceConnectActivity)
            BackTiltSend.setOnClickListener(this@BleDeviceConnectActivity)
            LumberSend.setOnClickListener(this@BleDeviceConnectActivity)
            SeatTiltSend.setOnClickListener(this@BleDeviceConnectActivity)
            BackHeightSend.setOnClickListener(this@BleDeviceConnectActivity)
            ChairHeightSend.setOnClickListener(this@BleDeviceConnectActivity)
            button4.setOnClickListener(this@BleDeviceConnectActivity)
            button1.setOnClickListener(this@BleDeviceConnectActivity)
            button2.setOnClickListener(this@BleDeviceConnectActivity)
            button3.setOnClickListener(this@BleDeviceConnectActivity)
        }
        setContentView(view)
        val out = findViewById<View>(R.id.out) as TextView

       


        printers = findViewById(R.id.printers)

        adapter = BluetoothDevicesAdapter(this)

        if (mBluetoothAdapter == null) {
            out.append("device not supported")
        }

        getbleDevices()
        printers.setOnItemClickListener { _, _, i, _ ->

                val device = devicesaddress[i]
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {


                }
                bletoothGatt = mBluetoothAdapter.getRemoteDevice(device)
                    .connectGatt(this, false, bluetoothGattCallback)

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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (grantResults.size>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()

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
                    filters.add(ScanFilter.Builder().setServiceUuid(ParcelUuid(UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb"))).build())
                    //  }

                    bluetoothLeScanner.startScan(filters,settings,leScanCallback)
                } else {
                    scanning = false
                    bluetoothLeScanner.stopScan(leScanCallback)
                }
                Log.d("filters", "onCreate: "+filters)
            }
        }else{
            Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
        }
        return
    }

    fun getbleDevices() {
        leScanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                super.onScanResult(callbackType, result)
                val scanRecord = result.scanRecord
                val advertisementData = scanRecord!!.bytes

                if (result.device.name != null) {
                    if (!devices.contains(result.device.name)) {
                        devicesaddress.add(result.device.address)
                        //  devices.add(result.device.name)

                        if (scanRecord.serviceUuids != null) {
                            tempdevices.add(scanRecord.serviceUuids.get(0).toString())
                            devices.add(result.device.name)
                        }
                        Log.d(
                            "advertisementData",
                            "advertisement data: " + scanRecord.serviceUuids + "  " + scanRecord.deviceName
                        )

                        Log.d("device", "onCreate: " + " Device: ${result.device.name}")
                    }

                }

                printers.adapter = adapter
            }
        }
        var filters: MutableList<ScanFilter> = ArrayList()
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
                ScanFilter.Builder()
                    .setServiceUuid(ParcelUuid(UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb")))
                    .build()
            )
            //  }

            bluetoothLeScanner.startScan(filters, settings, leScanCallback)
        } else {
            scanning = false
            bluetoothLeScanner.stopScan(leScanCallback)
        }
        Log.d("filters", "onCreate: " + filters)

        //    val leDeviceListAdapter = LeDeviceListAdapter()
        // Device scan callback.


        var connectionState = STATE_DISCONNECTED
        bluetoothGattCallback= object : BluetoothGattCallback() {
            override fun onConnectionStateChange(
                gatt: BluetoothGatt,
                status: Int,
                newState: Int
            ) {

                gatt.discoverServices()
                Log.d("connectionState", "onConnectionStateChange: " + gatt.services)

                Log.d(
                    "BluetoothProfile.STATE_CONNECTED",
                    "onConnectionStateChange: " + BluetoothProfile.STATE_CONNECTED
                )

                if (newState == BluetoothProfile.STATE_CONNECTED) {
//                     Toast.makeText(this@BleDeviceConnectActivity, "successfully connected ", Toast.LENGTH_SHORT).show()
                    if (ActivityCompat.checkSelfPermission(
                            this@BleDeviceConnectActivity,
                            Manifest.permission.BLUETOOTH_CONNECT
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {

                        return
                    }
                    connectionState = STATE_CONNECTED
                    Log.d("connectionState", "onConnectionStateChange: " + connectionState)
                    gatt.discoverServices()
                    // broadcastUpdate(ACTION_GATT_CONNECTED)
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    // disconnected from the GATT Server
                    connectionState = STATE_DISCONNECTED
                    Log.d("connected", "onConnectionStateChange: " + "disconnected")
                    Toast.makeText(
                        this@BleDeviceConnectActivity,
                        " disconnected ",
                        Toast.LENGTH_SHORT
                    ).show()
                    // broadcastUpdate(ACTION_GATT_DISCONNECTED)
                }
            }

            override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
                Log.d("statusdiscoverd", "onServicesDiscovered: " + gatt!!.services)

                for (gattService in gatt!!.services) {
                    Log.d("onServicesDiscovered", "onServicesDiscovered: ---------------------")
                    Log.i("", "onServicesDiscovered: service=" + gattService.uuid)
                    for (characteristic in gattService.characteristics) {
                        Log.i("", "onServicesDiscovered: characteristic=" + characteristic.uuid)
                        if (characteristic.uuid.toString() == "0000ffe1-0000-1000-8000-00805f9b34fb") {
                            if (ActivityCompat.checkSelfPermission(
                                    this@BleDeviceConnectActivity,
                                    Manifest.permission.BLUETOOTH_CONNECT
                                ) != PackageManager.PERMISSION_GRANTED
                            ) {
                                gatt.setCharacteristicNotification(characteristic, true)
                                //   Log.w("", "onServicesDiscovered: found LED")
                                //  val originalString = "560D0F0600F0AA"
                                //   var originalString = "AT"
                                // val b: ByteArray = hexStringToByteArray(originalString)!!
                                //     var b: ByteArray = originalString.toByteArray()
                                //  characteristic.setValue(b)// call this BEFORE(!) you 'write' any stuff to the server
                                wcharacteristic = characteristic
                                //   bluetoothGatt?.writeCharacteristic(characteristic)


                                Log.w(
                                    "onServicesDiscovered",
                                    "onServicesDiscovered: " + characteristic.getValue()
                                )
                                return
                            }
                        }
                    }
                }

                super.onServicesDiscovered(gatt, status)
            }
            override fun onCharacteristicChanged(
                gatt: BluetoothGatt?,
                characteristic: BluetoothGattCharacteristic?
            ) {

                bluetoothResponse2 = characteristic!!.value.decodeToString()
                bluetoothResponse =
                    characteristic!!.value.decodeToString()?.split("\\r?\\n".toRegex()).toString()
                bluetoothResponse3= characteristic!!.value.decodeToString()?.split("\r\n".toRegex()).toString()
                val array = characteristic!!.value.decodeToString()?.split("\\r?\\n".toRegex())
                var string = ""
              //  bluetoothResponse?.split("\\r?\\n".toRegex())[0]
                val size = array?.size

                // bluetoothResponse?.split("\\r?\\n".toRegex())

                Log.w(
                    "onCharacteristicChanged",
                    "in change" + bluetoothResponse


                )
                super.onCharacteristicChanged(gatt, characteristic)
            }
        }

    }

    override fun onClick(v: View?) {
        when(v){

            binding.TestingCMDSend->{
                wcharacteristic?.setValue( binding.etTestingCMD.text.toString())// call this BEFORE(!) you 'write' any stuff to the server
                // wcharacteristic?.setValue("AT")// call this BEFORE(!) you 'write' any stuff to the server
                bletoothGatt?.writeCharacteristic(wcharacteristic)
                wcharacteristic?.let {
                    // bletoothGatt = wcharacteristic!!.value.decodeToString()
                    binding.bleResponse.setText(bluetoothResponse)
                    binding.bleResponse2.setText(bluetoothResponse2)
                    binding.bleResponse3.setText(bluetoothResponse3)

                    Log.d(
                        "valuereponse",
                        "writeCharacteristics: " + wcharacteristic!!.value.decodeToString()
                    )
                }
            }
            binding.BatterySend->{
                wcharacteristic?.setValue("b")// call this BEFORE(!) you 'write' any stuff to the server
                //   wcharacteristic?.setValue("AT")// call this BEFORE(!) you 'write' any stuff to the server
                bletoothGatt?.writeCharacteristic(wcharacteristic)
                wcharacteristic?.let {
                    //bluetoothResponse = wcharacteristic!!.value.decodeToString()
                    binding.bleResponse.setText(bluetoothResponse)
                    binding.bleResponse2.setText(bluetoothResponse2)
                    binding.bleResponse3.setText(bluetoothResponse3)
                    Log.d(
                        "valuereponse",
                        "writeCharacteristics: " + wcharacteristic!!.value.decodeToString()
                    )
                }
            }
            binding.HomeSend->{
                wcharacteristic?.setValue("h9")// call this BEFORE(!) you 'write' any stuff to the server
                //  wcharacteristic?.setValue("AT")// call this BEFORE(!) you 'write' any stuff to the server
                bletoothGatt?.writeCharacteristic(wcharacteristic)
                wcharacteristic?.let {
                    //bluetoothResponse = wcharacteristic!!.value.decodeToString()
                    //  bleResponse.setText(bluetoothResponse)
                    binding.bleResponse.setText(bluetoothResponse)
                    binding.bleResponse2.setText(bluetoothResponse2)
                    binding.bleResponse3.setText(bluetoothResponse3)
                    Log.d(
                        "valuereponse",
                        "writeCharacteristics: " + wcharacteristic!!.value.decodeToString()
                    )
                }
            }
            binding.CurrentPositionSend->{
                wcharacteristic?.setValue("p9")// call this BEFORE(!) you 'write' any stuff to the server
                //  wcharacteristic?.setValue("AT")// call this BEFORE(!) you 'write' any stuff to the server
                bletoothGatt?.writeCharacteristic(wcharacteristic)
                wcharacteristic?.let {
                    //    bluetoothResponse = wcharacteristic!!.value.decodeToString()

                    binding.bleResponse.setText(bluetoothResponse)
                    binding.bleResponse2.setText(bluetoothResponse2)
                    binding.bleResponse3.setText(bluetoothResponse3)
                    Log.d(
                        "valuereponse",
                        "writeCharacteristics: " + wcharacteristic!!.value.decodeToString()
                    )
                }
            }
            binding.BackTiltSend->{
                wcharacteristic?.setValue("m1"+ binding.etBackTilt.text.toString())// call this BEFORE(!) you 'write' any stuff to the server
                //  wcharacteristic?.setValue("AT")// call this BEFORE(!) you 'write' any stuff to the server
                bletoothGatt?.writeCharacteristic(wcharacteristic)
                wcharacteristic?.let {
                    // bluetoothResponse = wcharacteristic!!.value.decodeToString()
                    binding. bleResponse.setText(bluetoothResponse)
                    binding.bleResponse2.setText(bluetoothResponse2)
                    binding.bleResponse3.setText(bluetoothResponse3)
                    Log.d(
                        "valuereponse",
                        "writeCharacteristics: " + wcharacteristic!!.value.decodeToString()
                    )
                }
            }
            binding.LumberSend->{
                wcharacteristic?.setValue("m3"+ binding.etLumber.text.toString())// call this BEFORE(!) you 'write' any stuff to the server
                // wcharacteristic?.setValue("AT")// call this BEFORE(!) you 'write' any stuff to the server
                bletoothGatt?.writeCharacteristic(wcharacteristic)
                wcharacteristic?.let {
                    //   bletoothGatt = wcharacteristic!!.value.decodeToString()
                    binding.bleResponse.setText(bluetoothResponse)
                    binding.bleResponse2.setText(bluetoothResponse2)
                    binding.bleResponse3.setText(bluetoothResponse3)
                    Log.d(
                        "valuereponse",
                        "writeCharacteristics: " + wcharacteristic!!.value.decodeToString()
                    )
                }
            }
            binding.SeatTiltSend->{
                wcharacteristic?.setValue("m0"+ binding.etSeatTilt.text.toString())// call this BEFORE(!) you 'write' any stuff to the server
                //wcharacteristic?.setValue("AT")// call this BEFORE(!) you 'write' any stuff to the server
                bletoothGatt?.writeCharacteristic(wcharacteristic)
                wcharacteristic?.let {
                    // bletoothGatt = wcharacteristic!!.value.decodeToString()
                    binding.bleResponse.setText(bluetoothResponse)
                    binding.bleResponse2.setText(bluetoothResponse2)
                    binding.bleResponse3.setText(bluetoothResponse3)
                    Log.d(
                        "valuereponse",
                        "writeCharacteristics: " + wcharacteristic!!.value.decodeToString()
                    )
                }
            }
            binding.BackHeightSend->{
                wcharacteristic?.setValue("m2"+ binding.etBackHeight.text.toString())// call this BEFORE(!) you 'write' any stuff to the server
                //  wcharacteristic?.setValue("AT")// call this BEFORE(!) you 'write' any stuff to the server
                bletoothGatt?.writeCharacteristic(wcharacteristic)
                wcharacteristic?.let {
                    // bletoothGatt = wcharacteristic!!.value.decodeToString()
                    binding.bleResponse.setText(bluetoothResponse)
                    binding. bleResponse2.setText(bluetoothResponse2)
                    binding.bleResponse3.setText(bluetoothResponse3)

                    Log.d(
                        "valuereponse",
                        "writeCharacteristics: " + wcharacteristic!!.value.decodeToString()
                    )
                }
            }
            binding.ChairHeightSend->{
                wcharacteristic?.setValue("m4"+ binding.etChairHeight.text.toString())// call this BEFORE(!) you 'write' any stuff to the server
                // wcharacteristic?.setValue("AT")// call this BEFORE(!) you 'write' any stuff to the server
                bletoothGatt?.writeCharacteristic(wcharacteristic)
                wcharacteristic?.let {
                    // bletoothGatt = wcharacteristic!!.value.decodeToString()
                    binding.bleResponse.setText(bluetoothResponse)
                    binding. bleResponse2.setText(bluetoothResponse2)
                    binding.bleResponse3.setText(bluetoothResponse3)

                    Log.d(
                        "valuereponse",
                        "writeCharacteristics: " + wcharacteristic!!.value.decodeToString()
                    )
                }
            }
            binding.button4->  {
                leScanCallback = object : ScanCallback() {
                    override fun onScanResult(callbackType: Int, result: ScanResult) {
                        super.onScanResult(callbackType, result)
                        val scanRecord = result.scanRecord
                        val advertisementData = scanRecord!!.bytes

                        if (result.device.name != null) {
                            if (!devices.contains(result.device.name)) {
                                devicesaddress.add(result.device.address)
                                //  devices.add(result.device.name)

                                if (scanRecord.serviceUuids != null) {
                                    tempdevices.add(scanRecord.serviceUuids.get(0).toString())
                                    devices.add(result.device.name)
                                }
                                Log.d(
                                    "advertisementData",
                                    "advertisement data: " + scanRecord.serviceUuids + "  " + scanRecord.deviceName
                                )

                                Log.d("device", "onCreate: " + " Device: ${result.device.name}")
                            }

                        }

                        printers.adapter = adapter
                    }
                }
                var filters: MutableList<ScanFilter> = ArrayList()
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
                        ScanFilter.Builder()
                            .setServiceUuid(ParcelUuid(UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb")))
                            .build()
                    )
                    //  }


                    // bluetoothLeScanner.startScan(leScanCallback)
                    bluetoothLeScanner.startScan(filters, settings, leScanCallback)
                } else {
                    scanning = false
                    bluetoothLeScanner.stopScan(leScanCallback)
                }
                Log.d("filters", "onCreate: " + filters)
            }
            binding.button1->{
                if (!mBluetoothAdapter!!.isEnabled) {
                    val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                    startActivityForResult(
                        enableBtIntent,
                        REQUEST_ENABLE_BT
                    )
                }
            }
            binding.button2->{
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
                        val devices: Set<BluetoothDevice> = mBluetoothAdapter.bondedDevices
                        for (device in devices) {
                            if (device.uuids[0] != null) {
                                deviceuuid = device.uuids[0].toString()
                            }
                            Log.d(
                                "deviceuuids",
                                "onCreate: " + " Device: ${device.uuids[0]}       ${device.name}"
                            )

                        }


                    }

                    val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE)
                    startActivityForResult(
                        enableBtIntent,
                        REQUEST_DISCOVERABLE_BT
                    )
                }
            }
            binding.button3->{
                mBluetoothAdapter!!.disable()
                //            out.append("TURN_OFF BLUETOOTH");
                val context = applicationContext
                val text: CharSequence = "TURNING_OFF BLUETOOTH"
                val duration = Toast.LENGTH_LONG
                val toast = Toast.makeText(context, text, Toast.LENGTH_SHORT)
                toast.show()
            }

        }
    }


}
