package com.ajou.foodbuddy.data.firebase.model.profile

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

data class ChatUserInfo(
    val userId: String,
    val nickname: String
)

data class UserInfo(
    val nickname: String,
    val friendCount: Int = 0
)
data class FindFriendInfo(
    val UserId: String,
    val profileImage:String?=null,
    val nickname: String,
    val friendCount: Int = 0

)