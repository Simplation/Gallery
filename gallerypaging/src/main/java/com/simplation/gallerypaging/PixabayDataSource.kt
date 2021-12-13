package com.simplation.gallerypaging

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.PageKeyedDataSource
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.google.gson.Gson

enum class NetworkStatus {
    LOADING,
    FAILED,
    COMPLETE
}

class PixabayDataSource(private val context: Context) : PageKeyedDataSource<Int, PhotoItem>() {
    var retry: (() -> Any)? = null
    private val _networkStatus = MutableLiveData<NetworkStatus>()
    val networkStatus: LiveData<NetworkStatus> = _networkStatus

    private val queryKey =
        arrayOf("cat", "dog", "car", "beauty", "phone", "computer", "flower", "animal")

    override fun loadInitial(
        params: LoadInitialParams<Int>,
        callback: LoadInitialCallback<Int, PhotoItem>
    ) {
        _networkStatus.postValue(NetworkStatus.LOADING)

        val url =
            "https://pixabay.com/api/?key=24699165-35749a326f5427612125c46e8&q=${queryKey}&per_page=50&page=1"
        StringRequest(
            Request.Method.GET,
            url,
            {
                retry = null
                val dataList = Gson().fromJson(it, Pixabay::class.java).hits.toList()
                callback.onResult(dataList, null, 2)
            },
            {
                // 保存函数
                retry = { loadInitial(params, callback) }
                _networkStatus.postValue(NetworkStatus.FAILED)
                Log.d("TAG", "loadInitial: $it")
            }
        ).also { VolleySingleton.getInstance(context).requestQueue.add(it) }
    }

    override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, PhotoItem>) {
    }

    override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, PhotoItem>) {
        _networkStatus.postValue(NetworkStatus.LOADING)
        val url =
            "https://pixabay.com/api/?key=24699165-35749a326f5427612125c46e8&q=${queryKey}&per_page=50&page=${params.key}"
        StringRequest(
            Request.Method.GET,
            url,
            {
                retry = null
                val dataList = Gson().fromJson(it, Pixabay::class.java).hits.toList()
                callback.onResult(dataList, params.key + 1)
            },
            {
                if (it.toString() == "loadAfter: com.android.volley.ClientError") {
                    _networkStatus.postValue(NetworkStatus.COMPLETE)
                } else {
                    retry = { loadAfter(params, callback) }
                    _networkStatus.postValue(NetworkStatus.FAILED)
                }

                Log.d("TAG", "loadAfter: $it")
            }
        ).also { VolleySingleton.getInstance(context).requestQueue.add(it) }
    }
}