package com.ajou.foodbuddy.ui.chat.detail

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.ajou.foodbuddy.databinding.ActivityChatroomDetailBinding
import com.ajou.foodbuddy.ui.chat.ChatViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ChatDetailActivity: AppCompatActivity() {

    private lateinit var binding: ActivityChatroomDetailBinding
    private val viewModel: ChatViewModel by viewModels()
    private lateinit var adapter: ChatMessageAdapter

    private lateinit var chatRoomId: String
    private lateinit var chatRoomTitle: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatroomDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initChatRoomId()
        initMessageList()
        initMessageInputView()
        initSendMessageButton()
    }

    private fun initChatRoomId() {
        chatRoomId = intent.getStringExtra(CHATROOM_ID).toString()
    }

    private fun initMessageList() {
        adapter = ChatMessageAdapter(viewModel.getUserId())
        viewModel.getChatMessageList(chatRoomId)

        binding.chatMessageRecyclerView.adapter = adapter

        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)

                binding.chatMessageRecyclerView.layoutManager?.smoothScrollToPosition(
                    binding.chatMessageRecyclerView,
                    null,
                    adapter.itemCount
                )
            }
        })

        lifecycleScope.launch {
            viewModel.chatMessageList.collect {
                adapter.submitList(it)
            }
        }
    }

    private fun initMessageInputView() {
        binding.messageInputEditTextView.addTextChangedListener {
            binding.sendButton.isClickable = binding.messageInputEditTextView.length() > 1
        }
    }

    private fun initSendMessageButton() {
        binding.sendButton.setOnClickListener {
            val messageContent = binding.messageInputEditTextView.text.toString()
            viewModel.sendMessage(chatRoomId, messageContent)
            binding.messageInputEditTextView.text.clear()

        }
    }

    companion object {
        const val SELECTED_FRIEND_LIST = "selectedFriendList"
        const val CHATROOM_ID = "ChatRoomId"
    }
}