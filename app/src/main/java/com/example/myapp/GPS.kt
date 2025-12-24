package com.example.myapp

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.location.*
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import org.zeromq.ZMQ
import android.telephony.CellInfoLte
import android.telephony.TelephonyManager

class GPS : AppCompatActivity() {

    private lateinit var VivodGPS: TextView
    private lateinit var locationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var telephonyManager: TelephonyManager

    private val ZMQ_SERVER_ADDRESS = "tcp://10.60.199.30:5555"
    private var zmqContext: ZMQ.Context? = null
    private var zmqSocket: ZMQ.Socket? = null

    private fun startZmqClient() {
        Thread {
            try {
                zmqContext = ZMQ.context(1)

                zmqSocket = zmqContext?.socket(ZMQ.PUSH)

                zmqSocket?.connect(ZMQ_SERVER_ADDRESS)

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    private fun sendLocationZmq(location: Location) {
        val json = JSONObject()
        json.put("latitude", location.latitude)
        json.put("longitude", location.longitude)
        json.put("timestamp", System.currentTimeMillis())
        json.put("device_id", "android-client-1")

        val allCellInfo = telephonyManager.allCellInfo
        for (cellInfo in allCellInfo) {
            if (cellInfo is CellInfoLte) {
                val cellSignal = cellInfo.cellSignalStrength

                json.put("RSRP", cellSignal.rsrp)
                json.put("RSRQ", cellSignal.rsrq)
                json.put("RSSI", cellSignal.rssi)
            }
        }


        val message = json.toString()

        Thread {
            try {
                if (zmqSocket != null) {

                    val success = zmqSocket!!.send(message.toByteArray(ZMQ.CHARSET), 0)

                    if (success) {
                        println("ZMQ: Сообщение отправлено")
                    } else {
                        println("ZMQ: Ошибка отправки сообщения (send вернул false).")
                    }
                } else {
                    println("ZMQ сокет не инициализирован (null).")
                }
            } catch (e: Exception) {
                println("ZMQ: Критическая ошибка при отправке")
            }
        }.start()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_gps)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager



        VivodGPS = findViewById(R.id.gps)
        locationClient = LocationServices.getFusedLocationProviderClient(this)

        startZmqClient()

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        } else if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION), 2)
        } else if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_PHONE_STATE), 3)
        } else {
            startLocationUpdates()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates()
        }
    }

    override fun onResume() {
        super.onResume()

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val timeString = SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(Date(location.time))
                    VivodGPS.text = "Широта: ${location.latitude}, Долгота: ${location.longitude}, Время: $timeString"
                } else {
                    VivodGPS.text = "Доступ к координатам закрыт"
                }
            }
        }
    }

    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.create().apply {
            interval = 3000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                for (location in result.locations) {
                    val timeString = SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(Date(location.time))
                    VivodGPS.text = "Широта: ${location.latitude}, Долгота: ${location.longitude}, Время: $timeString"
                    saveLocationToJson(location)
                    sendLocationZmq(location)
                }
            }
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationClient.requestLocationUpdates(locationRequest, locationCallback, mainLooper)
        }
    }

    private fun saveLocationToJson(location: Location) {
        val json = JSONObject()
        json.put("latitude", location.latitude)
        json.put("longitude", location.longitude)
        json.put("timestamp", System.currentTimeMillis())

        val file = File(filesDir, "locations.json")
        file.appendText(json.toString() + "\n")
    }

    override fun onPause() {
        super.onPause()
        locationClient.removeLocationUpdates(locationCallback)
    }

    override fun onDestroy() {
        super.onDestroy()
        zmqSocket?.close()
        zmqContext?.term()
    }

}
