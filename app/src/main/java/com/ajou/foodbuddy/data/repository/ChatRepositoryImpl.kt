package com.ajou.foodbuddy.data.repository

import android.util.Log
import com.ajou.foodbuddy.data.firebase.model.chat.*
import com.ajou.foodbuddy.data.firebase.model.profile.ChatUserInfo
import com.ajou.foodbuddy.data.firebase.path.Key
import com.ajou.foodbuddy.data.firebase.path.Key.CHATROOM_DETAIL_INFO
import com.ajou.foodbuddy.data.firebase.path.Key.CHATROOM_INFO
import com.ajou.foodbuddy.data.firebase.path.Key.CHATROOM_LIST
import com.ajou.foodbuddy.data.firebase.path.Key.CHATROOM_MEMBER
import com.ajou.foodbuddy.data.firebase.path.Key.CHATROOM_MESSAGE_INFO
import com.ajou.foodbuddy.data.firebase.path.Key.CHAT_INFO
import com.ajou.foodbuddy.data.firebase.path.Key.FCM_SERVER_KEY
import com.ajou.foodbuddy.data.firebase.path.Key.FCM_TOKEN
import com.ajou.foodbuddy.data.firebase.path.Key.NICKNAME
import com.ajou.foodbuddy.data.firebase.path.Key.USER_FRIEND_INFO
import com.ajou.foodbuddy.data.firebase.path.Key.USER_INFO
import com.ajou.foodbuddy.extensions.convertBase64ToStr
import com.ajou.foodbuddy.extensions.convertStrToBase64
import com.ajou.foodbuddy.ui.chat.sharing.chatroom.InviteChatRoomItem
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import javax.inject.Inject

class ChatRepositoryImpl @Inject constructor() : ChatRepository {

    private val database = Firebase.database.reference

    private val _chatRooms = MutableStateFlow<List<ProcessedChatRoomItem>>(emptyList())
    override val chatRooms: Flow<List<ProcessedChatRoomItem>>
        get() = _chatRooms.asStateFlow()

