package com.example.photogallery

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.photogallery.api.FlickrApi
import com.example.photogallery.api.PhotoInterceptor
import com.example.photogallery.api.PhotoResponse
import com.example.photogallery.model.GalleryItem
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

private const val TAG = "FlickrFetcher"

class FlickrFetcher {
    private val flickrApi: FlickrApi

    private lateinit var call: Call<*>

    init {
        val client = OkHttpClient.Builder()
            .addInterceptor(PhotoInterceptor())
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.flickr.com/")
            .addConverterFactory(GsonConverterFactory.create(getPhotoGson()))
            .client(client)
            .build()
        flickrApi = retrofit.create(FlickrApi::class.java)
    }

    fun fetchPhotosRequest(): Call<PhotoResponse> {
        return flickrApi.fetchPhotos()
    }

    fun fetchPhotos(): LiveData<List<GalleryItem>> {
        return fetchPhotoMetadata(fetchPhotosRequest())
    }

    fun searchPhotosRequest(query: String): Call<PhotoResponse> {
        return flickrApi.searchPhotos(query)
    }

    fun searchPhotos(query: String): LiveData<List<GalleryItem>> {
        return fetchPhotoMetadata(searchPhotosRequest(query))
    }

    private fun fetchPhotoMetadata(call: Call<PhotoResponse>): LiveData<List<GalleryItem>> {
        val responseLiveData = MutableLiveData<List<GalleryItem>>()
        call.enqueue(object : Callback<PhotoResponse> {
            override fun onResponse(call: Call<PhotoResponse>, response: Response<PhotoResponse>) {
                Log.d(TAG, "Response received")
                val photoResponse = response.body()
                var galleryItems = photoResponse?.galleryItems ?: mutableListOf()
                galleryItems = galleryItems.filterNot {
                    it.url.isBlank()
                }
                responseLiveData.value = galleryItems
            }

            override fun onFailure(call: Call<PhotoResponse>, t: Throwable) {
                Log.e(TAG, "Failed to fetch photos", t)
            }
        })
        return responseLiveData
    }

    @WorkerThread
    fun fetchPhoto(url: String): Bitmap? {
        val response = flickrApi.fetchUrlBytes(url).execute()
        val bitmap = response.body()?.byteStream()?.use(BitmapFactory::decodeStream)
        Log.i(TAG, "Decoded bitmap=$bitmap from Response=$response")
        return bitmap
    }

    fun cancelRequest() {
        if (::call.isInitialized) {
            call.apply {
                if (isExecuted) {
                    cancel()
                    if (!isCanceled) {
                        throw IllegalStateException("Request is not cancelled.")
                    }
                }
            }
        }
    }

    private fun getPhotoGson() = GsonBuilder()
        .registerTypeAdapter(PhotoResponse::class.java, PhotoJsonDeserializer())
        .create()

}