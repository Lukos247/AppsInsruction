package com.example.flappyface.model

import android.util.Log
import com.yandex.metrica.AppMetricaDeviceIDListener



class AppMetricaDevieID:AppMetricaDeviceIDListener{

    private var id:String? = ""
    var state = false

    override fun onLoaded(p0: String?) {
        id = p0

    }

    override fun onError(p0: AppMetricaDeviceIDListener.Reason) {
        TODO("Not yet implemented")
    }

    suspend fun getId():String {
        return id.toString()
    }


    

}