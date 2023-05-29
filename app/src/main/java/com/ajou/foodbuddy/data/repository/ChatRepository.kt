package com.ajou.foodbuddy.data.repository

import com.ajou.foodbuddy.data.firebase.model.ChatItem
import com.ajou.foodbuddy.data.firebase.model.ChatMessageItem
import com.ajou.foodbuddy.data.firebase.model.ProcessedChatItem
import kotlinx.coroutines.flow.Flow

interface ChatRepository {

    val chatRooms: Flow<List<ProcessedChatItem>>

    suspend fun getChatRoomList(userId: String)

    val chatMessages: Flow<List<ChatMessageItem>>

    suspend fun getChatMessageList(chatRoomId: String)

    fun createNewChatRoom(myUserId: String, users: List<String>, chatRoomTitle: String): String

    suspend fun sendMessage(chatRoomId: String, userId: String, messageContent: String)

}