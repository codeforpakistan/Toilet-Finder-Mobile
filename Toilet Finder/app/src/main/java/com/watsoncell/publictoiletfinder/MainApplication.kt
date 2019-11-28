package com.watsoncell.publictoiletfinder

import android.app.Application
import android.content.Context
import com.watsoncell.publictoiletfinder.utils.LocalHelper

class MainApplication : Application() {


    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(LocalHelper.onAttach(newBase!!))
    }
}