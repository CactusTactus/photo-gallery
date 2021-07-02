package com.example.photogallery

import com.example.photogallery.api.PhotoResponse
import com.example.photogallery.model.GalleryItem
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

class PhotoJsonDeserializer : JsonDeserializer<PhotoResponse> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): PhotoResponse {
        val photoResponse = PhotoResponse()
        json?.let {
            val photos = json.asJsonObject.get("photos").asJsonObject
            val photo = photos.getAsJsonArray("photo")
            photoResponse.galleryItems =
                if (photo.size() == 0) emptyList()
                else photo.map { galleryItemTransformer(it) }

        }
        return photoResponse
    }

    private val galleryItemTransformer: (JsonElement) -> GalleryItem = {
        val jsonObject = it.asJsonObject
        GalleryItem(
            id = jsonObject?.get("id")?.asString ?: "",
            owner = jsonObject.get("owner")?.asString ?: "",
            title = jsonObject?.get("title")?.asString ?: "",
            url = jsonObject?.get("url_s")?.asString ?: ""
        )
    }
}
