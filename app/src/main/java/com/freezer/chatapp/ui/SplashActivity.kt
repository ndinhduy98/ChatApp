package com.freezer.chatapp.ui

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.freezer.chatapp.fcm.FirebaseMessagingService
import com.freezer.chatapp.ui.login.PhoneAuthActivity
import com.freezer.chatapp.ui.main.MainActivity
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if(FirebaseAuth.getInstance().currentUser == null) {
            startActivity(Intent(this, PhoneAuthActivity::class.java))
        } else {
            startActivity(Intent(this, MainActivity::class.java))
        }

        createNotificationChannel()

        finish()
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "calling_channel"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(FirebaseMessagingService.CALL_CHANNEL_ID, name, importance)
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}