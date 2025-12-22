package com.cc.near_restaurant_app.util

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cc.near_restaurant_app.data.RestaurantReview
import com.cc.near_restaurant_app.databinding.ItemReviewBinding

class ReviewAdapter (
    private val reviews: List<RestaurantReview>
) : RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {

    inner class ReviewViewHolder(val binding: ItemReviewBinding) :
            RecyclerView.ViewHolder(binding.root) {

        fun bind(review: RestaurantReview) {
            binding.tvReviewAuthor.text = review.authorName ?: "익명"
            binding.tvReviewRating.text = review.rating?.let { "%.1f".format(it) } ?: "0.0"
            binding.tvReviewDate.text = review.relativePublishTimeDescription
            binding.tvReviewText.text = review.text ?: "내용이 없습니다."
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val binding = ItemReviewBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ReviewViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        holder.bind(reviews[position])
    }

    override fun getItemCount(): Int = reviews.size
}