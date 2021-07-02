package com.example.photogallery.api

import okhttp3.Interceptor
import okhttp3.Response

private const val FLICKR_API_KEY = "YOUR API KEY"

class PhotoInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        val newUrl = originalRequest.url().newBuilder()
            .addQueryParameter("api_key", FLICKR_API_KEY)
            .addQueryParameter("format", "json")
            .addQueryParameter("nojsoncallback", "1")
            .addQueryParameter("extras", "url_s")
            .addQueryParameter("safe_search", "1")
            .build()

        val newRequest = originalRequest.newBuilder()
            .url(newUrl)
            .build()

        return chain.proceed(newRequest)
    }
}