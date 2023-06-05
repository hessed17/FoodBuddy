package com.ajou.foodbuddy.ui.chat.sharing.chatroom

sealed class ChatRoomListUiState {

    object Uninitialized: ChatRoomListUiState()

    data class SuccessGetChatRoomList(
        val chatRoomList: List<InviteChatRoomItem>
    ): ChatRoomListUiState()
}
