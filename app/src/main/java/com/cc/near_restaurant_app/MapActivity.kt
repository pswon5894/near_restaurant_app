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
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    lateinit var binding : ActivityMapBinding

    private var mMap : GoogleMap? = null
    var currentLat : Double = 0.0
    var currentLng : Double = 0.0

    private val restaurants = mutableListOf<Restaurant>()
    private var restaurantLoadJob: Job? = null

    // 위치 관련 필드
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var locationManager: LocationManager? = null // LocationManager는 같은 패키지에 있다고 가정합니다.
    private val LOCATION_PERMISSION_REQUEST_CODE = 1000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMapBinding.inflate(layoutInflater)
        binding.rvRestaurants.layoutManager = LinearLayoutManager(this)
        setContentView(binding.root)

        // FusedLocationClient 초기화
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Intent에서 초기 위치 정보 수신
        currentLat = intent.getDoubleExtra("currentLat", 0.0)
        currentLng = intent.getDoubleExtra("currentLng", 0.0)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

        setButton()

        // 엣지 투 엣지 설정
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        restaurantLoadJob?.cancel()
        // Activity 종료 시 위치 업데이트 중지
        locationManager?.stopLocationUpdates()
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

    // --- 위치 권한 관련 함수 ---
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
                // 권한 부여 후 위치 업데이트 시작
                startLocationUpdateWithManager()
            } else {
                Log.d("MapActivity", "Location permission denied by user.")
            }
        }
    }

    /**
     * LocationManager를 사용하여 현재 위치를 한 번 업데이트하고 지도와 목록을 갱신합니다.
     */
    private fun startLocationUpdateWithManager() {
        if (locationManager == null) {
            // LocationManager 초기화 및 콜백 정의
            locationManager = LocationManager(fusedLocationClient) { latLng ->
                updateMapAndRestaurants(latLng.latitude, latLng.longitude)
                Log.d("MapActivity", "Location updated via FAB: ${latLng.latitude}, ${latLng.longitude}")
            }
        }
        locationManager?.startLocationUpdates()
    }


    /**
     * 새로운 위치를 기반으로 지도 카메라를 이동시키고 식당 목록을 로드합니다.
     */
    private fun updateMapAndRestaurants(lat: Double, lng: Double) {
        currentLat = lat
        currentLng = lng

        mMap?.animateCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(lat, lng),
                16f
            )
        )

        mMap?.clear()
        setMarker()
        loadNearbyRestaurants(lat, lng)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap?.let{
            val currentLocation = LatLng(currentLat, currentLng)
            it.setMaxZoomPreference(20.0f)
            it.setMinZoomPreference(12.0f)
            it.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 16f))
            setMarker()

            loadNearbyRestaurants(currentLat, currentLng)
        }
    }

    private fun setMarker() {
        mMap?.let{
            // 현재 위치 마커 추가
            val markerOption = MarkerOptions()
            markerOption.position(LatLng(currentLat, currentLng))
            markerOption.title("현재 위치")
            markerOption.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
            it.addMarker(markerOption)
        }
    }

    /**
     * 주변 식당을 로드하고 Place Details API를 병렬로 호출하여 별점, 타입을 가져옵니다.
     */
    private fun loadNearbyRestaurants(lat: Double, lng: Double) {

        restaurantLoadJob?.cancel()

        // Dispatchers.IO에서 네트워크 작업을 수행합니다.
        restaurantLoadJob = CoroutineScope(Dispatchers.IO).launch {

            val locationStr = "$lat,$lng"
            val apiKey = BuildConfig.PLACES_API_KEY

            try {
                // 1. Nearby Search API 호출
                val nearbyResponse = RetrofitClient.instance.getNearbyPlaces(
                    locationStr,
                    1000,
                    "restaurant",
                    apiKey
                )

                if (nearbyResponse.isSuccessful) {
                    val nearbyResults = nearbyResponse.body()?.results ?: emptyList()

                    // 2. 각 식당에 대해 상세 정보(Place Details)를 병렬적으로 가져옵니다.
                    val detailedAddressJobs = nearbyResults.mapNotNull { place ->
                        place.place_id?.let { placeId ->
                            // async를 사용하여 병렬로 Place Details API 호출
                            async {
                                try {
                                    // Place Details API 호출. 필요한 필드를 명시합니다.
                                    val detailsResponse = RetrofitClient.instance.getPlaceDetails(
                                        placeId,
                                        fields = "formatted_address,rating,types",
                                        apiKey = apiKey
                                    )

                                    val detailsResult = detailsResponse.body()?.result

                                    // Nearby Search 데이터와 상세 정보 결합
                                    Restaurant(
                                        name = place.name ?: "이름 없음",
                                        latLng = LatLng(place.geometry?.location?.lat ?: 0.0, place.geometry?.location?.lng ?: 0.0),
                                        photoReference = place.photos?.firstOrNull()?.photoReference,
                                        // 상세 주소, 별점, 타입 정보 추가
                                        address = detailsResult?.formatted_address ?: place.vicinity ?: "주소 정보 없음",
                                        rating = detailsResult?.rating, // Double?
                                        types = detailsResult?.types ?: emptyList() //  null이면 빈 리스트를 전달하여 Argument type mismatch 오류 방지
                                    )
                                } catch (e: Exception) {
                                    // Place Details 호출 실패 시 기본 데이터 사용
                                    Log.e("MapActivity", "Place Details failed for ${place.name}: ${e.message}")
                                    Restaurant(
                                        name = place.name ?: "이름 없음",
                                        latLng = LatLng(place.geometry?.location?.lat ?: 0.0, place.geometry?.location?.lng ?: 0.0),
                                        photoReference = place.photos?.firstOrNull()?.photoReference,
                                        address = place.vicinity ?: "주소 정보 없음"
                                    )
                                }
                            }
                        }
                    }

                    // 3. 모든 상세 정보 가져오기 작업이 완료될 때까지 기다립니다.
                    val updatedRestaurants = detailedAddressJobs.awaitAll()

                    // 4. 메인 스레드에서 UI 업데이트
                    withContext(Dispatchers.Main) {
                        restaurants.clear()
                        mMap?.clear()
                        setMarker()

                        // 데이터 처리 및 마커 추가
                        updatedRestaurants.forEach { restaurant ->
                            // 이름이 null이 아닌 경우에만 마커 추가
                            restaurant.name?.let {
                                mMap?.addMarker(
                                    MarkerOptions()
                                        .position(restaurant.latLng)
                                        .title(it)
                                )
                                restaurants.add(restaurant)
                            }
                        }

                        // RecyclerView 어댑터 적용
                        binding.rvRestaurants.adapter = RestaurantAdapter(restaurants)
                    }
                } else {
                    Log.e("MapActivity", "Nearby Search API Failed: ${nearbyResponse.code()}")
                }
            } catch (e: Exception) {
                Log.e("MapActivity", "네트워크 예외 발생: ${e.message}")
                e.printStackTrace()
            }
        }
    }
}