package com.cc.near_restaurant_app.retrofit.model

data class PlacesResponse (
    val results: List<PlaceResult>?,
    val status: String?
)

// Place Details API의 응답 전체를 담는 모델
data class PlaceDetailsResponse(
    val result: PlaceDetailsResult?,
    val status: String?
)

// Place Details API의 'result' 필드 상세 내용
data class PlaceDetailsResult(
    // Nearby Search 응답에는 없거나 부정확할 수 있으므로 여기서 가져옵니다.
    val formatted_address: String?,
    val formatted_phone_number: String?,

    //  MapActivity에서 필요로 하는 필드
    val rating: Double?,
    val types: List<String>?,
    val photos: List<Photo>?

    // ... 필요한 다른 상세 정보 필드 (예: hours, website, reviews 등)
)