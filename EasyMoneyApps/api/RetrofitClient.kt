package com.example.flappyface.api


import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {


    var retrofit:Retrofit? = null
    var baseURL:String = "https://api.ipstack.com/"

    fun Instance(): Retrofit {

        retrofit = Retrofit.Builder()
            .baseUrl(baseURL)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build()

        return retrofit as Retrofit

    }
}