package com.ajou.foodbuddy.data.firebase.model.community

import android.net.Uri

data class CommentInfo(
    val profileImage: String?=null,
    val userId:String?=null,
    val userComment:String?=null,
    val uploadTime:String?=null
)
