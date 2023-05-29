package com.ajou.foodbuddy.data.repository

import android.util.Log
import com.ajou.foodbuddy.data.firebase.model.UserInfo
import com.ajou.foodbuddy.data.firebase.path.Key.USER_FRIEND_INFO
import com.ajou.foodbuddy.data.firebase.path.Key.USER_INFO
import com.ajou.foodbuddy.extensions.convertBase64ToStr
import com.ajou.foodbuddy.extensions.convertStrToBase64
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(): UserRepository {

    private val database = Firebase.database.reference

    private val _myFriendUserInfoList = MutableStateFlow<List<UserInfo>>(emptyList())
    override val myFriendUserInfoList: Flow<List<UserInfo>>
        get() = _myFriendUserInfoList.asStateFlow()

    override suspend fun getMyFriendList(userId: String) {
        val myFriendUserIdList = mutableListOf<String>()
        database.child(USER_INFO).child(userId.convertStrToBase64()).child(USER_FRIEND_INFO).addValueEventListener(object: ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (snapshot in dataSnapshot.children) {
                    val friendUserId = snapshot.value.toString().convertBase64ToStr()
                    myFriendUserIdList.add(friendUserId)
                }

                myFriendUserIdList.map { getMyFriendUserInfo(it) }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun getMyFriendUserInfo(userId: String) {
        database.child(USER_INFO).child(userId.convertStrToBase64()).addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val friendUserId = snapshot.key.toString().convertBase64ToStr()
                val nickname = snapshot.child("nickname").value.toString()
                val userInfo = UserInfo(friendUserId, nickname)
                _myFriendUserInfoList.value = _myFriendUserInfoList.value + userInfo
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

}