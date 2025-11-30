package com.cc.near_restaurant_app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.cc.near_restaurant_app.databinding.ActivityMapBinding
import com.cc.near_restaurant_app.data.Restaurant
import com.cc.near_restaurant_app.retrofit.RetrofitClient
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.*

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    lateinit var binding: ActivityMapBinding
    private var mMap: GoogleMap? = null
    private val markerMap = mutableMapOf<String, Marker>() // 식당 이름 → 마커
    private var selectedMarker: Marker? = null

    var currentLat: Double = 0.0
    var currentLng: Double = 0.0

    private val restaurants = mutableListOf<Restaurant>()
    private var restaurantLoadJob: Job? = null

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var locationManager: LocationManager? = null
    private val LOCATION_PERMISSION_REQUEST_CODE = 1000

    private lateinit var adapter: RestaurantAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMapBinding.inflate(layoutInflater)
        binding.rvRestaurants.layoutManager = LinearLayoutManager(this)
        adapter = RestaurantAdapter(restaurants)
        binding.rvRestaurants.adapter = adapter
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
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

        // 리스트 아이템 클릭 -> 지도 이동 + 마커 강조
        adapter.setOnItemClickListener { position ->
            val restaurant = restaurants[position]
            val latLng = restaurant.latLng

            // 지도 이동
            mMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f))

            // 이전 선택 마커 색 초기화
            selectedMarker?.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
            selectedMarker?.hideInfoWindow()

            // 새로운 마커 색 변경
            val marker = markerMap[restaurant.name]
            markerMap[restaurant.name]?.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
            marker?.showInfoWindow()
            selectedMarker = markerMap[restaurant.name]

            // 하이라이트 처리
            adapter.setSelectedPosition(position)
        }
        //리스트뷰 상세보기 버튼 팝업 다이얼로그
        adapter.setOnDetailShowClickListener { restaurant ->
            val detailFragment = RestaurantDetailFragment.newInstance(restaurant)
            // MapActivity는 AppCompatActivity를 상속하므로 supportFragmentManager를 사용
            detailFragment.show(supportFragmentManager, "RestaurantDetailDialog")
        }
    }

    private fun setButton() {
        binding.fabCurrentLocation.setOnClickListener {
            if (checkLocationPermission()) {
                startLocationUpdateWithManager()
            } else {
                requestLocationPermission()
            }
        }
    }

    private fun checkLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdateWithManager()
            } else {
                Log.d("MapActivity", "Location permission denied by user.")
            }
        }
    }

    private fun startLocationUpdateWithManager() {
        if (locationManager == null) {
            locationManager = LocationManager(fusedLocationClient) { latLng ->
                updateMapAndRestaurants(latLng.latitude, latLng.longitude)
            }
        }
        locationManager?.startLocationUpdates()
    }

    private fun updateMapAndRestaurants(lat: Double, lng: Double) {
        currentLat = lat
        currentLng = lng

        mMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(lat, lng), 16f))
        mMap?.clear()
        markerMap.clear()
        setMarker()
        loadNearbyRestaurants(lat, lng)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap?.apply {
            val currentLocation = LatLng(currentLat, currentLng)
            setMaxZoomPreference(20f)
            setMinZoomPreference(12f)
            moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 16f))
            setMarker()
            loadNearbyRestaurants(currentLat, currentLng)

            setOnMarkerClickListener { marker ->
                val name = marker.title ?: return@setOnMarkerClickListener false
                val index = restaurants.indexOfFirst { it.name == name }
                if (index != -1) {
                    binding.rvRestaurants.smoothScrollToPosition(index)
                    adapter.setSelectedPosition(index)
                }

                // 마커 색 변경
                selectedMarker?.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                selectedMarker = marker
                false
            }
        }
    }

    private fun setMarker() {
        mMap?.addMarker(
            MarkerOptions()
                .position(LatLng(currentLat, currentLng))
                .title("현재 위치")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
        )
    }

    private fun loadNearbyRestaurants(lat: Double, lng: Double) {
        restaurantLoadJob?.cancel()
        restaurantLoadJob = CoroutineScope(Dispatchers.IO).launch {
            val locationStr = "$lat,$lng"
            val apiKey = BuildConfig.PLACES_API_KEY

            try {
                val nearbyResponse = RetrofitClient.instance.getNearbyPlaces(locationStr, 1000, "restaurant", apiKey)
                if (nearbyResponse.isSuccessful) {
                    val nearbyResults = nearbyResponse.body()?.results ?: emptyList()

                    val jobs = nearbyResults.mapNotNull { place ->
                        place.place_id?.let { placeId ->
                            async {
                                try {
                                    val detailsResponse = RetrofitClient.instance.getPlaceDetails(
                                        placeId,
                                        fields = "formatted_address,rating,types,website",
                                        apiKey = apiKey
                                    )
                                    val detailsResult = detailsResponse.body()?.result

                                    Restaurant(
                                        name = place.name ?: "이름 없음",
                                        latLng = LatLng(place.geometry?.location?.lat ?: 0.0, place.geometry?.location?.lng ?: 0.0),
                                        photoReference = place.photos?.firstOrNull()?.photoReference,
                                        address = detailsResult?.formatted_address ?: place.vicinity ?: "주소 정보 없음",
                                        rating = detailsResult?.rating,
                                        types = detailsResult?.types ?: emptyList(),
                                        website = detailsResult?.website
                                    )
                                } catch (e: Exception) {
                                    Log.e("MapActivity", "Place Details failed for ${place.name}: ${e.message}")
                                    Restaurant(
                                        name = place.name ?: "이름 없음",
                                        latLng = LatLng(place.geometry?.location?.lat ?: 0.0, place.geometry?.location?.lng ?: 0.0),
                                        photoReference = place.photos?.firstOrNull()?.photoReference,
                                        address = place.vicinity ?: "주소 정보 없음",
                                        website = null
                                    )
                                }
                            }
                        }
                    }

                    val updatedRestaurants = jobs.awaitAll()

                    withContext(Dispatchers.Main) {
                        restaurants.clear()
                        restaurants.addAll(updatedRestaurants)

                        mMap?.clear()
                        markerMap.clear()
                        setMarker()
                        updatedRestaurants.forEach { r ->
                            r.name?.let { name ->
                                val marker = mMap?.addMarker(MarkerOptions().position(r.latLng).title(name))
                                marker?.let { markerMap[name] = it }
                            }
                        }

                        adapter.notifyDataSetChanged() // 전체 갱신 (최초)
                    }
                } else Log.e("MapActivity", "Nearby Search API Failed: ${nearbyResponse.code()}")
            } catch (e: Exception) {
                Log.e("MapActivity", "네트워크 예외 발생: ${e.message}")
                e.printStackTrace()
            }
        }
    }
}
