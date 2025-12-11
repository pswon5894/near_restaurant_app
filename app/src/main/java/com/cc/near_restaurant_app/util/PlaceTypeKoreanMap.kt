package com.cc.near_restaurant_app.util

object PlaceTypeKoreanMap{

    val typeToKorean = mapOf(
        "restaurant" to "음식점",
        "korean_restaurant" to "한식",
        "japanese_restaurant" to "일식",
        "chinese_restaurant" to "중식",
        "vietnamese_restaurant" to "베트남 음식",
        "indian_restaurant" to "인도 음식",
        "italian_restaurant" to "이탈리아 음식",

        "cafe" to "카페",
        "bakery" to "베이커리",
        "bar" to "술집",
        "meal_takeaway" to "포장 음식",
        "meal_delivery" to "배달 음식",
        "fast_food_restaurant" to "패스트푸드",
        "hamburger_restaurant" to "패스트푸드",
        "food" to "음식",
        "supermarket" to "슈퍼마켓",
        "convenience_store" to "편의점",
    )

    fun toKorean(types: List<String>?): String {
        if (types.isNullOrEmpty()) return "기타"

        // types 중에서 번역 가능한 것 우선 찾기
        types.forEach { type ->
            typeToKorean[type]?.let { return it }
        }

        return "기타"
    }
}