package com.cc.near_restaurant_app.data

import android.os.Parcelable
import com.cc.near_restaurant_app.util.PlaceTypeKoreanMap
import com.google.android.gms.maps.model.LatLng
import kotlinx.parcelize.Parcelize

@Parcelize
data class Restaurant(
    val name: String,
    val latLng: LatLng,
    val photoName: String?,
    val address: String,
    val types: List<String>? = null,  // 추가
//    val type: String = "",               // 예: "일식", "햄버거"
    val rating: Double? = null,
    val website: String? = null, //  홈페이지 URL 필드 추가
    val placeId: String? = null
) : Parcelable {

    val category: String
        get() = PlaceTypeKoreanMap.toKorean(types)
}