package com.ajou.foodbuddy.data.repository

import com.ajou.foodbuddy.data.firebase.model.profile.ChatUserInfo
import kotlinx.coroutines.flow.Flow

interface UserRepository {

    val myFriendUserInfoList: Flow<List<ChatUserInfo>>

    suspend fun getMyFriendList(userId: String)
}