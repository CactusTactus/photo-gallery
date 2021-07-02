package com.example.photogallery

import android.app.Application
import androidx.lifecycle.*
import com.example.photogallery.model.GalleryItem

class PhotoGalleryViewModel(private val app: Application) : AndroidViewModel(app) {
    private val flickrFetcher = FlickrFetcher()
    private val mutableSearchTerm = MutableLiveData<String>()

    val searchTerm: String
        get() = mutableSearchTerm.value ?: ""

    val galleryItemLiveData: LiveData<List<GalleryItem>>

    init {
        mutableSearchTerm.value = QueryPreferences.getStoredQuery(app)
        galleryItemLiveData = Transformations.switchMap(mutableSearchTerm) { searchTerm ->
            if (searchTerm.isBlank()) {
                flickrFetcher.fetchPhotos()
            } else {
                flickrFetcher.searchPhotos(searchTerm)
            }
        }
    }

    override fun onCleared() {
        flickrFetcher.cancelRequest()
        super.onCleared()
    }

    fun fetchPhotos(query: String = "") {
        QueryPreferences.setStoredQuery(app, query)
        mutableSearchTerm.value = query
    }
}