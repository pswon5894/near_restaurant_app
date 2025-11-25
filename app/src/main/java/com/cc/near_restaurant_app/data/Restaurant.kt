package com.cc.near_restaurant_app.data

import com.google.android.gms.maps.model.LatLng

data class Restaurant(
    val name: String?,
    val latLng: LatLng,
    val photoReference: String?,
    val address: String?,
    val types: List<String>? = null,  // 추가
    val category: String = "",               // 예: "일식", "햄버거"
    val rating: Double? = null
)