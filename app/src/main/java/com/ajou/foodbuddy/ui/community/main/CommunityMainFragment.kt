package com.ajou.foodbuddy.ui.community.main

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModel
import com.ajou.foodbuddy.BaseFragment
import com.ajou.foodbuddy.databinding.FragmentCommunityBinding
import com.ajou.foodbuddy.ui.community.CommunityViewModel

class CommunityMainFragment : BaseFragment<FragmentCommunityBinding>() {

    override val viewModel: CommunityViewModel by activityViewModels()

    override fun getViewBinding(): FragmentCommunityBinding =
        FragmentCommunityBinding.inflate(layoutInflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}