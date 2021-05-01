package com.example.flappyface.model

data class UserIpData(
    val city: String,
    val connection: Connection,
    val continent_code: String,
    val continent_name: String,
    val country_code: String,
    val country_name: String,
    val currency: Currency,
    val ip: String,
    val latitude: Double,
    val location: Location,
    val longitude: Double,
    val region_code: String,
    val region_name: String,
    val time_zone: TimeZone,
    val type: String,
    val zip: String
)