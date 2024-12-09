package com.example.neighbourly.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.neighbourly.R
import com.example.neighbourly.TaskMarketplaceActivity
import com.example.neighbourly.utils.Constants.USER_COLLECTION
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        remoteMessage.notification?.let {
            showNotification(it.title, it.body)
        }
        remoteMessage.data.let {
            val chatId = it["chatId"]
            val senderName = it["senderName"]
            val message = it["message"]
            if (chatId != null && senderName != null && message != null) {
                showNotification(senderName, message, chatId)
            }
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "New token: $token")
        sendTokenToServer(token)
    }

    private fun sendTokenToServer(token: String) {
        // Get current user's ID
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

        if (currentUserId != null) {
            // Update Firestore with the new token
            val db = FirebaseFirestore.getInstance()
            val userRef = db.collection(USER_COLLECTION).document(currentUserId)

            userRef.update("fcmToken", token)
                .addOnSuccessListener {
                    Log.d("FCM", "Token successfully sent to Firestore.")
                }
                .addOnFailureListener { e ->
                    Log.e("FCM", "Error sending token to Firestore", e)
                }
        } else {
            Log.w("FCM", "User not logged in. Token not sent to Firestore.")
        }
    }

    private fun showNotification(title: String?, message: String?, chatId: String? = null) {
        val intent = Intent(this, TaskMarketplaceActivity::class.java).apply {
            putExtra("CHAT_ID", chatId)
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = "chat_notifications"
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(
            channelId,
            "Chat Notifications",
            NotificationManager.IMPORTANCE_HIGH
        )
        notificationManager.createNotificationChannel(channel)

        notificationManager.notify(0, notificationBuilder.build())
    }
}
