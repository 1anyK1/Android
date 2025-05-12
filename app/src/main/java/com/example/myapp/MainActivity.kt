package com.example.myapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    private lateinit var MP3 : Button
    private lateinit var CalcBut: Button
    private lateinit var GPSBut: Button


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
        GPSBut = findViewById(R.id.GPSButton)

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

        GPSBut.setOnClickListener{
            val pageGPS = Intent(this@MainActivity, GPS::class.java)
            startActivity(pageGPS)
        }
    }
}