    override suspend fun getChatRoomList(userId: String) {

        database.child(CHAT_INFO).child(userId.convertStrToBase64()).child(CHATROOM_LIST)
            .addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val chatRoomId = snapshot.getValue(String::class.java)

                    if (chatRoomId != null) {
                        database.child(CHATROOM_INFO).child(chatRoomId)
                            .addValueEventListener(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    val item = snapshot.getValue(ChatItem::class.java)

                                    if (item?.lastMessageContent != null) {
                                        val processedItem = item.toProcessedChatItem(chatRoomId)
                                        var index = -1
                                        val currentList = _chatRooms.value.toMutableList()

                                        currentList.map {
                                            if (it.chatRoomId == processedItem.chatRoomId) {
                                                index = currentList.indexOf(it)
                                            }
                                        }

                                        if (index == -1) {
                                            _chatRooms.value =
                                                _chatRooms.value.toMutableList() + processedItem
                                        } else {
                                            currentList[index] = processedItem
                                            _chatRooms.value = currentList
                                        }
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {}
                            })
                    }
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onChildRemoved(snapshot: DataSnapshot) {}
                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onCancelled(error: DatabaseError) {}

            })
    }

    private val _chatMessages = MutableStateFlow<List<ProcessedChatMessageItem>>(emptyList())
    override val chatMessages: Flow<List<ProcessedChatMessageItem>>
        get() = _chatMessages.asStateFlow()

    override suspend fun getChatMessageList(chatRoomId: String) {
        val chatUserProfileInfoList = getChatMemberList(chatRoomId)

        database.child(CHATROOM_DETAIL_INFO).child(chatRoomId).child(CHATROOM_MESSAGE_INFO)
            .addChildEventListener(
                object : ChildEventListener {
                    override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                        val message = snapshot.getValue(ChatMessageItem::class.java)

                        if (message != null) {
                            val processMessage =
                                message.copy(writerUserId = message.writerUserId!!.convertBase64ToStr())
                            val processedChatMessageItem =
                                processMessage.toProcessedChatMessageItem(chatUserProfileInfoList)
                            _chatMessages.value =
                                _chatMessages.value.toMutableList() + processedChatMessageItem
                        }
                    }

                    override fun onChildChanged(
                        snapshot: DataSnapshot,
                        previousChildName: String?
                    ) {
                    }

                    override fun onChildRemoved(snapshot: DataSnapshot) {}
                    override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
                    override fun onCancelled(error: DatabaseError) {}
                })
    }

    private suspend fun getChatMemberList(chatRoomId: String): List<ChatUserProfileInfo> {
        val list =
            database.child(CHATROOM_DETAIL_INFO).child(chatRoomId).child(CHATROOM_MEMBER).get()
                .await().value as? List<*>
        val userIdEncodedForBase64List = list?.map { it.toString() }
        val chatUserProfileInfoList = mutableListOf<ChatUserProfileInfo>()
        userIdEncodedForBase64List?.forEach { userIdEncodedForBase64 ->
            val userInfo = database.child(USER_INFO).child(userIdEncodedForBase64).get().await()
            val nickname = userInfo.child("nickname").value.toString()
            val profileImage = userInfo.child("profileImage").value.toString()
            chatUserProfileInfoList.add(
                ChatUserProfileInfo(
                    profileImageUrl = profileImage,
                    nickname = nickname,
                    userId = userInfo.key.toString()
                )
            )
        }

        return chatUserProfileInfoList
    }

    override fun createNewChatRoom(
        myUserId: String,
        users: List<String>,
        chatRoomTitle: String
    ): String {
        val chatRoomId = database.child(CHATROOM_DETAIL_INFO).push().key
        users.forEach { userid ->
            database.child(CHAT_INFO).child(userid.convertStrToBase64()).child(CHATROOM_LIST)
                .push().setValue(chatRoomId)
        }
        database.child(USER_INFO).child(myUserId.convertStrToBase64()).child("nickname")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val nickname = snapshot.value.toString()
                    Log.d("nickname", nickname)
                    val title = mapOf("title" to chatRoomTitle)
                    database.child(CHATROOM_INFO).child(chatRoomId.toString()).updateChildren(title)
                }

                override fun onCancelled(error: DatabaseError) {}

            })
        database.child(CHATROOM_DETAIL_INFO).child(chatRoomId.toString()).child(CHATROOM_MEMBER)
            .setValue(users.map { it.convertStrToBase64() })

        return chatRoomId.toString()
    }

    override suspend fun sendMessage(
        chatRoomId: String,
        userId: String,
        messageContent: String,
        sharingId: String?,
        sharingType: String
    ) {
        var content = messageContent
        if (sharingType == Key.SHARING_RESTAURANT) {
            content = "식당을 공유했습니다."
        } else if (sharingType == Key.SHARING_REVIEW) {
            content = "리뷰를 공유했습니다."
        }
        withContext(Dispatchers.IO) {
            updateLastMessage(chatRoomId, userId, content)
        }
        withContext(Dispatchers.IO) {
            addMessage(
                chatRoomId = chatRoomId,
                userId = userId,
                messageContent = messageContent,
                sharingType = sharingType,
                sharingId = sharingId
            )
        }
        withContext(Dispatchers.IO) {
            sendNotificationOtherUser(chatRoomId, userId, content)
        }
    }

    private fun updateLastMessage(chatRoomId: String, userId: String, messageContent: String) {
        val lastMessageUpdate = hashMapOf(
            "lastMessageWriter" to userId.convertStrToBase64(),
            "lastMessageContent" to messageContent,
            "lastUploadTime" to ServerValue.TIMESTAMP,
        )
        database.child(CHATROOM_INFO).child(chatRoomId).updateChildren(lastMessageUpdate)
    }

    private fun addMessage(
        chatRoomId: String,
        userId: String,
        messageContent: String,
        sharingType: String,
        sharingId: String?
    ) {
        val message = ChatMessageItem(
            writerUserId = userId.convertStrToBase64(),
            messageType = sharingType,
            messageContent = messageContent,
            uploadTime = ServerValue.TIMESTAMP,
            sharingId = sharingId
        )
        database.child(CHATROOM_DETAIL_INFO).child(chatRoomId).child(CHATROOM_MESSAGE_INFO).push()
            .setValue(message)
    }

    private suspend fun sendNotificationOtherUser(
        chatRoomId: String,
        userId: String,
        messageContent: String
    ) {
        val otherUserFcmTokenList = getOtherUserList(chatRoomId)
        otherUserFcmTokenList.forEach { token ->
            useOkHttp(token, userId, messageContent)
        }
    }

    private suspend fun getOtherUserList(chatRoomId: String): List<String> {
        return database.child(CHATROOM_DETAIL_INFO).child(chatRoomId).child(CHATROOM_MEMBER).get()
            .await()
            .children.toList().map {
                val userId = it.value.toString()
                getUserFcmToken(userId)
            }
    }

    private suspend fun getUserFcmToken(userId: String): String {
        return database.child(CHAT_INFO).child(userId.convertStrToBase64()).child(FCM_TOKEN).get()
            .await().value.toString()
    }

    private fun useOkHttp(fcmToken: String, userId: String, messageContent: String) {
        val client = OkHttpClient()

        val root = JSONObject()
        val notification = JSONObject()
        notification.put("title", userId.convertStrToBase64())
        notification.put("body", messageContent)

        root.put("to", fcmToken)
        root.put("priority", "high")
        root.put("notification", notification)

        val requestBody =
            root.toString()
                .toRequestBody("application/json; charset=utf-8".toMediaType())
        val request =
            Request.Builder().post(requestBody)
                .url("https://fcm.googleapis.com/fcm/send")
                .header("Authorization", "key=$FCM_SERVER_KEY").build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.stackTraceToString()
            }

            override fun onResponse(call: Call, response: Response) {
                // ignore onResponse
            }

        })
    }

    override suspend fun getSharableChatRoomList(userId: String): List<InviteChatRoomItem> {
        val dataSnapshot =
            database.child(CHAT_INFO).child(userId.convertStrToBase64()).child(CHATROOM_LIST)
                .get().await()
        val chatRoomIdList = mutableListOf<String>()
        for (snapshot in dataSnapshot.children) {
            val chatRoomId = snapshot.getValue(String::class.java)

            if (!chatRoomId.isNullOrBlank()) {
                chatRoomIdList.add(chatRoomId)
            }
        }

        val chatRoomList = mutableListOf<InviteChatRoomItem>()

        for (chatRoomId in chatRoomIdList) {
            val dataSnapshot2 = database.child(CHATROOM_INFO).child(chatRoomId).get().await()
            val item = dataSnapshot2.getValue(ChatItem::class.java)

            if (item?.lastMessageContent != null) {
                chatRoomList.add(item.toInviteChatRoomModel(dataSnapshot2.key.toString()))
            }
        }

        return chatRoomList
    }

    override suspend fun getSharableUserList (userId: String): List<ChatUserInfo> {
        val dataSnapshot = database.child(USER_INFO).child(userId.convertStrToBase64())
            .child(USER_FRIEND_INFO).get().await()

        val userIdList = mutableListOf<String>()
        for (snapshot in dataSnapshot.children) {
            val userid = snapshot.getValue(String::class.java)

            if (userid != null) {
                userIdList.add(userid.toString().convertBase64ToStr())
            }
        }

        val chatUserInfoList = mutableListOf<ChatUserInfo>()
        for (userid in userIdList) {
            val dataSnapshot2 =
                database.child(USER_INFO).child(userid.convertStrToBase64()).get().await()
            val decodedUserId = dataSnapshot2.key.toString().convertBase64ToStr()
            val nickname = dataSnapshot2.child(NICKNAME).getValue(String::class.java)

            if (nickname != null) {
                chatUserInfoList.add(ChatUserInfo(userId = decodedUserId, nickname = nickname))
            }
        }

        return chatUserInfoList
    }
}