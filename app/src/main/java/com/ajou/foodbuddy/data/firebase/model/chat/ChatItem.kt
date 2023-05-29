package com.ajou.foodbuddy.data.firebase.model.chat

import com.ajou.foodbuddy.extensions.convertBase64ToStr

data class ChatItem(
    val title: String? = null,
    val lastMessageWriter: String? = null,
    val lastMessageContent: String? = null,
    val lastUploadTime: Any? = null,
) {
    fun toProcessedChatItem(chatRoomId: String) =
        ProcessedChatItem(
            chatRoomId = chatRoomId,
            title = this.title!!,
            lastMessageWriterUserId = this.lastMessageWriter!!.convertBase64ToStr(),
            lastMessageContent = this.lastMessageContent!!,
            lastUploadTime = this.lastUploadTime!!.toString().toLong()
        )
}

data class ProcessedChatItem(
    val chatRoomId: String,
    val title: String,
    val lastMessageWriterUserId: String,
    val lastMessageContent: String,
    val lastUploadTime: Long
)