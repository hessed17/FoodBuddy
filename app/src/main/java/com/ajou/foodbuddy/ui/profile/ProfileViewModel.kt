package com.ajou.foodbuddy.ui.profile

import androidx.lifecycle.ViewModel
import com.ajou.foodbuddy.data.db.preference.SharedPreferenceManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val preferenceManager: SharedPreferenceManager
): ViewModel() {

    fun getUserId() =  preferenceManager.getUserId()

}