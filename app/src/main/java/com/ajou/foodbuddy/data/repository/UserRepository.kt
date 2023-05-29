package com.ajou.foodbuddy.data.repository

import com.ajou.foodbuddy.data.firebase.model.UserInfo
import kotlinx.coroutines.flow.Flow

interface UserRepository {

    val myFriendUserInfoList: Flow<List<UserInfo>>

    suspend fun getMyFriendList(userId: String)
}