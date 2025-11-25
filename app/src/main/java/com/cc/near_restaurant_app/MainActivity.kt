package com.cc.near_restaurant_app

import android.Manifest
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.cc.near_restaurant_app.databinding.ActivityMainBinding
import java.io.IOException
import java.util.Locale

class MainActivity : AppCompatActivity() {

    lateinit var binding : ActivityMainBinding
    // LocationProvider 인스턴스는 권한 승인 후 초기화되므로 'lateinit' 제거
    private var locationProvider: LocationProvider? = null

    private val PERMISSIONS_REQUEST_CODE = 100

    var latitude : Double? = 0.0
    var longitude : Double? = 0.0

    // ACCESS_BACKGROUND_LOCATION은 필요하다면 여기에 추가해야 합니다.
    // 현재는 포그라운드(앱 사용 중) 위치 권한만 요청합니다.
    val REQUIRED_PERMISSIONS = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    lateinit var getGPSPermissionLauncher : ActivityResultLauncher<Intent>

    // ActivityResultCallback 람다식으로 변경 (Kotlin 관용적 표현)
    val startMapActivityResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if(result.resultCode == Activity.RESULT_OK){
            latitude = result.data?.getDoubleExtra("latitude", 0.0) ?: 0.0
            longitude = result.data?.getDoubleExtra("longitude", 0.0) ?: 0.0
            updateUI() // 지도에서 돌아온 후 UI 업데이트
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        // 1. 초기 UI 업데이트 로직 제거 (권한 문제 방지)
        // 2. 권한 및 서비스 체크 시작
        checkAllPermissions()
        setFab()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    // 초기 실행 또는 권한 확인 후 위치 획득 및 UI 업데이트를 시작하는 함수
    private fun startLocationAndUIUpdate() {
        locationProvider = LocationProvider(this@MainActivity)

        // 이전에 MapActivity에서 설정한 값이 없다면 현재 위치를 가져옵니다.
        if(latitude == 0.0 && longitude == 0.0) {
            latitude = locationProvider?.getLocationLatitude()
            longitude = locationProvider?.getLocationLongitude()
        }

        updateUI() // 위치 정보가 확보된 후 UI 업데이트 시작
    }

    private fun checkAllPermissions(){
        if(!isLocationServicesAvailable()){
            showDialogForLocationServiceSetting()
        } else{
            isRunTimePermissionGranted()
        }
    }

    private fun isLocationServicesAvailable() : Boolean {
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager

        return (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER))
    }

    private fun isRunTimePermissionGranted() {
        val fineLocationGranted = ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarseLocationGranted = ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

        if (fineLocationGranted && coarseLocationGranted) {
            // 권한이 이미 모두 부여된 상태: 바로 위치 획득 시작
            startLocationAndUIUpdate()
        } else {
            // 권한 요청
            ActivityCompat.requestPermissions(this@MainActivity, REQUIRED_PERMISSIONS, PERMISSIONS_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(requestCode == PERMISSIONS_REQUEST_CODE && grantResults.isNotEmpty()){
            var checkResult = true

            for (result in grantResults){
                if(result != PackageManager.PERMISSION_GRANTED){
                    checkResult = false
                    break
                }
            }

            if(checkResult) {
                // 중요 수정: 권한이 허용되었을 때만 위치 획득 및 UI 업데이트 시작
                startLocationAndUIUpdate()
            } else {
                Toast.makeText(this@MainActivity, "위치 권한이 거부되었습니다. 앱을 다시 실행하여 권한을 허용해주세요.", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    private fun showDialogForLocationServiceSetting() {
        getGPSPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {result ->
            if(result.resultCode == Activity.RESULT_OK) {
                if(isLocationServicesAvailable()) {
                    isRunTimePermissionGranted() // GPS 활성화 후 런타임 권한 체크로 진행
                } else {
                    Toast.makeText(this@MainActivity, "위치 서비스를 사용할 수 없습니다.", Toast.LENGTH_LONG).show()
                    finish()
                }
            }
        }
        val builder : AlertDialog.Builder = AlertDialog.Builder(this@MainActivity)
        builder.setTitle("위치 서비스 비활성화")
        builder.setMessage("위치 서비스가 꺼져있습니다. 설정해야 앱을 사용할 수 있습니다.")
        builder.setCancelable(true)
        builder.setPositiveButton("설정", DialogInterface.OnClickListener { dialogInterface, i ->
            val callGPSSettingIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            getGPSPermissionLauncher.launch(callGPSSettingIntent)
        })
        builder.setNegativeButton("취소", DialogInterface.OnClickListener {dialogInterface, i ->
            dialogInterface.cancel()
            Toast.makeText(this@MainActivity, "위치 서비스를 사용할 수 없습니다.", Toast.LENGTH_LONG).show()
            finish()
        })
        builder.create().show()
    }

    // UI 업데이트 로직 (위치 정보가 있을 때만 실행)
    private fun updateUI() {
        if(latitude != null && longitude != null && latitude != 0.0 && longitude != 0.0) {
            //1. 현재 위치 가져오고 UI 업데이트
            val address = getCurrentAddress(latitude!!, longitude!!)

            address?.let{
                // 주소가 성공적으로 반환되었을 때만 UI 업데이트
                binding.tvLocationTitle.text="${it.subLocality ?: "주변 지역 정보 없음"}"
                //도로명
                binding.tvLocationSub.text="${it.countryName} ${it.adminArea}"
            } ?: run {
                // 지오코더 실패 시
                binding.tvLocationTitle.text="위치 정보 획득"
                binding.tvLocationSub.text="(${latitude}, ${longitude})"
            }
        }else{
            Toast.makeText(this, "위도, 경도 정보를 가져올 수 없습니다. 위치 권한 또는 GPS 설정을 확인해주세요.", Toast.LENGTH_LONG).show()
        }
    }

    private fun setFab(){
        binding.fab.setOnClickListener {
            val intent = Intent(this, MapActivity::class.java)
            intent.putExtra("currentLat", latitude)
            intent.putExtra("currentLng", longitude)
            startMapActivityResult.launch(intent)
        }
    }

    private fun getCurrentAddress (latitude : Double, longitude : Double) : Address?{
        val geoCoder = Geocoder(this, Locale.KOREA)
        val addresses: List<Address>?

        addresses = try {
            // Android 13 (API 33) 이상에서는 deprecated 되었으나, 하위 버전 호환성을 위해 사용
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // 비동기 처리가 필요하지만, 현재는 동기식 호출을 유지 (LocationManager 사용과 일관성을 위해)
                geoCoder.getFromLocation(latitude, longitude, 1)
            } else {
                geoCoder.getFromLocation(latitude, longitude, 1)
            }

        }catch (ioException : IOException){
            Toast.makeText(this, "지오코더 서비스를 이용할 수 없습니다.", Toast.LENGTH_LONG).show()
            return null
        }catch (illegalArgumentException : java.lang.IllegalArgumentException){
            Toast.makeText(this, "잘못된 위도, 경도입니다.", Toast.LENGTH_LONG).show()
            return null
        }

        if(addresses.isNullOrEmpty()) {
            Toast.makeText(this, "주소가 발견되지 않았습니다.", Toast.LENGTH_LONG).show()
            return null
        }

        return addresses[0]
    }
}