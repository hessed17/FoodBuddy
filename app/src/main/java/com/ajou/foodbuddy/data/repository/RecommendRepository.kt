package com.ajou.foodbuddy.data.repository

interface RecommendRepository {

    suspend fun recommendRandomRestaurant()
}