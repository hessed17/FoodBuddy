package com.ajou.foodbuddy.data.firebase.model

import android.net.Uri

//data class UserProfileInfo(
//    val nickname: String? = null,
//    val friendCount: Int = 0,
//    val friendList: String,
//    val favoriteRestaurant
//)
//
//data class ProcessedUserProfileInfo(
//    val profileImageFileName: String? = null,
//    val nickname: String,
//    val friendCount: Int,
//    val friendList: List<String>,
//)

data class LoginUserInfo(
    val nickname: String,
    val friendCount: Int = 0,
    val userFriendsInfo: Any? = null
)

data class UserInfo(
    val userId: String,
    val nickname: String
)