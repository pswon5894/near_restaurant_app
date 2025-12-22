package com.cc.near_restaurant_app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.cc.near_restaurant_app.data.Restaurant
import com.cc.near_restaurant_app.databinding.FragmentRestaurantDetailBinding
import com.cc.near_restaurant_app.util.ReviewAdapter
import com.google.android.libraries.places.api.Places
//import com.google.android.libraries.places.api.model.Place
//import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient

class RestaurantDetailFragment : DialogFragment() {

    private var _binding: FragmentRestaurantDetailBinding? = null
    private val binding get() = _binding!!
    private lateinit var placesClient: PlacesClient

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

    override fun onCreate(savedInstanceState: Bundle?) { // <--- onCreate로 이동
        super.onCreate(savedInstanceState)

        Places.initialize(requireContext(), BuildConfig.NEW_PLACES_API_KEY)
        placesClient = Places.createClient(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentRestaurantDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 팝업으로 전달받은 Restaurant 데이터를 꺼냄
        val restaurant: Restaurant? = arguments?.getParcelable<Restaurant>(ARG_RESTAURANT)

        restaurant?.let{ r ->
            // 여기에 팝업 레이아웃의 TextView에 대이터를 설정하는 로직 구현
            binding.tvPopupName.text = r.name
            binding.tvPopupAddress.text = r.address
            binding.tvPopupRating.text = r.rating?.let { r-> "평점 %.1f".format(r) }?: "평점 없음"
            //..(사진 로딩, 상세 정보 표시 등)

            // 3. New Place API에서 받아온 상세 정보 표시 (이미 객체에 있음!)
            // 서비스 옵션
            binding.tvServesLunch.text = if (r.servesLunch == true) "점심 제공 ✅" else "정보 없음"
            binding.tvServesDinner.text = if (r.servesDinner == true) "저녁 제공 ✅" else "정보 없음"

            // 편의 시설 및 계획
            binding.tvParkingOptions.text = if (r.parkingOptions == true) "주차 가능 🅿️" else "정보 없음"
            binding.tvRestroom.text = if (r.restroom == true) "화장실 있음 🚻" else "정보 없음"

            if (!r.reviews.isNullOrEmpty()) {
                val reviewAdapter = ReviewAdapter(r.reviews)
                binding.rvReviews.apply {
                    layoutManager = LinearLayoutManager(context)
                    adapter = reviewAdapter
                }
//                binding.tvNoReviews.visibility = View.GONE
            } else {
//                binding.tvNoReviews.visibility = View.VISIBLE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}