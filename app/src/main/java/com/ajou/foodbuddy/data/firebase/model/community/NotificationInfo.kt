package com.ajou.foodbuddy.data.firebase.model.notification

data class NotificationInfo (
    var reviewId:String?=null,
    var nickname:String?=null,
    var profileImage:String?=null,
    val reviewTitle:String?=null,
    var message:String?=null,
    var uploadTime:String?=null
)