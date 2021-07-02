package com.example.photogallery.model

import android.net.Uri
import com.google.gson.annotations.SerializedName

data class GalleryItem(
    var id: String = "",
    var title: String = "",
    var owner: String = "",
    @SerializedName("url_s") var url: String = ""
) {
    val photoPageUri: Uri
        get() = Uri.parse("https://www.flickr.com/photos/")
            .buildUpon()
            .appendPath(owner)
            .appendPath(id)
            .build()
}