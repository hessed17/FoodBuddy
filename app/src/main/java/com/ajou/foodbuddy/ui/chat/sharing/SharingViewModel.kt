package com.ajou.foodbuddy.ui.chat.sharing

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ajou.foodbuddy.data.repository.ChatRepository
import com.ajou.foodbuddy.ui.chat.sharing.chatroom.ChatRoomListUiState
import com.ajou.foodbuddy.ui.chat.sharing.friend.FriendListUiState
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SharingViewModel @Inject constructor(
    private val chatRepository: ChatRepository
) : ViewModel() {

    private fun getUserId() = Firebase.auth.currentUser!!.email.toString()

    private val _queriedChatRoomList =
        MutableLiveData<ChatRoomListUiState>(ChatRoomListUiState.Uninitialized)
    val queriedChatRoomList: LiveData<ChatRoomListUiState> = _queriedChatRoomList

    fun getStaticChatRoomList() {
        viewModelScope.launch {
            _queriedChatRoomList.postValue(
                ChatRoomListUiState.SuccessGetChatRoomList(
                    chatRepository.getStaticChatRoomList(getUserId())
                )
            )
        }
    }

    private val _queriedFriendList =
        MutableLiveData<FriendListUiState>(FriendListUiState.Uninitialized)
    val queriedFriendList: LiveData<FriendListUiState> = _queriedFriendList

    fun getStaticUserList() {
        viewModelScope.launch {
            _queriedFriendList.postValue(
                FriendListUiState.SuccessGetUserList(
                    chatRepository.getStaticUserList(getUserId())
                )
            )
        }
    }

    fun share(
        chatRoomId: String,
        sharingType: String,
        sharingId: String,
        sharingTitle: String
    ) {
        viewModelScope.launch {
            chatRepository.sendMessage(
                chatRoomId = chatRoomId,
                userId = getUserId(),
                messageContent = sharingTitle,
                sharingId = sharingId,
                sharingType = sharingType
            )
        }
    }

}