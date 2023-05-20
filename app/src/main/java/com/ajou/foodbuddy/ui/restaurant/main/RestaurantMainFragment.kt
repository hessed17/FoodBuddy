package com.ajou.foodbuddy.ui.restaurant.main

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import com.ajou.foodbuddy.databinding.FragmentRestaurantMainBinding
import com.ajou.foodbuddy.BaseFragment
import com.ajou.foodbuddy.ui.restaurant.RestaurantViewModel
import com.ajou.foodbuddy.ui.restaurant.detail.RestaurantDetailActivity

class RestaurantMainFragment: BaseFragment<FragmentRestaurantMainBinding>() {

    override val viewModel: RestaurantViewModel by activityViewModels()

    override fun getViewBinding(): FragmentRestaurantMainBinding =
        FragmentRestaurantMainBinding.inflate(layoutInflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initNavigateButton()
    }

    private fun initNavigateButton() {
        _binding?.temporaryButton?.setOnClickListener {
            startActivity(Intent(requireActivity(), RestaurantDetailActivity::class.java))
        }
    }
}