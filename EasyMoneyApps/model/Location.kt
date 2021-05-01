package com.example.flappyface.model

data class Location(
    val calling_code: String,
    val capital: String,
    val country_flag: String,
    val country_flag_emoji: String,
    val country_flag_emoji_unicode: String,
    val geoname_id: Int,
    val is_eu: Boolean,
    val languages: List<Language>
)