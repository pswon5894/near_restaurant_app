package com.cc.near_restaurant_app.retrofit.model

data class Geometry (
    val location: Location?
)

data class Location(
    val lat: Double,
    val lng: Double
)