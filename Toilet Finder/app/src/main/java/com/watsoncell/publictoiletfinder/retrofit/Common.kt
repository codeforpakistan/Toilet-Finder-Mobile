package com.watsoncell.publictoiletfinder.retrofit

class Common{


    companion object{
        private val BASE_URL = "http://publictoiletfinder.totalsanitationkp.gov.pk/public/api/"

        fun getApi() : IToiletFinderApi{
            return RetrofitClient().getRetrofitClient(BASE_URL).create(IToiletFinderApi::class.java)
        }
    }
}