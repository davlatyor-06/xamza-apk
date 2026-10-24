package com.xamzabek.app

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.xamzabek.app.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var recordingService: RecordingService? = null
    private var isBound = false

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            val localBinder = binder as RecordingService.LocalBinder
            recordingService = localBinder.getService()
            isBound = true
            updateUI(recordingService?.isRecording == true)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            recordingService = null
            isBound = false
        }
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnRecord.setOnClickListener {
            if (hasPermissions()) {
                toggleRecording()
            } else {
                requestPermissions()
            }
        }

        // Bind to service if running
        val intent = Intent(this, RecordingService::class.java)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    private fun toggleRecording() {
        val isCurrentlyRecording = recordingService?.isRecording == true

        if (!isCurrentlyRecording) {
            // Start recording
            val intent = Intent(this, RecordingService::class.java).apply {
                action = RecordingService.ACTION_START
            }
            ContextCompat.startForegroundService(this, intent)
            Toast.makeText(this, "Yozish boshlandi! 🔴", Toast.LENGTH_SHORT).show()
        } else {
            // Stop recording
            val intent = Intent(this, RecordingService::class.java).apply {
                action = RecordingService.ACTION_STOP
            }
            startService(intent)
            Toast.makeText(this, "Yozish to'xtatildi ✅", Toast.LENGTH_SHORT).show()
        }

        updateUI(!isCurrentlyRecording)
    }

    private fun updateUI(isRecording: Boolean) {
        if (isRecording) {
            binding.btnRecord.text = "⏹ TO'XTATISH"
            binding.btnRecord.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_red_dark))
            binding.tvStatus.text = "🔴 Yozilmoqda..."
            binding.tvStatus.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_light))
        } else {
            binding.btnRecord.text = "▶ YOZISHNI BOSHLASH"
            binding.btnRecord.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_green_dark))
            binding.tvStatus.text = "⏸ Tayyor"
            binding.tvStatus.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray))
        }
    }

    private fun hasPermissions(): Boolean {
        val permissions = mutableListOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        return permissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        ActivityCompat.requestPermissions(this, permissions.toTypedArray(), PERMISSION_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                toggleRecording()
            } else {
                Toast.makeText(this, "Ruxsatlar berilmadi! Sozlamalardan ruxsat bering.", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Sync UI with service state
        recordingService?.let {
            updateUI(it.isRecording)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isBound) {
            unbindService(serviceConnection)
            isBound = false
        }
    }
}
