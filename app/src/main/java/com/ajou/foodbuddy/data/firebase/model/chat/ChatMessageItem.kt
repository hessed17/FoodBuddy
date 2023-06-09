package com.ajou.foodbuddy.data.firebase.model.chat

import android.net.Uri
import android.util.Log
import com.ajou.foodbuddy.data.firebase.path.Key
import com.ajou.foodbuddy.extensions.convertBase64ToStr
import com.ajou.foodbuddy.extensions.convertTimeStampToDate

data class ChatMessageItem(
    val writerUserId: String? = null,
    val messageType: String = Key.SHARING_NORMAL,
    val messageContent: String? = null,
    val uploadTime: Any? = null,
    val sharingId: String? = null
) {
    fun toProcessedChatMessageItem(chatUserProfileInfoList: List<ChatUserProfileInfo>): ProcessedChatMessageItem {
        var profileImageLink: String? = null
        var nickname: String? = null
        chatUserProfileInfoList.forEach {
            if (it.userId.convertBase64ToStr() == this.writerUserId) {
                nickname = it.nickname
                profileImageLink = it.profileImageUrl
            }
        }

        return ProcessedChatMessageItem(
            userId = this.writerUserId!!,
            profileImageUrl = profileImageLink!!,
            nickname = nickname.toString(),
            messageType = this.messageType,
            messageContent = this.messageContent!!,
            uploadTime = this.uploadTime.toString().convertTimeStampToDate(),
            sharingId = this.sharingId
        )

    }
}

data class ChatUserProfileInfo(
    val profileImageUrl: String,
    val userId: String,
    val nickname: String
)

data class ProcessedChatMessageItem(
    val userId: String,
    val profileImageUrl: String,
    val nickname: String,
    val messageType: String = Key.SHARING_NORMAL,
    val messageContent: String,
    val uploadTime: String,
    val sharingId: String? = null
)