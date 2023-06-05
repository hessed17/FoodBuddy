package com.ajou.foodbuddy.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ajou.foodbuddy.data.db.preference.PreferenceManager
import com.ajou.foodbuddy.data.repository.ChatRepository
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
//    private val preferenceManager: PreferenceManager
): ViewModel() {

    val chatRoomList = chatRepository.chatRooms
    val chatMessageList = chatRepository.chatMessages

    private fun getUserId() = Firebase.auth.currentUser!!.email.toString()

    fun getChatRoomList() {
        viewModelScope.launch {
            chatRepository.getChatRoomList(getUserId())
        }
    }

    fun getChatMessageList(chatRoomId: String) {
        viewModelScope.launch {
            chatRepository.getChatMessageList(chatRoomId)
        }
    }

    fun sendMessage(chatRoomId: String, messageContent: String) {
        viewModelScope.launch {
            chatRepository.sendMessage(chatRoomId, getUserId(), messageContent)
        }
    }
}