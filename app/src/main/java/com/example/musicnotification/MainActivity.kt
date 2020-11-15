package com.example.musicnotification

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    var playList = ArrayList<Song>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val song1 = Song("Ceza", "Suspus", R.raw.suspus, R.drawable.ceza)
        val song2 = Song("Mor ve Ötesi", "Bir Derdim Var", R.raw.birderdimvar, R.drawable.morveotesi)
        val song3 = Song("Manga", "Beni Benimle Bırak", R.raw.benibenimlebirak, R.drawable.manga)
        playList.add(song1)
        playList.add(song2)
        playList.add(song3)
    }

    fun startNotification(view : View) {
        val intent = Intent(this, ForegroundClass::class.java)
        println("listsize: ${playList.size}")
        intent.putParcelableArrayListExtra("playList", playList)
        ContextCompat.startForegroundService(this, intent)
    }

    fun stopNotification(view : View) {
        val intent = Intent(this, ForegroundClass::class.java)
        stopService(intent)
    }

    override fun onDestroy() {
        val intent = Intent(this, ForegroundClass::class.java)
        stopService(intent)
        super.onDestroy()
    }

}