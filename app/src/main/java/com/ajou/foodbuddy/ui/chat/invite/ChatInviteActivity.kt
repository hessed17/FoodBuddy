package com.ajou.foodbuddy.ui.chat.invite

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.ajou.foodbuddy.databinding.ActivityCreateGroupChatBinding
import com.ajou.foodbuddy.ui.chat.ChatViewModel
import com.ajou.foodbuddy.ui.chat.detail.ChatDetailActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ChatInviteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateGroupChatBinding
    private lateinit var adapter: FriendListAdapter
    private val viewModel: InviteViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateGroupChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initToolbar()
        initSearchFriendView()
        initFriendList()
    }

    private fun initToolbar() {
        with(binding) {
            backButton.setOnClickListener {
                finish()
            }

            completeButton.setOnClickListener {
                val selectedList = adapter.getSelectedFriendList()
                if (selectedList.isNotEmpty()) {
                    val chatRoomId = viewModel.createNewChatRoom(selectedList)
                    startActivity(
                        Intent(
                            this@ChatInviteActivity,
                            ChatDetailActivity::class.java
                        ).apply {
                            putExtra(ChatDetailActivity.CHATROOM_ID, chatRoomId)
                        })
                } else {
                    Toast.makeText(this@ChatInviteActivity, "최소 한 명을 선택해주세요.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun initSearchFriendView() {
        binding.searchNicknameEditTextView.apply {
            setOnEditorActionListener { editText, _, _ ->
                currentFocus?.let { view ->
                    val inputMethodManager =
                        getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                    inputMethodManager?.hideSoftInputFromWindow(view.windowToken, 0)
                }

                clearFocus()

                return@setOnEditorActionListener true
            }
        }
    }

    private fun initFriendList() {
        adapter = FriendListAdapter()
        binding.friendRecyclerView.adapter = adapter

        viewModel.getMyFriendList()

        lifecycleScope.launch {
            viewModel.myFriendUserInfoList.collect {
                adapter.submitList(it)
            }
        }
    }
}