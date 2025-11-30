package com.cc.near_restaurant_app

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebViewClient
import androidx.fragment.app.DialogFragment
import com.cc.near_restaurant_app.data.Restaurant
import com.cc.near_restaurant_app.databinding.FragmentRestaurantDetailBinding

class RestaurantDetailFragment : DialogFragment() {

    private var _binding: FragmentRestaurantDetailBinding? = null
    private val binding get() = _binding!!

    companion object {
        private const val ARG_RESTAURANT = "restaurant_data"

        // Factory 메서드를 사용하여 Restaurant 객체를 전달받음
        fun newInstance(restaurant: Restaurant): RestaurantDetailFragment {
            val fragment = RestaurantDetailFragment()
            val args = Bundle().apply {
                putParcelable(ARG_RESTAURANT, restaurant) // Restaurant는 Parcelable이어야 함
            }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentRestaurantDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 팝업으로 전달받은 Restaurant 데이터를 꺼냄
        val restaurant: Restaurant? = arguments?.getParcelable(ARG_RESTAURANT)

        restaurant?.let{
            // 여기에 팝업 레이아웃의 TextView에 대이터를 설정하는 로직 구현
            binding.tvPopupName.text = it.name
            binding.tvPopupAddress.text = it.address
            binding.tvPopupRating.text = it.rating?.let { r-> "평점 %.1f".format(r)}?: "평점 없음"
            //..(사진 로딩, 상세 정보 표시 등)

            val websiteUrl = it.website

            if (!websiteUrl.isNullOrEmpty()) {
                binding.wvRestaurantWebsite.settings.javaScriptEnabled = true
                binding.wvRestaurantWebsite.webViewClient = WebViewClient()
                binding.wvRestaurantWebsite.loadUrl(websiteUrl)
            } else {
                binding.wvRestaurantWebsite.visibility = View.GONE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}