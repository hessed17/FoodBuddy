package com.ajou.foodbuddy.data.firebase.model

data class ReviewInfo(
    var userId:String?=null,
    var reviewTitle:String?=null,
    var reviewContent:String?=null,
    var categoryId:String?=null,
    var restaurantName:String?=null,
    var reviewRating:Float?=0f,
    var reviewLikeCount:Int?=0,
    var uploadTime:String?=null
) {
    fun toProcessedReviewInfo(reviewId: String) =
        ProcessedReviewInfo(
            reviewId = reviewId,
            userId = userId!!,
            reviewTitle = reviewTitle!!,
            reviewContent = reviewContent!!,
            categoryId = categoryId!!,
            restaurantName = restaurantName!!,
            reviewRating = reviewRating!!,
            reviewLikeCount = reviewLikeCount!!,
            uploadTime = uploadTime!!
        )
}

data class ProcessedReviewInfo(
    var reviewId: String,
    var userId: String,
    var reviewTitle: String,
    var reviewContent: String,
    var categoryId: String,
    var restaurantName: String,
    var reviewRating: Float,
    var reviewLikeCount: Int,
    var uploadTime: String
)