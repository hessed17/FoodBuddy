package com.ajou.foodbuddy.data.firebase.model.restaurant

import android.net.Uri

data class RestaurantItem(
    val categoryId: String? = null,
    val latitude: Float = 0f,
    val longitude: Float = 0f,
) {
    fun convertToFirstProcessedRestaurantItem(restaurantName: String) = FirstProcessedRestaurantItem(
        restaurantName = restaurantName,
        categoryId = this.categoryId!!
    )
}

data class FirstProcessedRestaurantItem(
    val restaurantName: String,
    val categoryId: String,
) {
    fun convertToSecondProcessedRestaurantItem(thumbnailImage: Uri?,ratingNumber: Float?,reviewNumber: Int?) =
        SecondProcessedRestaurantItem(
            restaurantName = this.restaurantName,
            categoryId = this.categoryId,
            thumbnailImage = thumbnailImage,
            ratingNumber = ratingNumber,
            reviewNumber = reviewNumber
        )
}

data class SecondProcessedRestaurantItem(
    val restaurantName: String,
    val categoryId: String,
    val thumbnailImage: Uri?,
    val ratingNumber: Float?=0f,
    val reviewNumber:Int?=0
)

data class RestaurantDetailItem(
    val restaurantName: String? = null,
    val categoryId: String? = null,
    val reviewRating: Float? = 0f,
    val reviewCount: Int? = 0,
    val latitude: Double = 0.0,
    val longitude: Double? = 0.0
)

data class MyRestaurant(
    val restaurantName: String?,
    val thumbnailImage: Uri?,
)