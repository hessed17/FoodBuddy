package com.ajou.foodbuddy.ui.chat.invite

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ajou.foodbuddy.data.firebase.model.UserInfo
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

    fun createNewChatRoom(users: List<UserInfo>): String {
        val chatRoomIdList = users.map { it.userId } + getMyUserId()
        return chatRepository.createNewChatRoom(getMyUserId(), chatRoomIdList,
            users.map { it.nickname }.joinToString(", "))
    }
}