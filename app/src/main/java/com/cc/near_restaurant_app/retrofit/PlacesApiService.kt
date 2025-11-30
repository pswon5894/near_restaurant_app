package com.cc.near_restaurant_app.retrofit

import com.cc.near_restaurant_app.BuildConfig
import com.cc.near_restaurant_app.retrofit.model.PlaceDetailsResponse
import com.cc.near_restaurant_app.retrofit.model.PlacesResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
interface PlacesApiService {

    @GET("maps/api/place/nearbysearch/json")
    suspend fun getNearbyPlaces(
        @Query("location") location: String,
        @Query("radius") radius: Int = 1000,
        //1km 주변
        @Query("type") type: String = "restaurant",
        @Query("key") apiKey: String= BuildConfig.PLACES_API_KEY,
        @Query("language") language: String = "ko"
    ): Response<PlacesResponse>

    @GET("maps/api/place/details/json")
    suspend fun getPlaceDetails(
        @Query("place_id") placeId: String,
        @Query("fields") fields: String =
            "name,rating,formatted_phone_number,formatted_address,types,photos, website",
        @Query("key") apiKey: String = BuildConfig.PLACES_API_KEY,
        @Query("language") language: String = "ko"
    ): Response<PlaceDetailsResponse> //  PlaceDetailsResponse는 이 API의 응답을 파싱할 데이터 클래스여야 합니다.
}