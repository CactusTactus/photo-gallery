package com.example.photogallery

import android.app.Activity
import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.photogallery.app.NOTIFICATION_CHANNEL_ID
import com.example.photogallery.model.GalleryItem

private const val TAG = "PollWorker"

class PollWorker(private val context: Context, workerParameters: WorkerParameters) :
    Worker(context, workerParameters) {
    override fun doWork(): Result {
        val query = QueryPreferences.getStoredQuery(context)
        val lastResultId = QueryPreferences.getLastResultId(context)
        val galleryItems: List<GalleryItem> = if (query.isEmpty()) {
            FlickrFetcher().fetchPhotosRequest()
                .execute()
                .body()
                ?.galleryItems
        } else {
            FlickrFetcher().searchPhotosRequest(query)
                .execute()
                .body()
                ?.galleryItems
        } ?: emptyList()

        if (galleryItems.isEmpty()) {
            return Result.success()
        }

        val resultId = galleryItems.first().id
        if (resultId == lastResultId) {
            Log.i(TAG, "Got an old result: $resultId")
        } else {
            Log.i(TAG, "Got a new result: $resultId")
            QueryPreferences.setLastResultId(context, resultId)

            val intent = PhotoGalleryActivity.newIntent(context)
            val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)
            val resources = context.resources
            val notification = NotificationCompat
                .Builder(context, NOTIFICATION_CHANNEL_ID)
                .setTicker(resources.getString(R.string.new_pictures_title))
                .setSmallIcon(android.R.drawable.ic_menu_report_image)
                .setContentTitle(resources.getString(R.string.new_pictures_title))
                .setContentText(resources.getString(R.string.new_pictures_text))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()

            // todo: replace to Activity.RESULT_OK?
            showBackgroundNotification(Activity.RESULT_CANCELED, notification)
        }

        return Result.success()
    }

    private fun showBackgroundNotification(requestCode: Int, notification: Notification) {
        val intent = Intent(ACTION_SHOW_NOTIFICATION).apply {
            putExtra(EXTRA_REQUEST_CODE, requestCode)
            putExtra(EXTRA_NOTIFICATION, notification)
        }

        context.sendOrderedBroadcast(intent, PERMISSION_PRIVATE)
    }

    companion object {
        const val ACTION_SHOW_NOTIFICATION = "com.example.photogallery.SHOW_NOTIFICATION"
        const val PERMISSION_PRIVATE = "com.example.photogallery.PRIVATE"
        const val EXTRA_REQUEST_CODE = "request_code"
        const val EXTRA_NOTIFICATION = "notification"
    }
}