package com.freezer.chatapp.fcm

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.freezer.chatapp.R
import com.freezer.chatapp.data.model.Call
import com.freezer.chatapp.ui.call.AudioCallActivity
import com.freezer.chatapp.ui.call.VideoCallActivity
import com.freezer.chatapp.utils.FCMUtils
import com.freezer.chatapp.webrtc.CallMode
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


class FirebaseMessagingService : FirebaseMessagingService() {
    companion object {
        const val CALL_CHANNEL_ID = "call_channel"
        const val NOTIFICATION_ID = 10
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        FCMUtils().sendRegistrationToken(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val data = message.data
        // TODO: Check here
        if (data != null) {
            val callDataObj = Call(
                uid = data["uid"]!!,
                from = data["fromUid"]!!,
                to = data["toUid"]!!,
                status = data["status"]!!,
                callType = data["callType"]!!
            )

            createCallNotification(callDataObj)

            // Check if connection end
            val database = Firebase.firestore
            val callInfoRef = database.collection("calls_info").document(callDataObj.uid)
            var listener: ListenerRegistration? = null
            listener = callInfoRef.addSnapshotListener { snapshot, _ ->
                val data = snapshot?.toObject(Call::class.java)
                if (data != null) {
                    if (data.status != "RINGING") {
                        // Remove notification
                        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                        manager.cancel(NOTIFICATION_ID)
                        listener?.remove()
                    }
                }
            }
        }
    }

    private fun createCallNotification(call: Call) {
        val bundle = Bundle().apply {
            putString("calling_mode", CallMode.CALL_MODE_ANSWER)
            putParcelable("call_data", call)
        }

        val notifyIntent = if (call.callType == Call.Type.CALL_TYPE_VIDEO)
            Intent(this, VideoCallActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        else Intent(this, AudioCallActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        val pendingIntentAnswer = PendingIntent.getActivity(
            this, 0,
            notifyIntent.apply {
                putExtras(bundle)
                putExtra("notification_action", "answer")
            }, PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val pendingIntentHangUp = PendingIntent.getActivity(
            this, 1,
            notifyIntent.apply {
                putExtra("notification_action", "hang_up")
            }, PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, CALL_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentTitle("ChatApp")
            .setContentText("You have a call")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .addAction(0, "Answer", pendingIntentAnswer)
            .addAction(0, "Hang up", pendingIntentHangUp)
            .setOngoing(true)

        with(NotificationManagerCompat.from(this)) {
            notify(NOTIFICATION_ID, builder.build())
        }
    }
}