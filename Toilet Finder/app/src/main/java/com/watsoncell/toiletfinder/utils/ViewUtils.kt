package com.watsoncell.toiletfinder.utils

import android.content.Context
import android.support.design.widget.Snackbar
import android.view.View
import android.widget.Toast


fun View.snackBar(message: String) {
    Snackbar.make(this, message, Snackbar.LENGTH_LONG).let { snackBar ->
        snackBar.setAction("OK") {
            snackBar.dismiss()
        }
    }.show()
}

fun Context.toast(message: String){
    Toast.makeText(this,message,Toast.LENGTH_LONG).show()
}
