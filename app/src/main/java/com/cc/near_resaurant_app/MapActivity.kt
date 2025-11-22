package com.cc.near_resaurant_app

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.cc.near_resaurant_app.databinding.ActivityMapBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.internal.IGoogleMapDelegate
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    lateinit var binding : ActivityMapBinding

    private var mMap : GoogleMap? = null
    var currentLat : Double = 0.0
    var currentLng : Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentLat = intent.getDoubleExtra("currentLat", 0.0)
        currentLng = intent.getDoubleExtra("currentLng", 0.0)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

        setButton()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setButton() {
        binding.fabCurrentLocation.setOnClickListener {
            val locationProvider = LocationProvider(this@MapActivity)
            val latitude = locationProvider.getLocationLatitude()
            val longitude = locationProvider.getLocationLongitude()

            mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(latitude!!,longitude!!), 16f))
            setMarker()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap?.let{
            val currentLocation = LatLng(currentLat, currentLng)
            it.setMaxZoomPreference(20.0f)
            it.setMinZoomPreference(12.0f)
            it.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 16f))
            setMarker()

            // 주변 식당 불러오기
            loadNearbyRestaurants(currentLat, currentLng)
        }
    }

    private fun setMarker() {
        mMap?.let{
            it.clear()
            val markerOption = MarkerOptions()
            markerOption.position(it.cameraPosition.target)
            markerOption.title("마커 위치")
            val marker = it.addMarker(markerOption)
        }
    }

    private fun loadNearbyRestaurants(lat: Double, lng: Double) {

        val locationStr = "$lat,$lng"
        val apiKey = BuildConfig.PLACES_API_KEY

        val call = RetrofitClient.instance.getNearbyPlaces(
            locationStr,
            1000,
            "restaurant",
            apiKey
        )

        call.enqueue(object : Callback<PlacesResponse> {
            override fun onResponse(
                call: Call<PlacesResponse>,
                response: Response<PlacesResponse>
            ) {
                if (response.isSuccessful) {
                    val body = response.body() ?: return

                    for (place in body.results) {
                        val p = place.geometry?.location ?: continue

                        val pos = LatLng(p.lat, p.lng)

                        mMap?.addMarker(
                            MarkerOptions()
                                .position(pos)
                                .title(place.name)
                        )
                    }
                }
            }

            override fun onFailure(call: Call<PlacesResponse>, t: Throwable) {
                t.printStackTrace()
            }
        })
    }
}