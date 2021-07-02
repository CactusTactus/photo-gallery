package com.example.photogallery.api

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url

// Can be named as FlickrService
interface FlickrApi {
    @GET("/")
    fun fetchContents(): Call<String>

    @GET("services/rest/?method=flickr.interestingness.getList")
    fun fetchPhotos(@Query("page") page: Int = 1): Call<PhotoResponse>

    @GET
    fun fetchUrlBytes(@Url url: String): Call<ResponseBody>

    @GET("services/rest?method=flickr.photos.search")
    fun searchPhotos(@Query("text") query: String): Call<PhotoResponse>
}