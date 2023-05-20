package com.ajou.foodbuddy.data.firebase.model

data class RestaurantInfo(
    val restaurantImageFolderPath: String,
    val categoryId: String,
    val reviewRating: Float,
    val reviewCount: Int,
    val latitude: Double,
    val longitude: Double
)
