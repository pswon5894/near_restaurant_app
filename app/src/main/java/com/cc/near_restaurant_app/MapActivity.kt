package com.cc.near_restaurant_app

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.cc.near_restaurant_app.databinding.ActivityMapBinding
import com.cc.near_restaurant_app.retrofit.PlacesResponse
import com.cc.near_restaurant_app.retrofit.RetrofitClient
import com.cc.near_restaurant_app.data.Restaurant
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// 불필요한 import 제거: retrofit2.Call, retrofit2.Callback, retrofit2.Response


class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    lateinit var binding : ActivityMapBinding

    private var mMap : GoogleMap? = null
    var currentLat : Double = 0.0
    var currentLng : Double = 0.0

    private val restaurants = mutableListOf<Restaurant>()

    // 코루틴 작업을 관리하기 위한 Job 객체
    private var restaurantLoadJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMapBinding.inflate(layoutInflater)
        binding.rvRestaurants.layoutManager = LinearLayoutManager(this)
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

    // 액티비티가 파괴될 때 코루틴 작업을 취소하여 메모리 누수를 방지합니다.
    override fun onDestroy() {
        super.onDestroy()
        restaurantLoadJob?.cancel()
    }

    private fun setButton() {
        binding.fabCurrentLocation.setOnClickListener {
            val locationProvider = LocationProvider(this@MapActivity)
            val latitude = locationProvider.getLocationLatitude()
            val longitude = locationProvider.getLocationLongitude()

            if (latitude != null && longitude != null) {
                mMap?.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(latitude, longitude),
                        16f
                    )
                )
                setMarker()

                // 주변 식당 다시 불러오기
                loadNearbyRestaurants(latitude, longitude)
            }
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
            // Note: 기존 마커 초기화는 loadNearbyRestaurants에서 처리됨
            val markerOption = MarkerOptions()
            markerOption.position(it.cameraPosition.target)
            markerOption.title("현재 위치")
            markerOption.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
            it.addMarker(markerOption)
        }
    }

    private fun loadNearbyRestaurants(lat: Double, lng: Double) {

        // 기존 작업이 있다면 취소하고 새로 시작
        restaurantLoadJob?.cancel()

        // 🌟 CoroutineScope 블록 전체를 try-catch로 감쌉니다.
        restaurantLoadJob = CoroutineScope(Dispatchers.IO).launch {

            val locationStr = "$lat,$lng"
            val apiKey = BuildConfig.PLACES_API_KEY

            try {
                // 🌟 1. suspend 함수를 호출하고 결과를 'response' 변수에 받습니다. 🌟
                val response = RetrofitClient.instance.getNearbyPlaces(
                    locationStr,
                    1000,
                    "restaurant",
                    apiKey
                )

                // 🌟 2. 메인 스레드로 전환하여 UI 업데이트를 수행합니다. 🌟
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val body = response.body()
                        val results = body?.results ?: emptyList()

                        // UI 초기화
                        restaurants.clear()
                        mMap?.clear()
                        setMarker()

                        // 데이터 처리 및 마커 추가
                        for (place in results) {
                            val p = place.geometry?.location ?: continue
                            val pos = LatLng(p.lat, p.lng)

                            // photoReference 정의 및 사용
                            val photoReference = place.photos?.firstOrNull()?.photoReference
                            val placeName = place.name ?: "이름 없음"
                            val address = place.vicinity ?: "주소 정보 없음"

                            mMap?.addMarker(
                                MarkerOptions()
                                    .position(pos)
                                    .title(placeName)
                            )

                            restaurants.add(Restaurant(placeName, pos, photoReference, address))
                        }

                        // RecyclerView 어댑터 적용
                        binding.rvRestaurants.adapter = RestaurantAdapter(restaurants)
                    } else {
                        // 응답 실패 처리
                    }
                }
                // 🌟 3. 네트워크 관련 예외를 여기서 catch 합니다. 🌟
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}