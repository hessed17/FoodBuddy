package com.ajou.foodbuddy.data.firebase.model

import android.net.Uri
import com.ajou.foodbuddy.data.firebase.path.Key

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
    fun convertToSecondProcessedRestaurantItem(thumbnailImage: Uri?) =
        SecondProcessedRestaurantItem(
            restaurantName = this.restaurantName,
            categoryId = this.categoryId,
            thumbnailImage = thumbnailImage
        )
}

data class SecondProcessedRestaurantItem(
    val restaurantName: String,
    val categoryId: String,
    val thumbnailImage: Uri?
)

data class RestaurantDetailItem(
    val restaurantName: String? = null,
    val categoryId: String? = null,
    val reviewRating: Float? = 0f,
    val reviewCount: Int? = 0,
    val latitude: Double = 0.0,
    val longitude: Double? = 0.0
)
