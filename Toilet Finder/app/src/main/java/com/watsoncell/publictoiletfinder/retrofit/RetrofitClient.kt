package com.watsoncell.publictoiletfinder.retrofit

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


class RetrofitClient{
    var mRetrofit: Retrofit? = null

    fun getRetrofitClient(baseUrl: String): Retrofit {


        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(1, TimeUnit.MINUTES)
            .readTimeout(35, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .build()


        val gson = GsonBuilder()
            .setLenient()
            .create()

        if (mRetrofit == null) {
            mRetrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
        }
        return mRetrofit!!
    }

}