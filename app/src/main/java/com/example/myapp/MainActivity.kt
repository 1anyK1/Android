package com.example.myapp

import com.google.android.gms.location.LocationServices
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    private lateinit var VivodGPS: TextView
    private lateinit var MP3 : Button
    private lateinit var CalcBut: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        CalcBut = findViewById(R.id.calc)
        MP3 = findViewById(R.id.mp3B)
        VivodGPS = findViewById(R.id.gps)

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this@MainActivity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        } else if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this@MainActivity, arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION), 2)
        }
    }

    override fun onResume() {
        super.onResume()

        CalcBut.setOnClickListener{
            val pageCalc = Intent(this@MainActivity, Calculator::class.java)
            startActivity(pageCalc)
        }
        MP3.setOnClickListener{
            val pageMP3 = Intent(this@MainActivity, MP3Player::class.java)
            startActivity(pageMP3)
        }

        val ourLocation = LocationServices.getFusedLocationProviderClient(this)

        if (ActivityCompat.checkSelfPermission(this@MainActivity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            ourLocation.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    VivodGPS.text = "Широта: ${location.latitude}, Долгота: ${location.longitude}"
                } else {
                    VivodGPS.text = "Доступ к координатам закрыт"
                }
            }
        }
    }
}