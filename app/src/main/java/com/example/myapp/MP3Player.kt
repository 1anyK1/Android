package com.example.myapp

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import java.io.File

class MP3Player : AppCompatActivity() {

    private lateinit var seekbar: SeekBar
    private lateinit var volume: SeekBar
    private lateinit var playB: Button
    private lateinit var nextB: Button
    private lateinit var prevB: Button
    private lateinit var cycleB: Button
    private lateinit var permText: TextView

    private lateinit var handler: Handler
    private lateinit var runnable: Runnable

    private var musicList = mutableListOf<String>()
    private var players = mutableListOf<MediaPlayer>()
    private var playing = 0
    private var cycleCode = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mp3_player)

        volume = findViewById(R.id.seekVol)
        seekbar = findViewById(R.id.seekBar)
        playB = findViewById(R.id.play)
        nextB = findViewById(R.id.next)
        prevB = findViewById(R.id.previous)
        cycleB = findViewById(R.id.cycle)
        permText = findViewById(R.id.textView2)

        checkPermissions()
    }

    private fun checkPermissions() {
        val permissions = mutableListOf<String>()
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.READ_MEDIA_AUDIO)
        }

        if (permissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissions.toTypedArray(), 1)
        } else {
            loadMusic()
        }
    }

    private fun loadMusic() {
        val musicDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).path)
        if (musicDir.exists() && musicDir.isDirectory) {
            val files = musicDir.listFiles { file -> file.extension == "mp3" } ?: return
            musicList = files.map { it.absolutePath }.toMutableList()

            for (path in musicList) {
                val player = MediaPlayer().apply {
                    setDataSource(path)
                    prepare()
                }
                players.add(player)
            }

            setupPlayer()
        } else {
            permText.text = "Музыкальные файлы не найдены"
        }
    }

    private fun setupPlayer() {
        handler = Handler()

        seekbar.max = players[playing].duration
        runnable = object : Runnable {
            override fun run() {
                seekbar.progress = players[playing].currentPosition
                handler.postDelayed(this, 1000)
            }
        }
        handler.postDelayed(runnable, 1000)

        volume.max = 100
        volume.progress = 100
        volume.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                players[playing].setVolume(progress / 100f, progress / 100f)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, pos: Int, changed: Boolean) {
                if (changed) {
                    players[playing].seekTo(pos)
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {}
            override fun onStopTrackingTouch(p0: SeekBar?) {}
        })

        playB.setOnClickListener { startPause() }
        nextB.setOnClickListener { if (cycleCode == 0) next() else { cycle(); next(); startPause() } }
        prevB.setOnClickListener { if (cycleCode == 0) back() else { cycle(); back(); startPause() } }
        cycleB.setOnClickListener { cycle() }

        players[playing].setOnCompletionListener {
            if (cycleCode == 0) next() else players[playing].start()
        }

        players[playing].start()
    }

    private fun startPause() {
        if (players[playing].isPlaying) {
            players[playing].pause()
        } else {
            players[playing].start()
        }
    }

    private fun next() {
        players[playing].stop()
        players[playing].prepare()
        playing = (playing + 1) % players.size
        players[playing].start()
        seekbar.max = players[playing].duration
    }

    private fun back() {
        players[playing].stop()
        players[playing].prepare()
        playing = if (playing == 0) players.size - 1 else playing - 1
        players[playing].start()
        seekbar.max = players[playing].duration
    }

    private fun cycle() {
        cycleCode = if (cycleCode == 0) 1 else 0
        players[playing].setOnCompletionListener {
            if (cycleCode == 0) next() else players[playing].start()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(runnable)
        players.forEach { it.release() }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            loadMusic()
        } else {
            permText.text = "Нет доступа к аудиофайлам"
        }
    }
}
