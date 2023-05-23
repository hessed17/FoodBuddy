package com.ajou.foodbuddy.ui.profile.search

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import com.ajou.foodbuddy.BaseFragment
import com.ajou.foodbuddy.databinding.FragmentSearchUserBinding
import com.ajou.foodbuddy.ui.profile.ProfileViewModel

class SearchUserFragment : BaseFragment<FragmentSearchUserBinding>() {
    override fun getViewBinding(): FragmentSearchUserBinding =
        FragmentSearchUserBinding.inflate(layoutInflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}