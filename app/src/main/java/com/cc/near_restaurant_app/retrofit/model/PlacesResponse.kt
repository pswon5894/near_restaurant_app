package com.cc.near_restaurant_app.retrofit.model

data class NearbySearchResponse (
    val places: List<PlaceDetail>?
)

data class PlaceDetail(
    val id: String?,
    val displayName: LocalizedText?,
    val location: LatLngData?,
    val formattedAddress: String?,
    val rating: Double?,
    val types: List<String>?,
    val websiteUri: String?,
    val photos: List<PhotoDetail>?,
    val shortFormattedAddress: String? = null
)

data class LocalizedText(
    val text: String?
)
data class LatLngData(
    val latitude: Double,
    val longitude: Double
)
data class PhotoDetail(
    val name: String?
)