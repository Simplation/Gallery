package com.simplation.gallerypaging

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.paging.toLiveData


class GalleryViewModel(application: Application) : AndroidViewModel(application) {
    private val factory = PixabayDataSourceFactory(application)
    val pageListLiveData = factory.toLiveData(1)
    val networkStatus: LiveData<NetworkStatus> =
        Transformations.switchMap(factory.pixabayDataSource) { it.networkStatus }

    fun resetQuery() {
        pageListLiveData.value?.dataSource?.invalidate()
    }

    fun retry() {
        factory.pixabayDataSource.value?.retry?.invoke()
    }
}