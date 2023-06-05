package com.ajou.foodbuddy.data.repository

import android.util.Log
import com.ajou.foodbuddy.data.firebase.model.profile.ChatUserInfo
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

    private val _myFriendChatUserInfoList = MutableStateFlow<List<ChatUserInfo>>(emptyList())
    override val myFriendUserInfoList: Flow<List<ChatUserInfo>>
        get() = _myFriendChatUserInfoList.asStateFlow()

    override suspend fun getMyFriendList(userId: String) {
        val myFriendUserIdList = mutableListOf<String>()
        database.child(USER_INFO).child(userId.convertStrToBase64()).child(USER_FRIEND_INFO).addValueEventListener(object: ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (snapshot in dataSnapshot.children) {
                    val friendUserId = snapshot.value.toString().convertBase64ToStr()
                    Log.d("friendUserId", friendUserId)
                    myFriendUserIdList.add(friendUserId)
                }

                myFriendUserIdList.map { getMyFriendChatUserInfo(it) }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun getMyFriendChatUserInfo(userId: String) {
        database.child(USER_INFO).child(userId.convertStrToBase64()).addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val friendUserId = snapshot.key.toString().convertBase64ToStr()
                val nickname = snapshot.child("nickname").value.toString()
                val chatUserInfo = ChatUserInfo(friendUserId, nickname)
                Log.d("nickname", nickname)
                _myFriendChatUserInfoList.value = _myFriendChatUserInfoList.value + chatUserInfo
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

}