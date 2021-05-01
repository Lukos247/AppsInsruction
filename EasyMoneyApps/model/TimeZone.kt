package com.example.flappyface.model

data class TimeZone(
    val code: String,
    val current_time: String,
    val gmt_offset: Int,
    val id: String,
    val is_daylight_saving: Boolean
)