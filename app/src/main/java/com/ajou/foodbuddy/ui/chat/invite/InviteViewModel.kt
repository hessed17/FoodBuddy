package com.ajou.foodbuddy.ui.chat.invite

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ajou.foodbuddy.data.firebase.model.profile.ChatUserInfo
import com.ajou.foodbuddy.data.repository.ChatRepository
import com.ajou.foodbuddy.data.repository.UserRepository
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InviteViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val chatRepository: ChatRepository
//    private val preferenceManager: PreferenceManager
) : ViewModel() {

    private fun getMyUserId() = Firebase.auth.currentUser!!.email.toString()

    val myFriendUserInfoList = userRepository.myFriendUserInfoList

    fun getMyFriendList() {
        viewModelScope.launch {
            userRepository.getMyFriendList(getMyUserId())
        }
    }

    fun createNewChatRoom(title: String, users: List<ChatUserInfo>): String {
        val chatRoomIdList = users.map { it.userId } + getMyUserId()
        Log.d("dlskvjasdivejv", chatRoomIdList.toString())
        return chatRepository.createNewChatRoom(getMyUserId(), chatRoomIdList, title)
    }
}