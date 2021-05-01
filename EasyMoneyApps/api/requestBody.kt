package com.example.flappyface.api


import com.google.gson.annotations.SerializedName

data class requestBody(
    @SerializedName("geo")
    val geo:String,
    @SerializedName("non-organic")
    val traffic:Boolean)
