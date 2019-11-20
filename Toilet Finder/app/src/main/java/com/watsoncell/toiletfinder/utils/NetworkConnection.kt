package com.watsoncell.toiletfinder.utils

import android.content.Context
import android.net.ConnectivityManager

object InternetConnection {
    fun isNetworkAvailable(context : Context) : Boolean{
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        connectivityManager.activeNetworkInfo.let {
            return it != null && it.isConnected
        }
    }
}