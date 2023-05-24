package com.ajou.foodbuddy.ui.login

import androidx.lifecycle.ViewModel
import com.ajou.foodbuddy.data.db.preference.SharedPreferenceManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val preferenceManager: SharedPreferenceManager
): ViewModel() {

    fun putUserId(userId: String) {
        preferenceManager.putUserId(userId)
    }
}