package com.ajou.foodbuddy.data.repository

import com.ajou.foodbuddy.data.firebase.model.profile.ChatUserInfo
import com.ajou.foodbuddy.data.firebase.model.chat.ProcessedChatRoomItem
import com.ajou.foodbuddy.data.firebase.model.chat.ProcessedChatMessageItem
import com.ajou.foodbuddy.data.firebase.path.Key
import com.ajou.foodbuddy.ui.chat.sharing.chatroom.InviteChatRoomItem
import kotlinx.coroutines.flow.Flow

interface ChatRepository {

    val chatRooms: Flow<List<ProcessedChatRoomItem>>

    suspend fun getChatRoomList(userId: String)

    val chatMessages: Flow<List<ProcessedChatMessageItem>>

    suspend fun getChatMessageList(chatRoomId: String)

    fun createNewChatRoom(myUserId: String, users: List<String>, chatRoomTitle: String): String

    suspend fun sendMessage(
        chatRoomId: String,
        userId: String,
        messageContent: String,
        sharingId: String? = null,
        sharingType: String = Key.SHARING_NORMAL
    )

    suspend fun getSharableChatRoomList(userId: String): List<InviteChatRoomItem>

    suspend fun getSharableUserList(userId: String): List<ChatUserInfo>

}