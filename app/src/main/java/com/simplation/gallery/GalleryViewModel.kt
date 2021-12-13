package com.simplation.gallery

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.google.gson.Gson
import kotlin.math.ceil
import kotlin.collections.*

/**
 * @作者: Simplation
 * @日期: 2021/12/06 17:53
 * @描述:
 * @更新:
 */
const val DATA_STATUS_CAN_LOAD_MORE = 0
const val DATA_STATUS_NO_MORE = 1
const val DATA_STATUS_NETWORK_ERROR = 2


class GalleryViewModel(application: Application) : AndroidViewModel(application) {
    private val _dataStatusLive = MutableLiveData<Int>()
    private val _photoListLive = MutableLiveData<List<PhotoItem>>()

    private val keyWords =
        arrayOf("cat", "dog", "car", "beauty", "phone", "computer", "flower", "animal")
    private val per_page = 100
    private var currentPage = 1
    private var totalPage = 1
    private var currentKey = "cat"
    private var isNewQuery = true
    private var isLoading = false
    private var _needToScrollToTop = true
    var needToScrollToTop: Boolean
        get() = _needToScrollToTop
        set(value) {
            _needToScrollToTop = value
        }

    init {
        resetQuery()
    }

    val dataStatusLive: LiveData<Int>
        get() = _dataStatusLive

    val photoListLive: LiveData<List<PhotoItem>>
        get() = _photoListLive

    fun resetQuery() {
        currentPage = 1
        totalPage = 1
        currentKey = keyWords.random()
        isNewQuery = true
        needToScrollToTop = true

        fetchData()
    }

    fun fetchData() {
        if (isLoading) return

        if (currentPage > totalPage) {
            _dataStatusLive.value = DATA_STATUS_NO_MORE
            return
        }

        isLoading = true

        val stringRequest = StringRequest(
            Request.Method.GET,
            getUrl(),
            {
                with(Gson().fromJson(it, Pixabay::class.java)) {
                    // ceil()   将给定值 x 舍入到正无穷大的整数。
                    totalPage = ceil(totalHits.toDouble() / per_page).toInt()
                    totalPage = totalHits / per_page
                    if (isNewQuery) {
                        _photoListLive.value = hits.toList()
                    } else {
                        // flatten() 将指定的两个 list 整合成新的 list
                        _photoListLive.value =
                            arrayListOf(_photoListLive.value!!, hits.toList()).flatten()
                    }
                }
                _dataStatusLive.value = DATA_STATUS_CAN_LOAD_MORE
                isLoading = false
                isNewQuery = false
                currentPage++
            },
            {
                _dataStatusLive.value = DATA_STATUS_NETWORK_ERROR
                isLoading = false

            }
        )

        VolleySingleton.getInstance(getApplication()).requestQueue.add(stringRequest)
    }

    private fun getUrl(): String {
        return "https://pixabay.com/api/?key=24699165-35749a326f5427612125c46e8&q=${currentKey}&per_page=${per_page}&page=${currentPage}"
    }
}
