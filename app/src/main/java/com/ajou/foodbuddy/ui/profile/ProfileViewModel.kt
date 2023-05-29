package com.ajou.foodbuddy.ui.profile

import androidx.lifecycle.ViewModel
import com.ajou.foodbuddy.data.db.preference.SharedPreferenceManager
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
//    private val preferenceManager: SharedPreferenceManager
): ViewModel() {

    fun getUserId() =  Firebase.auth.currentUser!!.email.toString()

}