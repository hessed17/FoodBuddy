package com.ajou.foodbuddy.ui.profile.main

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import com.ajou.foodbuddy.BaseFragment
import com.ajou.foodbuddy.databinding.FragmentProfileBinding
import com.ajou.foodbuddy.ui.profile.ProfileViewModel

class ProfileMailFragment: BaseFragment<FragmentProfileBinding>() {

    override val viewModel: ProfileViewModel by activityViewModels()

    override fun getViewBinding(): FragmentProfileBinding =
        FragmentProfileBinding.inflate(layoutInflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }
}