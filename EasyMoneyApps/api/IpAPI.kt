package com.example.flappyface.api


import com.example.flappyface.model.UserIpData
import io.reactivex.Observable
import retrofit2.http.GET

interface IpAPI {

    @GET("check?access_key=60768222b4cb93ed5becec7a4dab1731")
    fun getUserIP(): Observable<UserIpData>
}