package com.ajou.foodbuddy.ui.community.register

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import com.ajou.foodbuddy.BaseFragment
import com.ajou.foodbuddy.databinding.FragmentCommunityRegisterNewReviewBinding
import com.ajou.foodbuddy.ui.community.CommunityViewModel

class CommunityRegisterNewReviewFragment: BaseFragment<FragmentCommunityRegisterNewReviewBinding>() {

    override fun getViewBinding(): FragmentCommunityRegisterNewReviewBinding =
        FragmentCommunityRegisterNewReviewBinding.inflate(layoutInflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}