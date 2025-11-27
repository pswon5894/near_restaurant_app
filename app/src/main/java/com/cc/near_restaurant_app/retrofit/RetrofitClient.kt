package com.cc.near_restaurant_app.retrofit

import com.cc.near_restaurant_app.BuildConfig
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val  BASE_URL = "https://maps.googleapis.com/"
    val instance: PlacesApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PlacesApiService::class.java)
    }
    fun getPhotoUrl(photoReference: String, width: Int, height: Int): String {
        return "${BASE_URL}maps/api/place/photo" +
                "?maxwidth=$width" +
                "&photoreference=$photoReference" +
                "&key=${BuildConfig.PLACES_API_KEY}"
    }
}