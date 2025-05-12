package com.example.myapp

import android.Manifest
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


class GPS : AppCompatActivity() {

    private lateinit var VivodGPS: TextView
    private lateinit var locationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_gps)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        VivodGPS = findViewById(R.id.gps)
        locationClient = LocationServices.getFusedLocationProviderClient(this)

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        } else if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION), 2)
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
            interval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                for (location in result.locations) {
                    val timeString = SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(Date(location.time))
                    VivodGPS.text = "Широта: ${location.latitude}, Долгота: ${location.longitude}, Время: $timeString"
                    saveLocationToJson(location)
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
}
