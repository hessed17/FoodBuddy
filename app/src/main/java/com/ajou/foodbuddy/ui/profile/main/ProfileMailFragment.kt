package com.ajou.foodbuddy.ui.profile.main

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.activityViewModels
import com.ajou.foodbuddy.BaseFragment
import com.ajou.foodbuddy.databinding.FragmentProfileMainBinding
import com.ajou.foodbuddy.ui.profile.ProfileViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileMailFragment: BaseFragment<FragmentProfileMainBinding>() {

    private val profileViewModel: ProfileViewModel by activityViewModels()

    private val storageRef = Firebase.storage.reference
    private val database = Firebase.database.reference
    private lateinit var Profileadapter: ProfileRestaurantAdapter

    override fun getViewBinding(): FragmentProfileMainBinding =
        FragmentProfileMainBinding.inflate(layoutInflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        val userId = profileViewModel.getUserId()

//        _binding?.profileNameTextView?.text = userId

        initProfile()
    }

    private fun initProfile(){
        val userInfoRef = FirebaseDatabase.getInstance().reference.child("UserInfo")

        userInfoRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val MyUser = dataSnapshot.child("UserId1") //해당 유저만 보여준다.

                _binding?.friendNameTextView?.text = MyUser.child("nickname").value.toString()
                _binding?.navigateFriendListButton?.text = MyUser.child("friendCount").value.toString()
            }
            override fun onCancelled(databaseError: DatabaseError) {
                // Handle the error if retrieval is canceled
                Log.d("Error", "Error retrieving data: ${databaseError.message}")
            }
        })
    }
}