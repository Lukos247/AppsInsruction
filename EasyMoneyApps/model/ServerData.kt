package com.example.flappyface.model

data class ServerData(
    val classes: String,
    val error: Boolean,
    val open_wb: Boolean,
    val url: String,
    val xml: String
)