package com.cc.near_restaurant_app

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.util.Log
import androidx.core.app.ActivityCompat

/**
 * LocationManager를 사용하여 마지막 알려진 위치를 가져오는 클래스입니다.
 *
 * NOTE: 이 클래스가 성공적으로 작동하려면, 호출하는 Activity/Fragment에서
 * Manifest.permission.ACCESS_FINE_LOCATION 권한을 런타임에 미리 요청하고 부여받아야 합니다.
 */
class LocationProvider(val context : Context) {
    private var location : Location? = null
    private var locationManager : LocationManager? = null
    // 코틀린 컨벤션에 따라 TAG를 logTag로 수정했습니다.
    private val logTag = "LocationProvider"

    init {
        // NOTE: 이 클래스는 Activity에서 권한이 부여된 후 호출되어야 합니다.
        location = getLocation()
    }

    // getLastKnownLocation은 권한 체크가 필요함 (Suppress annotation 추가)
    @Suppress("MissingPermission")
    private fun getLocation() : Location? {
        try {
            locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

            var gpsLocation : Location? = null
            var networkLocation : Location? = null

            // GPS 또는 Network 가 활성화 되었는지 확인
            val isGPSEnabled = locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) ?: false
            val isNetworkEnabled = locationManager?.isProviderEnabled(LocationManager.NETWORK_PROVIDER) ?: false

            // GPS, Network 둘 다 비활성화된 경우 위치 획득 불가
            // 논리 오류 수정: !isGPSEnabled && isNetworkEnabled -> !isGPSEnabled && !isNetworkEnabled
            if (!isGPSEnabled && !isNetworkEnabled) {
                Log.d(logTag, "GPS 및 Network Provider 모두 비활성화됨.")
                return null
            }

            // 런타임 권한이 부여되었는지 확인
            val hasFineLocation = ActivityCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            val hasCoarseLocation = ActivityCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

            // 두 권한 중 하나라도 있어야 getLastKnownLocation()을 호출할 수 있습니다.
            if (!hasFineLocation && !hasCoarseLocation) {
                Log.w(logTag, "위치 권한(FINE 또는 COARSE)이 부여되지 않았습니다.")
                return null
            }

            if(isNetworkEnabled){
                networkLocation = locationManager?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            }

            if(isGPSEnabled){
                gpsLocation = locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            }

            // GPS와 Network 위치 모두 유효할 경우, 더 정확한(accuracy 값이 낮은) 위치 선택
            if(gpsLocation != null && networkLocation != null) {
                // 정확도는 값이 낮을수록 좋음 (GPS.accuracy < Network.accuracy)
                if (gpsLocation.accuracy < networkLocation.accuracy){
                    location = gpsLocation
                } else{
                    location = networkLocation
                }
            } else if (gpsLocation != null) {
                location = gpsLocation
            } else if (networkLocation != null) {
                location = networkLocation
            }

        } catch (e: Exception){
            Log.e(logTag, "위치 획득 중 오류 발생: ${e.message}")
            e.printStackTrace()
        }
        return location
    }

    fun getLocationLatitude() : Double?{
        return location?.latitude
    }

    fun getLocationLongitude() : Double?{
        return location?.longitude
    }
}