package com.cc.near_restaurant_app.retrofit

import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

// API 전체 응답 모델
data class PlacesResponse(
    val results: List<PlaceResult>?, // List는 null일 수 없지만, API 응답은 결과가 없을 때 null 대신 빈 리스트를 반환할 수 있습니다. 안전하게 ? 추가
    val status: String? // 응답 상태(OK, ZERO_RESULTS 등) 필드 추가
)

// 개별 식당 정보 모델
data class PlaceResult(
    val name: String?,

    //  1. 주소 필드를 'vicinity'로 변경 및 추가
    // Nearby Search API는 보통 간단한 주소(vicinity)를 제공합니다.
    val vicinity: String?,

    val geometry: Geometry?,

    val photos: List<Photo>?,

    // 2. 다른 필드 (예: place_id)도 추가해둡니다.
    val place_id: String?
)

// 위치 정보 모델
data class Geometry(
    val location: LocationData? // null 가능성 추가
)

// 위도/경도 데이터 모델
data class LocationData(
    val lat: Double,
    val lng: Double
)

// 사진 정보 모델
data class Photo(
    val photoReference: String?,
    val height: Int?, // 필드 추가
    val width: Int?   // 필드 추가
)

interface PlacesApiService {

    @GET("maps/api/place/nearbysearch/json")
    suspend fun getNearbyPlaces(
        @Query("location") location: String,
        @Query("radius") radius: Int = 1000,
        //1km 주변
        @Query("type") type: String = "restaurant",
        @Query("key") apiKey: String,
        @Query("language") language: String = "ko"
    ): Response<PlacesResponse>

}
