package com.xamzabek.app

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.MediaRecorder
import android.os.Binder
import android.os.Build
import android.os.Environment
import android.os.IBinder
import androidx.core.app.NotificationCompat
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RecordingService : Service() {

    companion object {
        const val ACTION_START = "ACTION_START_RECORDING"
        const val ACTION_STOP = "ACTION_STOP_RECORDING"
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "xamzabek_recording_channel"
    }

    private val binder = LocalBinder()
    private var mediaRecorder: MediaRecorder? = null
    var isRecording = false
        private set

    inner class LocalBinder : Binder() {
        fun getService(): RecordingService = this@RecordingService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startRecording()
            ACTION_STOP -> {
                stopRecording()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_STICKY
    }

    private fun startRecording() {
        if (isRecording) return

        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification())

        try {
            val outputFile = getOutputFile()
            val cameraId = getBackCameraId()

            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(this)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            mediaRecorder?.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setVideoSource(MediaRecorder.VideoSource.CAMERA)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setVideoEncoder(MediaRecorder.VideoEncoder.H264)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setVideoSize(1280, 720)
                setVideoFrameRate(30)
                setVideoEncodingBitRate(5_000_000)
                setAudioSamplingRate(44100)
                setAudioEncodingBitRate(128000)
                setOutputFile(outputFile.absolutePath)
                prepare()
                start()
            }

            isRecording = true
        } catch (e: Exception) {
            e.printStackTrace()
            isRecording = false
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    private fun stopRecording() {
        if (!isRecording) return
        try {
            mediaRecorder?.apply {
                stop()
                reset()
                release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            mediaRecorder = null
            isRecording = false
        }
    }

    private fun getBackCameraId(): String? {
        val cameraManager = getSystemService(CAMERA_SERVICE) as CameraManager
        for (id in cameraManager.cameraIdList) {
            val chars = cameraManager.getCameraCharacteristics(id)
            val facing = chars.get(CameraCharacteristics.LENS_FACING)
            if (facing == CameraCharacteristics.LENS_FACING_BACK) return id
        }
        return null
    }

    private fun getOutputFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val dir = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            File(getExternalFilesDir(Environment.DIRECTORY_MOVIES), "Xamzabek")
        } else {
            @Suppress("DEPRECATION")
            File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES),
                "Xamzabek"
            )
        }
        if (!dir.exists()) dir.mkdirs()
        return File(dir, "VID_$timeStamp.mp4")
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Xamzabek Yozuv",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Video yozilmoqda"
                setSound(null, null)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        val stopIntent = Intent(this, RecordingService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 0, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val openIntent = Intent(this, MainActivity::class.java)
        val openPendingIntent = PendingIntent.getActivity(
            this, 0, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("🔴 Xamzabek — Yozilmoqda")
            .setContentText("Video yozilmoqda. To'xtatish uchun bosing.")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentIntent(openPendingIntent)
            .addAction(android.R.drawable.ic_media_pause, "⏹ To'xtatish", stopPendingIntent)
            .setOngoing(true)
            .setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopRecording()
    }
}
