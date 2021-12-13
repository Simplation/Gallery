package com.simplation.gallerypaging

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource

/**
 * @作者: Simplation
 * @日期: 2021/12/13 16:28
 * @描述:
 * @更新:
 */
class PixabayDataSourceFactory(private val context: Context) :
    DataSource.Factory<Int, PhotoItem>() {

    private val _pixabayDataSource = MutableLiveData<PixabayDataSource>()
    val pixabayDataSource: LiveData<PixabayDataSource> = _pixabayDataSource

    override fun create(): DataSource<Int, PhotoItem> {
        return PixabayDataSource(context).also { _pixabayDataSource.postValue(it) }
    }
}