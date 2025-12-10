package com.cc.near_restaurant_app.retrofit.model

data class NearbySearchRequest (
    val includedTypes: List<String>? = null,        // 검색할 장소 유형 (예: restaurant)
    val maxResultCount: Int? =20,
    val rankPreference: String? = "DISTANCE",
    val locationRestriction: LocationRestriction
)

data class LocationRestriction(
    val circle: Circle
)

data class Circle(
    val center: LatLngData,
    val radius: Double
)