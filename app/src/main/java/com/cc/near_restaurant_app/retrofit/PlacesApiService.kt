package com.cc.near_restaurant_app.retrofit

import com.cc.near_restaurant_app.BuildConfig
import com.cc.near_restaurant_app.retrofit.model.NearbySearchRequest
import com.cc.near_restaurant_app.retrofit.model.NearbySearchResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
interface PlacesApiService {

    @POST("v1/places:searchNearby")
    suspend fun searchNearby(
        @Body body: NearbySearchRequest,
        @Header("X-Goog-Api-Key") apiKey: String = BuildConfig.NEW_PLACES_API_KEY,
        @Header("X-Goog-FieldMask") fieldMask: String =
            "places.displayName,places.location,places.formattedAddress,places.rating,places.types,places.websiteUri,places.photos"
    ): Response<NearbySearchResponse>

//    @GET("v1/places/{placeId}")
//    suspend fun getPlaceDetails(
//        @Path("placeId") placeId: String,
//        @Query("fields") fields: String =
//            "id,displayName, formattedAddress,location,rating,photos,types,websiteUri",
//        @Query("key") apiKey: String = BuildConfig.NEW_PLACES_API_KEY
//    ): Response<PlaceDetailsResponse>

    @GET("v1/{photoName}/media")
    suspend fun getPhoto(
        @Path("photoName", encoded = true) photoName: String,
        @Query("maxHeightPx") maxHeight: Int = 400,
        @Query("key") apiKey: String = BuildConfig.NEW_PLACES_API_KEY
    ): Response<ResponseBody>
}

//Retrofit에서 @Path는 URL 경로(Path) 안에 변수를 넣을 때 사용
//신 Places API는 RESTFUL 구조라서 리소스를 URL로 직접 접근

//이전 Places API는 GET 요청만 썼기 때문에 Body가 필요 없음.
//하지만 신 Places API는 POST 기반