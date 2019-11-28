package com.watsoncell.publictoiletfinder.utils

import android.util.Log
import com.watsoncell.publictoiletfinder.models.UserReviewModel
import java.text.DecimalFormat

/*
* Author: Arsalan khan
* Created at: 8/19/2019
* Aim: Calculating each toilet average rating
* */
object CalculateToiletRating {


    fun getAverageRating(feedback: ArrayList<UserReviewModel>): Float {

        val ratingArray = IntArray(feedback.size)
        val decimalFormat = DecimalFormat("#.##")
        try {
            for (i in 0 until feedback.size) {
                val rating = feedback[i].userRating
                ratingArray[i] = rating.toInt()
            }
            return decimalFormat.format(ratingArray.average().toFloat()).toFloat()
        } catch (ex: Exception) {
            ex.printStackTrace()
            Log.d("arsalan", "error: ${ex.message}")
        }
        return 0f
    }
}