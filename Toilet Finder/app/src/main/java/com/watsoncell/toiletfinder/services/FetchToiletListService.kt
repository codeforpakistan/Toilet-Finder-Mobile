package com.watsoncell.toiletfinder.services

import android.app.IntentService
import android.content.Intent
import android.util.Log
import com.watsoncell.toiletfinder.models.NearByToiletResponse
import com.watsoncell.toiletfinder.retrofit.Common
import com.watsoncell.toiletfinder.retrofit.IToiletFinderApi
import com.watsoncell.toiletfinder.utils.Constant
import com.watsoncell.toiletfinder.utils.toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FetchToiletListService :
    IntentService("FetchToiletListService") {

    private var mLatitude: Double = 0.0
    private var mLongitude: Double = 0.0
    private var mCity = ""
    private var mService: IToiletFinderApi = Common.getApi()

    private val mRadius = 10


    override fun onHandleIntent(intent: Intent?) {

        mLatitude = intent!!.getDoubleExtra("latitude",0.0)
        mLongitude = intent.getDoubleExtra("longitude",0.0)
        mCity = intent.getStringExtra("city")

        //getting current city toilet list
        getCurrentCityToiletList()
    }

    private fun getCurrentCityToiletList() {
        mService.getNearByToilets(
            mLatitude,
            mLongitude,
            mRadius
        ).enqueue(object : Callback<NearByToiletResponse> {
            override fun onResponse(
                call: Call<NearByToiletResponse>,
                response: Response<NearByToiletResponse>
            ) {
                    if (response.isSuccessful) {
                        val nearByToiletResponse = response.body()
                        Constant.currentCityToiletList = nearByToiletResponse!!.data!!
                    } else{
                        toast("server side error: ${response.errorBody().toString()}")
                        Log.d("arsalan","error: ${response.errorBody().toString()}")
                        Log.d("arsalan","response error: ${response}")
                    }
            }

            override fun onFailure(call: Call<NearByToiletResponse>, t: Throwable) {
                toast("error: ${t.message}")
            }
        })
    }


}