package com.ajou.foodbuddy.ui.restaurant.main

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import com.ajou.foodbuddy.databinding.FragmentRestaurantBinding
import com.ajou.foodbuddy.BaseFragment
import com.ajou.foodbuddy.ui.restaurant.RestaurantViewModel

class RestaurantMainFragment: BaseFragment<FragmentRestaurantBinding>() {

    override val viewModel: RestaurantViewModel by activityViewModels()

    override fun getViewBinding(): FragmentRestaurantBinding =
        FragmentRestaurantBinding.inflate(layoutInflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}