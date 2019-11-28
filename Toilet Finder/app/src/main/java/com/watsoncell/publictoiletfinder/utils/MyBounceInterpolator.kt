package com.watsoncell.publictoiletfinder.utils

import android.view.animation.Interpolator
import kotlin.math.cos


class MyBounceInterpolator(amplitude : Double, frequency : Double) : Interpolator {
    private val mAmplitude = amplitude
    private val mFrequency = frequency

    override fun getInterpolation(time: Float): Float {
        return  (-1 * Math.pow(Math.E, - time/ mAmplitude) *
                cos(mFrequency * time) + 1).toFloat()
    }
}