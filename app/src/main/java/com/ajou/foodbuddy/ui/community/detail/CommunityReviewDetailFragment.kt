package com.ajou.foodbuddy.ui.community.detail

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import com.ajou.foodbuddy.BaseFragment
import com.ajou.foodbuddy.databinding.FragmentCommunityReviewDetailBinding
import com.ajou.foodbuddy.ui.community.CommunityViewModel

class CommunityReviewDetailFragment : BaseFragment<FragmentCommunityReviewDetailBinding>() {

    override val viewModel: CommunityViewModel by activityViewModels()

    override fun getViewBinding(): FragmentCommunityReviewDetailBinding =
        FragmentCommunityReviewDetailBinding.inflate(layoutInflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}