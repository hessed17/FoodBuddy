package com.ajou.foodbuddy.ui.restaurant.detail

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import com.ajou.foodbuddy.BaseFragment
import com.ajou.foodbuddy.databinding.FragmentRestaurantDetailBinding
import com.ajou.foodbuddy.ui.restaurant.RestaurantViewModel

class RestaurantDetailFragment: BaseFragment<FragmentRestaurantDetailBinding>() {

    override val viewModel: RestaurantViewModel by activityViewModels()

    override fun getViewBinding(): FragmentRestaurantDetailBinding =
        FragmentRestaurantDetailBinding.inflate(layoutInflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}