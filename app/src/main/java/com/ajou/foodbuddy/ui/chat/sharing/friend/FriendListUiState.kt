package com.ajou.foodbuddy.ui.chat.sharing.friend

import com.ajou.foodbuddy.data.firebase.model.profile.ChatUserInfo

sealed class FriendListUiState {

    object Uninitialized: FriendListUiState()

    data class SuccessGetUserList(
        val userList: List<ChatUserInfo>
    ): FriendListUiState()

}
