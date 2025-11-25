package com.cc.near_restaurant_app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cc.near_restaurant_app.databinding.ItemRestaurantBinding
import com.bumptech.glide.Glide
import com.cc.near_restaurant_app.data.Restaurant
import com.cc.near_restaurant_app.retrofit.RetrofitClient
import java.util.Locale

class RestaurantAdapter(private val restaurants: List<Restaurant>) :
    RecyclerView.Adapter<RestaurantAdapter.RestaurantViewHolder>() {

    inner class RestaurantViewHolder(private val binding: ItemRestaurantBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(restaurant: Restaurant) {
            // 이름, 주소
            binding.tvRestaurantName.text = restaurant.name
            binding.tvRestaurantAddress.text = restaurant.address

            // 평점 표시
            binding.tvRating.text = restaurant.rating?.let { "평점 %.1f".format(it) } ?: "평점 없음"
            binding.tvRating.visibility = View.VISIBLE

            // 타입 표시
            binding.tvFoodType.text = restaurant.types?.let { formatTypes(it) } ?: "타입 없음"
            binding.tvFoodType.visibility = View.VISIBLE

            // 사진 표시
            val photoUrl = restaurant.photoReference?.let {
                RetrofitClient.getPhotoUrl(it, 400, 400)
            }

            if (photoUrl != null) {
                Glide.with(binding.root.context)
                    .load(photoUrl)
                    .placeholder(android.R.drawable.ic_menu_rotate)
                    .error(android.R.drawable.ic_menu_report_image)
                    .into(binding.ivRestaurantPhoto)
            } else {
                binding.ivRestaurantPhoto.setImageResource(android.R.drawable.ic_menu_camera)
            }
        }

        private fun formatTypes(types: List<String>): String {
            // API 타입과 한국어 이름 매핑
            val typeMap = mapOf(
                "bakery" to "빵집",
                "cafe" to "카페",
                "bar" to "술집/바",
                "korean_restaurant" to "한식",
                "japanese_restaurant" to "일식",
                "chinese_restaurant" to "중식",
                "thai_restaurant" to "태국 요리",
                "indian_restaurant" to "인도 요리",
                "italian_restaurant" to "이탈리아 요리",
                "american_restaurant" to "양식",
                "mexican_restaurant" to "멕시코 요리",
                "seafood_restaurant" to "해산물",
                "pizza_restaurant" to "피자",
                "steak_house" to "스테이크",
                "burger_joint" to "햄버거",
                "dessert_shop" to "디저트",
                "fast_food_restaurant" to "패스트푸드",
                "sushi_restaurant" to "초밥",
                "ramen_restaurant" to "라멘"
            )

            // 의미 없는 타입 제외
            val irrelevantTypes = listOf(
                "point_of_interest", "establishment", "food", "store",
                "meal_takeaway", "meal_delivery", "restaurant"
            )

            return types
                // 불필요한 타입 필터링
                .filter { !irrelevantTypes.contains(it) }
                // 한국어 변환 또는 영어 포맷팅
                .map { apiType ->
                    // 1. Map에서 한국어 이름을 찾습니다.
                    typeMap[apiType]
                    // 2. 한국어 이름이 없으면 기존 방식대로 언더바 제거 및 첫 글자 대문자화(영어)를 수행합니다.
                        ?: apiType.replace("_", " ")
                            .replaceFirstChar { c -> c.titlecase(Locale.getDefault()) }
                }
                .distinct()
                .take(3)
                .joinToString(" / ")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RestaurantViewHolder {
        val binding = ItemRestaurantBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return RestaurantViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RestaurantViewHolder, position: Int) {
        holder.bind(restaurants[position])
    }

    override fun getItemCount(): Int = restaurants.size
}
