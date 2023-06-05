package com.ajou.foodbuddy.data.firebase.model.chat

import android.net.Uri
import com.ajou.foodbuddy.data.firebase.path.Key

data class ChatMessageItem(
    val writerUserId: String? = null,
    val messageType: String = Key.SHARING_NORMAL,
    val messageContent: String? = null,
    val uploadTime: Any? = null,
    val sharingId: String? = null
)

data class ChatUserInfo(
    val profileImage: Uri,
    val userId: String,
    val nickname: String
)