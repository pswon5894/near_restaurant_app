package com.cc.near_restaurant_app

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cc.near_restaurant_app.databinding.ItemRestaurantBinding // 실제 바인딩 클래스로 변경
import com.bumptech.glide.Glide
import com.cc.near_restaurant_app.data.Restaurant
import com.cc.near_restaurant_app.retrofit.RetrofitClient

class RestaurantAdapter(private val restaurants: List<Restaurant>) :
    RecyclerView.Adapter<RestaurantAdapter.RestaurantViewHolder>(){

    inner class RestaurantViewHolder(private val binding: ItemRestaurantBinding) :
            RecyclerView.ViewHolder(binding.root) {
        fun bind(restaurant: Restaurant) {
            binding.tvRestaurantName.text = restaurant.name
            binding.tvRestaurantAddress.text = restaurant.address

            val photoReference = restaurant.photoReference

            if (!photoReference.isNullOrEmpty()) {

                // Google Places API에서 사진을 가져올 최종 URL을 생성합니다.
                // YOUR_API_KEY는 MapActivity에서 사용하던 BuildConfig.PLACES_API_KEY 입니다.
                // MapActivity에서 API 키를 가져오지 못하면 이 코드는 작동하지 않습니다.
                // 키를 직접 Adapter로 전달하거나, MapActivity에서 처리해야 합니다.
                val photoUrl = RetrofitClient.getPhotoUrl(
                    photoReference,
                    width = 400,
                    height = 400
                )
                Glide.with(itemView.context)
                    .load(photoUrl) // 생성한 URL 로드
                    .placeholder(android.R.drawable.ic_menu_rotate) // 로딩 중 이미지 (필요하다면)
                    .error(android.R.drawable.ic_delete) // 로드 실패 시 이미지 (필요하다면)
                    .into(binding.ivRestaurantPhoto) // ImageView에 표시
            } else {
                // 사진 정보가 없을 경우 기본 이미지나 빈 이미지를 표시
                binding.ivRestaurantPhoto.setImageResource(android.R.drawable.ic_menu_camera)
            }
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RestaurantViewHolder {
        // R.layout.item_restaurant 대신 바인딩 클래스 사용
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