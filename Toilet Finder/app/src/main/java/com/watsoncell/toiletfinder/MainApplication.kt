package com.watsoncell.toiletfinder

import android.app.Application
import android.content.Context
import com.watsoncell.toiletfinder.utils.LocalHelper

class MainApplication : Application() {


    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(LocalHelper.onAttach(newBase!!))
    }
}