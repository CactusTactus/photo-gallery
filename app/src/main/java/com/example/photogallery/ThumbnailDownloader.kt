package com.example.photogallery

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import java.util.concurrent.ConcurrentHashMap

private const val TAG = "ThumbnailDownloader"
private const val THREAD_NAME = "thumbnail_downloader_handler_thread"
private const val MESSAGE_DOWNLOAD = 0

class ThumbnailDownloader<in T>(
    private val responseHandler: Handler,
    private val onThumbnailDownloaded: (T, Bitmap) -> Unit
) : HandlerThread(THREAD_NAME) {
    // TODO: all exercises
    // TODO: add caching LRU cache (page 537 en)
    private val requestMap = ConcurrentHashMap<T, String>()
    private val flickrFetcher = FlickrFetcher()

    private var hasQuit = false

    private lateinit var requestHandler: Handler

    val fragmentLifecycleObserver = object : LifecycleObserver {
        @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
        fun setup() {
            Log.i(TAG, "Starting background thread")
            start()
            looper
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        fun tearDown() {
            Log.i(TAG, "Destroying background thread")
            quit()
        }
    }

    val viewLifecycleObserver = object : LifecycleObserver {
        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        fun clearQueue() {
            Log.i(TAG, "[viewLO -> onDestroy()] Clearing all requests from queue")
            requestHandler.removeMessages(MESSAGE_DOWNLOAD)
            requestMap.clear()
        }
    }

    @Suppress("UNCHECKED_CAST")
    @SuppressLint("HandlerLeak")
    override fun onLooperPrepared() {
        super.onLooperPrepared()
        requestHandler =
            object : Handler() {
                override fun handleMessage(msg: Message) {
                    super.handleMessage(msg)
                    if (msg.what == MESSAGE_DOWNLOAD) {
                        val target = msg.obj as T
                        if (target != null) {
                            Log.i(TAG, "Got a request for URL: ${requestMap[target]}")
                            handleRequest(target)
                        }
                    }
                }
            }
    }

    private fun handleRequest(target: T) {
        if (target != null) {
            val url = requestMap[target] ?: return
            val bitmap = flickrFetcher.fetchPhoto(url) ?: return
            responseHandler.post {
                if (requestMap[target] != url || hasQuit) {
                    return@post
                }
                requestMap.remove(target)
                onThumbnailDownloaded(target, bitmap)
            }
        }

    }

    override fun quit(): Boolean {
        hasQuit = true
        return super.quit()
    }

    fun queueThumbnail(target: T, url: String) {
        requestMap[target] = url
        requestHandler.obtainMessage(MESSAGE_DOWNLOAD, target).sendToTarget()
        Log.i(TAG, "Got a URL: $url")
    }

}