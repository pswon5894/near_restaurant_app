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
    val rating: Double? = null,
    val website: String? = null, //  홈페이지 URL
    val placeId: String? = null,

    //프레그먼트 뷰
    val delivery: Boolean? = null,
    val takeout: Boolean? = null,
    val servesLunch: Boolean? = null,
    val servesDinner: Boolean? = null,
    val restroom: Boolean? = null,
    val wifi: Boolean? = null,
    val reservable: Boolean? = null,
    val parkingOptions: Boolean? = null, // 주차 유무 (ParkingOptions 객체 대신 단순 유무

    //val phone

    val reviews: List<RestaurantReview>? = null
) : Parcelable {

    val category: String
        get() = PlaceTypeKoreanMap.toKorean(types)
}

//Restaurant 객체에 포함하기 위해 Parcelable을 구현한 리뷰 데이터 클래스
@Parcelize
data class RestaurantReview(
    val authorName: String?,
    val rating: Double?,
    val text: String?,
    val relativePublishTimeDescription: String?
) : Parcelable