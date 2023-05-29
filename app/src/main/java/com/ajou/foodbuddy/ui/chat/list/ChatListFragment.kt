package com.ajou.foodbuddy.ui.chat.list

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.ajou.foodbuddy.BaseFragment
import com.ajou.foodbuddy.databinding.FragmentChatMainBinding
import com.ajou.foodbuddy.ui.chat.ChatViewModel
import com.ajou.foodbuddy.ui.chat.detail.ChatDetailActivity
import com.ajou.foodbuddy.ui.chat.invite.ChatInviteActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ChatListFragment: BaseFragment<FragmentChatMainBinding>() {

    private lateinit var adapter: ChatListAdapter
    private val chatViewModel: ChatViewModel by activityViewModels()

    override fun getViewBinding(): FragmentChatMainBinding = FragmentChatMainBinding.inflate(layoutInflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initChatRoomList()
        initCreateChatRoomButton()
    }

    private fun initChatRoomList() {
        adapter = ChatListAdapter { item ->
            startActivity(Intent(requireActivity(), ChatDetailActivity::class.java).apply {
                putExtra(ChatDetailActivity.CHATROOM_ID, item.chatRoomId)
            })
        }

        _binding?.chatRoomRecyclerView?.adapter = adapter

        chatViewModel.getChatRoomList()

        lifecycleScope.launch {
            chatViewModel.chatRoomList.collect {
                adapter.submitList(it)
            }
        }
    }

    private fun initCreateChatRoomButton() {
        _binding?.createChatRoomButton?.setOnClickListener {
            startActivity(Intent(requireActivity(), ChatInviteActivity::class.java))
        }

    }
}