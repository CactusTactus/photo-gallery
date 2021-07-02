package com.example.photogallery

import android.app.Activity
import android.app.Notification
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationManagerCompat

private const val TAG = "NotificationBroadcastReceiver"

class NotificationBroadcastReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Received broadcast: ${intent.action} (result: $resultCode)")
        if (resultCode != Activity.RESULT_OK) {
            return
        }

        val requestCode = intent.getIntExtra(PollWorker.EXTRA_REQUEST_CODE, 0)
        val notification = intent.getParcelableExtra<Notification>(PollWorker.EXTRA_NOTIFICATION)!!

        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(requestCode, notification)
    }
}