package com.cc.near_restaurant_app.retrofit.model

import com.cc.near_restaurant_app.retrofit.Geometry

data class PlaceResult (
    val name: String?,
    // 주소 정보 (nearbysearch API는 보통 vicinity를 제공)
    val vicinity: String?,
    val geometry: Geometry?,
    val photos: List<Photo>?
)
