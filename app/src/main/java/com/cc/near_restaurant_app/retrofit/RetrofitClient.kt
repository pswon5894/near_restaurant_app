package com.cc.near_restaurant_app.retrofit

import com.cc.near_restaurant_app.BuildConfig
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val  BASE_URL = "https://places.googleapis.com/"
    val instance: PlacesApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PlacesApiService::class.java)
    }
    fun getPhotoUrl(photoName: String, maxHeight: Int = 400): String {
        return "$BASE_URL/v1/${photoName}/media" +
                "?maxHeightPx=$maxHeight" +
                "&key=${BuildConfig.NEW_PLACES_API_KEY}"
    }
}