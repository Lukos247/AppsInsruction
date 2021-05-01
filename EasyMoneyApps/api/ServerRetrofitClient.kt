package com.example.flappyface.api


import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

object ServerRetrofitClient {

    var retrofit: Retrofit? = null
    var baseURL: String = "https://everygrowapp.site/"

    fun Instance(): Retrofit {

            var httpLogging = HttpLoggingInterceptor()
            httpLogging.level = HttpLoggingInterceptor.Level.BODY
         val okHttp = OkHttpClient.Builder().addInterceptor(httpLogging).build()


        retrofit = Retrofit.Builder().baseUrl(baseURL)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create()).client(okHttp)
            .build()

        return retrofit as Retrofit

    }
}