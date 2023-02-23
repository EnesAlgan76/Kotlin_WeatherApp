package com.example.havadurumu

import android.content.Context
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley


class MySingleton private constructor(context: Context) {
    private var requestQueue: RequestQueue?

    init {
        ctx = context
        requestQueue = getRequestQueue()
    }


    fun getRequestQueue(): RequestQueue {
        if (requestQueue == null) {
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            requestQueue = Volley.newRequestQueue(ctx.getApplicationContext())
        }
        return requestQueue as RequestQueue
    }


    fun <T> addToRequestQueue(req: Request<T>?) {
        requestQueue?.add(req)
    }



    companion object {
        private var instance: MySingleton? = null
        private lateinit var ctx: Context
        @Synchronized
        fun getInstance(context: Context): MySingleton? {
            if (instance == null) {
                instance = MySingleton(context)
            }
            return instance
        }
    }
}