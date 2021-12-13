package com.simplation.gallery

import android.content.Context
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley

/**
 * @作者: Simplation
 * @日期: 2021/12/07 11:44
 * @描述:
 * @更新:
 */
class VolleySingleton private constructor(context: Context) {
    companion object {
        private var INSTANCE: VolleySingleton? = null

        fun getInstance(context: Context) =
            INSTANCE ?: synchronized(this) {
                VolleySingleton(context).also { INSTANCE = it }
            }
    }

    val requestQueue: RequestQueue by lazy {
        Volley.newRequestQueue(context.applicationContext)
    }
}
