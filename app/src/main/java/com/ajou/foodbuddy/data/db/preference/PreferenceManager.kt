package com.ajou.foodbuddy.data.db.preference

interface PreferenceManager {
    fun putUserId(address: String)

    fun getUserId(): String
}