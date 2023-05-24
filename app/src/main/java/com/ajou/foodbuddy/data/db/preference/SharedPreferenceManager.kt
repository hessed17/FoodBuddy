package com.ajou.foodbuddy.data.db.preference

import android.content.SharedPreferences
import com.ajou.foodbuddy.R
import javax.inject.Inject

class SharedPreferenceManager @Inject constructor(
    private val sharedPreferences: SharedPreferences
): PreferenceManager {

    override fun putUserId(address: String) {
        with(sharedPreferences.edit()) {
            putString(R.string.preference_user_id.toString(), address)
            apply()
        }
    }

    override fun getUserId(): String {

        return sharedPreferences.getString(
            R.string.preference_user_id.toString(),
            INVALID_STRING_VALUE
        ).toString()
    }

    companion object {
        const val INVALID_STRING_VALUE = "null"
    }
}