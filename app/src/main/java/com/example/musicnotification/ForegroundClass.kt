package com.example.musicnotification

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class ForegroundClass : Service() {

    var notification : Notification ?= null
    var isPlaying = false
    var list = ArrayList<Song>()
    var now = 0
    lateinit var mediaPlayer: MediaPlayer
    var currentSong = -1

    override fun onCreate() {
        registerReceiver(broadcastReceiver, IntentFilter("ACTION"))
        super.onCreate()
    }

    override fun onBind(p0: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val playList : ArrayList<Song>? = intent?.getParcelableArrayListExtra<Song>("playList")

        println("playlist size : ${playList?.size}")

        if (playList != null) {
            list = playList
            now = 0
            createNotification("play", list.get(now))
            playMedia()
        }else {
            println("Error!")
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        unregisterReceiver(broadcastReceiver)
        mediaPlayer.stop()
        super.onDestroy()
    }

    private fun createNotification(action : String, song : Song) {
        var pausePlay = R.drawable.ic_baseline_play_arrow_24
        if (action == "play") {
            pausePlay = R.drawable.ic_baseline_pause_24
        }

        val intent1 = Intent(this, BroadcastClass::class.java).setAction("pausePlay")
        val intent2 = Intent(this, BroadcastClass::class.java).setAction("previous")
        val intent3 = Intent(this, BroadcastClass::class.java).setAction("next")

        val pendingIntent1 = PendingIntent.getBroadcast(this, 1, intent1, 0)
        val pendingIntent2 = PendingIntent.getBroadcast(this, 2, intent2, 0)
        val pendingIntent3 = PendingIntent.getBroadcast(this, 3, intent3, 0)

        val activityIntent = Intent(this, MainActivity::class.java)
        val activityPendingIntent = PendingIntent.getActivity(this, 2, activityIntent, 0)

        val bitmap = BitmapFactory.decodeResource(resources, song.image)

        notification = NotificationCompat.Builder(this, "channel1")
            .setContentText(song.songName)
            .setContentTitle(song.singer)
            .setContentIntent(activityPendingIntent)
            .addAction(R.drawable.ic_baseline_skip_previous_24, "Previous", pendingIntent2)
            .addAction(pausePlay, "PausePlay", pendingIntent1)
            .addAction(R.drawable.ic_baseline_skip_next_24, "Next", pendingIntent3)
            .setSmallIcon(R.drawable.ic_baseline_music_note_24)
            .setLargeIcon(bitmap)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOnlyAlertOnce(true)
            .setStyle(androidx.media.app.NotificationCompat.MediaStyle().setShowActionsInCompactView(0, 1, 2))
            .setSubText("Album")
            .build()


        startForeground(1, notification)

    }

    val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            val action = p1?.getStringExtra("action")

            if (action == "pausePlay") {
                if (isPlaying) {
                    createNotification("pause", list.get(now))
                    pauseMedia()
                }else {
                    createNotification("play", list.get(now))
                    playMedia()
                }
            }else if (action == "previous") {
                mediaPlayer?.let {
                    if (mediaPlayer.currentPosition > 3000) {
                        it.stop()
                        currentSong = -1
                        createNotification("play", list.get(now))
                        playMedia()
                    }else if(now != 0) {
                        it.stop()
                        now--
                        createNotification("play", list.get(now))
                        playMedia()
                    }
                }
            }else if (action == "next") {
                println("now: $now , size: ${list.size}")
                if (now+1 != list.size) {
                    mediaPlayer.stop()
                    now++
                    createNotification("play", list.get(now))
                    playMedia()
                }
            }
        }
    }

    private fun playMedia() {
        println("playing..")
        if (list.get(now).song != currentSong) {
            mediaPlayer = MediaPlayer.create(applicationContext, list.get(now).song)
            currentSong = list.get(now).song
        }
        mediaPlayer.start()
        isPlaying = true

        mediaPlayer.setOnCompletionListener {
            notification?.let {
                if (list.size == now+1) {
                    currentSong = -1
                    isPlaying = false
                    now = 0
                    createNotification("pause", list.get(0))
                    stopMedia()
                }else {
                    now++
                    createNotification("play", list.get(now))
                    playMedia()
                }
                sendBroadcast(Intent("ACTION").putExtra("action", "complete")
                    .putExtra("now", now).putExtra("isPlaying", isPlaying))
            }
        }
    }

    private fun pauseMedia() {
        println("pause..")
        mediaPlayer.pause()

        isPlaying = false
        stopForeground(false)
    }

    private fun stopMedia() {
        println("stop..")

        isPlaying = false
        stopForeground(false)
    }



}