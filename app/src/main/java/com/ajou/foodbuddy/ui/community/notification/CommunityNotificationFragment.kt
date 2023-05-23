package com.ajou.foodbuddy.ui.community.notification

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import com.ajou.foodbuddy.BaseFragment
import com.ajou.foodbuddy.databinding.FragmentCommunityNotificationBinding
import com.ajou.foodbuddy.ui.community.CommunityViewModel

class CommunityNotificationFragment : BaseFragment<FragmentCommunityNotificationBinding>() {

    override fun getViewBinding(): FragmentCommunityNotificationBinding =
        FragmentCommunityNotificationBinding.inflate(layoutInflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}