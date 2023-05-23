package com.ajou.foodbuddy.ui.chat.list

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import com.ajou.foodbuddy.BaseFragment
import com.ajou.foodbuddy.databinding.FragmentChatMainBinding
import com.ajou.foodbuddy.ui.chat.ChatViewModel

class ChatListFragment: BaseFragment<FragmentChatMainBinding>() {

    override fun getViewBinding(): FragmentChatMainBinding = FragmentChatMainBinding.inflate(layoutInflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}