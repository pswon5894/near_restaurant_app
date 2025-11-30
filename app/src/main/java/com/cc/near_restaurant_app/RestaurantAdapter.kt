package com.cc.near_restaurant_app

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cc.near_restaurant_app.databinding.ItemRestaurantBinding
import com.bumptech.glide.Glide
import com.cc.near_restaurant_app.data.Restaurant
import com.cc.near_restaurant_app.retrofit.RetrofitClient

class RestaurantAdapter(
    private val restaurants: List<Restaurant>
) : RecyclerView.Adapter<RestaurantAdapter.RestaurantViewHolder>() {

    private var selectedPosition: Int = -1
    private var itemClickListener: ((Int) -> Unit)? = null

    private var onDetailShowClickListener: ((restaurant: Restaurant) -> Unit)? = null

    inner class RestaurantViewHolder(val binding: ItemRestaurantBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(restaurant: Restaurant, position: Int) {
            binding.tvRestaurantName.text = restaurant.name
            binding.tvRestaurantAddress.text = restaurant.address
            binding.tvRating.text = restaurant.rating?.let { "평점 %.1f".format(it) } ?: "평점 없음"
            binding.tvFoodType.text = restaurant.types?.joinToString(" / ") ?: "타입 없음"


            val photoReference = restaurant.photoReference
            val photoUrl = if (photoReference != null) {
                RetrofitClient.getPhotoUrl(photoReference, 400, 400)
            } else {
                null
            }

            if (!photoUrl.isNullOrEmpty()) {
                // 유효한 URL이 있으면 Glide 로드
                Glide.with(binding.root.context)
                    .load(photoUrl)
                    .placeholder(android.R.drawable.ic_menu_rotate)
                    .error(android.R.drawable.ic_menu_report_image)
                    .into(binding.ivRestaurantPhoto)
            } else {
                // photoReference가 없거나 URL 생성에 실패했을 때
                binding.ivRestaurantPhoto.setImageResource(android.R.drawable.ic_menu_camera)
            }

            binding.root.setBackgroundResource(
                if (position == selectedPosition) android.R.color.holo_blue_light
                else android.R.color.transparent
            )
            binding.root.setOnClickListener { itemClickListener?.invoke(position) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RestaurantViewHolder {
        val binding = ItemRestaurantBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RestaurantViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RestaurantViewHolder, position: Int) {
        holder.bind(restaurants[position], position)

        val restaurant = restaurants[position]
        holder.binding.bDetailShow.setOnClickListener {
            onDetailShowClickListener?.invoke(restaurant)
        }
    }

    override fun getItemCount(): Int = restaurants.size

    fun setSelectedPosition(position: Int) {
        val previous = selectedPosition
        selectedPosition = position
        if (previous != -1) notifyItemChanged(previous)
        notifyItemChanged(selectedPosition)
    }

    fun setOnItemClickListener(listener: (Int) -> Unit) {
        itemClickListener = listener
    }

    fun setOnDetailShowClickListener(listener: (restaurant: Restaurant)-> Unit) {
        this.onDetailShowClickListener = listener
    }
}

//https://developers.google.com/maps/documentation/places/web-service/legacy/details?hl=ko#PlacePhoto
//구글 place api 기존