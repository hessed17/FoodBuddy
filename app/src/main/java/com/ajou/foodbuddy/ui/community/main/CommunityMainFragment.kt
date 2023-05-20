package com.ajou.foodbuddy.ui.community.main

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import com.ajou.foodbuddy.BaseFragment
import com.ajou.foodbuddy.databinding.FragmentCommunityMainBinding
import com.ajou.foodbuddy.ui.community.CommunityViewModel

class CommunityMainFragment : BaseFragment<FragmentCommunityMainBinding>() {

    override val viewModel: CommunityViewModel by activityViewModels()

    override fun getViewBinding(): FragmentCommunityMainBinding =
        FragmentCommunityMainBinding.inflate(layoutInflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}