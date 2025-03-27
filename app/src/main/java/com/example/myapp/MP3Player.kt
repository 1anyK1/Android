package com.example.myapp

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.widget.Button
import android.widget.SeekBar
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MP3Player : AppCompatActivity() {

    lateinit private var seekbar: SeekBar;               lateinit private var player: MediaPlayer
    lateinit private var player2: MediaPlayer;           lateinit private var playB: Button
    lateinit private var nextB: Button;                  lateinit private var prevB: Button
    lateinit private var music: Array<MediaPlayer>;      lateinit private var runnable: Runnable
    lateinit private var handler: Handler;               lateinit private var cycleB: Button
    lateinit private var volume: SeekBar


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_mp3_player)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        player = MediaPlayer.create(this@MP3Player, R.raw.music1)
        player2 = MediaPlayer.create(this@MP3Player, R.raw.music2)

        volume = findViewById(R.id.seekVol)
        music = arrayOf(player, player2)
        playB = findViewById(R.id.play)
        nextB = findViewById(R.id.next)
        prevB = findViewById(R.id.previous)
        seekbar = findViewById(R.id.seekBar)
        cycleB = findViewById(R.id.cycle)

    }
    var playing = 0

    private fun startPause(){
        if(music[playing].isPlaying){ music[playing].pause() }
        else { music[playing].start() }
    }

    private fun next(){

        music[playing].stop()
        music[playing].prepare()

        if(playing >= music.size - 1){
            playing = 0
        }
        else {
            playing += 1
        }

        seekbar.max = music[playing].duration
        seekbar.progress = 0
        music[playing].start()

        music[playing].setOnCompletionListener {
            next()
        }
    }

    private fun back(){
        music[playing].stop()
        music[playing].prepare()

        if(playing == 0){ playing = music.size - 1 }
        else { playing -= 1 }

        music[playing].start()
    }

    private var cycleCode = 0

    private fun cycle() {
        if (cycleCode == 0) {
            cycleCode += 1
            music[playing].setOnCompletionListener {
                music[playing].start()
            }
        } else {
            cycleCode -= 1
            music[playing].setOnCompletionListener {
                next()
            }
        }
    }

    override fun onStart() {
        super.onStart()

        volume.setProgress(volume.max)
        volume.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress : Int, fromUser: Boolean) {
                music[playing].setVolume(progress / 100f, progress / 100f)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        handler = Handler()
        seekbar.max = music[playing].duration
        runnable = Runnable {
                seekbar.progress = music[playing].currentPosition
                handler.postDelayed(runnable, 1000)
        }
        handler.postDelayed(runnable, 1000)

        seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(p0: SeekBar?, pos: Int, changed: Boolean) {
                if (changed){
                    music[playing].seekTo(pos)
                }
            }
            override fun onStartTrackingTouch(p0: SeekBar?) {}
            override fun onStopTrackingTouch(p0: SeekBar?) {}
        })

        music[playing].setOnCompletionListener {
            next()
        }

        cycleB. setOnClickListener{
            cycle()
        }

        playB.setOnClickListener{ startPause() }
        nextB.setOnClickListener{
            if (cycleCode == 0){
                next()
            } else {
                cycle()
                next()
                startPause()
            }
        }
        prevB.setOnClickListener{
            if (cycleCode == 0){
                back()
            } else {
                cycle()
                back()
                startPause()
            }
        }
    }

    override fun onStop() {
        super.onStop()
        handler.removeCallbacks(runnable)
    }
}