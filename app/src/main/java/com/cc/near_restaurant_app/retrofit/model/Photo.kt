package com.cc.near_restaurant_app.retrofit.model

data class Photo (
    val photoReference: String?, // @SerializedName 등을 사용하여 API 필드와 매핑 필요
    val height: Int,
    val width: Int
)