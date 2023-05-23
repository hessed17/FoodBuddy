package com.ajou.foodbuddy.ui.profile.main

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import com.ajou.foodbuddy.BaseFragment
import com.ajou.foodbuddy.databinding.FragmentProfileMainBinding
import com.ajou.foodbuddy.ui.profile.ProfileViewModel

class ProfileMailFragment: BaseFragment<FragmentProfileMainBinding>() {

    override fun getViewBinding(): FragmentProfileMainBinding =
        FragmentProfileMainBinding.inflate(layoutInflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }
}