package com.cc.near_restaurant_app.retrofit.model

data class PlaceResult (
    val business_status: String?,
    //  필수: Place Details를 호출하기 위한 ID
    val place_id: String?,
    val name: String?,
    // 주소 정보 (nearbysearch API는 보통 vicinity를 제공)
    val vicinity: String?,
    val geometry: Geometry?,
    val photos: List<Photo>?,
    // Nearby Search 결과에 이미 별점이나 타입이 포함되어 있다면 여기에 추가 가능
     val rating: Double?,
     val types: List<String>?
)