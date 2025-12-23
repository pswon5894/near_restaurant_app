package com.cc.near_restaurant_app.retrofit.model

data class NearbySearchResponse (
    val places: List<PlaceDetail>?
)

data class PlaceDetail(
    val id: String?,
    val displayName: LocalizedText?,
    val location: LatLngData?,
    val formattedAddress: String?,
    val shortFormattedAddress: String?,
    val rating: Double?,
    val types: List<String>?,
    val websiteUri: String?,
    val photos: List<PhotoDetail>?,

    //프레그먼트 뷰
    val delivery: Boolean?,
    val takeout: Boolean?,
    val servesLunch: Boolean?,
    val servesDinner: Boolean?,
    val restroom: Boolean?,
    val wifi: Boolean?,
    val reservable: Boolean?,
    val parkingOptions: ParkingOptions?,
    val reviews: List<Review>?
)

data class LocalizedText(
    val text: String?,
)
data class LatLngData(
    val latitude: Double,
    val longitude: Double
)

data class Photo (
    val name: String?, // @SerializedName 등을 사용하여 API 필드와 매핑 필요
)
data class PhotoDetail(
    val name: String?
)

//data class DisplayName(
//    val text: String
//)

data class Review(
    val authorAttribution: AuthorAttribution?, //  작성자 정보 객체
    val rating: Double?,
    val text: LocalizedText?,                  //  리뷰 내용 (객체 형태임)
    val relativePublishTimeDescription: String? // "1주일 전" 같은 시간 정보
)

data class AuthorAttribution(
    val displayName: String?,  // 작성자 이름
    val uri: String?,
    val photoUri: String?
)

data class ParkingOptions(
    val hasParking: Boolean? = null
)