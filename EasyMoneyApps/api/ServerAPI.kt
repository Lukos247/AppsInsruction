package com.example.flappyface.api




import com.example.flappyface.model.ServerData
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

public interface ServerAPI {



 //   @Headers("Content-Type: text/html; charset=UTF-8")
    @POST("api/")
    fun getRequest(@Body body: Map<String, @JvmSuppressWildcards Any>) : Call<ServerData> //ServerData

}