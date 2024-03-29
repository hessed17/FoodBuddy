package com.ajou.foodbuddy.ui.chat.list

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.ajou.foodbuddy.BaseFragment
import com.ajou.foodbuddy.data.firebase.path.Key
import com.ajou.foodbuddy.databinding.FragmentChatMainBinding
import com.ajou.foodbuddy.ui.chat.ChatViewModel
import com.ajou.foodbuddy.ui.chat.detail.ChatDetailActivity
import com.ajou.foodbuddy.ui.chat.invite.ChatInviteActivity
import com.ajou.foodbuddy.ui.chat.sharing.SharingRestaurantToChatRoomActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ChatListFragment : BaseFragment<FragmentChatMainBinding>() {

    private lateinit var adapter: ChatListAdapter
    private val chatViewModel: ChatViewModel by activityViewModels()

    override fun getViewBinding(): FragmentChatMainBinding =
        FragmentChatMainBinding.inflate(layoutInflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initChatRoomList()
        initCreateChatRoomButton()
    }

    private fun initChatRoomList() {
        adapter = ChatListAdapter { item ->
            startActivity(Intent(requireActivity(), ChatDetailActivity::class.java).apply {
                putExtra(ChatDetailActivity.CHATROOM_ID, item.chatRoomId)
                putExtra(ChatDetailActivity.CHATROOM_TITLE, item.title)
            })
        }

        _binding?.chatRoomRecyclerView?.adapter = adapter

        chatViewModel.getChatRoomList()

        lifecycleScope.launch {
            chatViewModel.chatRoomList.collect { itemList ->
                val sortedList = itemList.toMutableList()
                    .sortedByDescending { item -> item.lastUploadTime.toString().toLong() }
                adapter.submitList(sortedList)
            }
        }
    }

    private fun initCreateChatRoomButton() {
        _binding?.createChatRoomButton?.setOnClickListener {
            startActivity(Intent(requireActivity(), ChatInviteActivity::class.java))
        }

    }


}