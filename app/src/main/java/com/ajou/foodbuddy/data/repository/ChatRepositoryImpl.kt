package com.ajou.foodbuddy.data.repository

import android.util.Log
import com.ajou.foodbuddy.data.firebase.model.ChatItem
import com.ajou.foodbuddy.data.firebase.model.ChatMessageItem
import com.ajou.foodbuddy.data.firebase.model.ProcessedChatItem
import com.ajou.foodbuddy.data.firebase.path.Key.CHATROOM_DETAIL_INFO
import com.ajou.foodbuddy.data.firebase.path.Key.CHATROOM_INFO
import com.ajou.foodbuddy.data.firebase.path.Key.CHATROOM_LIST
import com.ajou.foodbuddy.data.firebase.path.Key.CHATROOM_MEMBER
import com.ajou.foodbuddy.data.firebase.path.Key.CHATROOM_MESSAGE_INFO
import com.ajou.foodbuddy.data.firebase.path.Key.CHAT_INFO
import com.ajou.foodbuddy.data.firebase.path.Key.FCM_SERVER_KEY
import com.ajou.foodbuddy.data.firebase.path.Key.FCM_TOKEN
import com.ajou.foodbuddy.data.firebase.path.Key.USER_INFO
import com.ajou.foodbuddy.extensions.convertBase64ToStr
import com.ajou.foodbuddy.extensions.convertStrToBase64
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
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

    private val _chatRooms = MutableStateFlow<List<ProcessedChatItem>>(emptyList())
    override val chatRooms: Flow<List<ProcessedChatItem>>
        get() = _chatRooms.asStateFlow()

    override suspend fun getChatRoomList(userId: String) {
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

        chatRoomIdList.map { chatRoomId ->
            getChatRoomInfo(chatRoomId)
        }

//            _chatRooms.value =
//                chatList.sortedByDescending { it.lastUploadTime.toString().toLong() }
    }

    private fun getChatRoomInfo(chatRoomId: String) {
        database.child(CHATROOM_INFO).child(chatRoomId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val chatItem = snapshot.getValue(ChatItem::class.java)

                    if (chatItem?.lastMessageWriter != null) {
                        chatItem.toProcessedChatItem(chatRoomId)
                        _chatRooms.value = _chatRooms.value.toMutableList() + chatItem.toProcessedChatItem(chatRoomId)
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private val _chatMessages = MutableStateFlow<List<ChatMessageItem>>(emptyList())
    override val chatMessages: Flow<List<ChatMessageItem>>
        get() = _chatMessages.asStateFlow()

    private lateinit var chatRoomMessageDatabaseRef: DatabaseReference

    override suspend fun getChatMessageList(chatRoomId: String) {
        chatRoomMessageDatabaseRef =
            database.child(CHATROOM_DETAIL_INFO).child(chatRoomId).child(CHATROOM_MESSAGE_INFO)
        chatRoomMessageDatabaseRef
            .addChildEventListener(messageChildEventListener)
    }

    private val messageChildEventListener = object : ChildEventListener {
        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
            val message = snapshot.getValue(ChatMessageItem::class.java)

            if (message != null) {
                val processMessage =
                    message.copy(writerUserId = message.writerUserId!!.convertBase64ToStr())
                _chatMessages.value = _chatMessages.value.toMutableList() + processMessage
            }
        }

        override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
        override fun onChildRemoved(snapshot: DataSnapshot) {}
        override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
        override fun onCancelled(error: DatabaseError) {}

    }

    override fun createNewChatRoom(myUserId: String, users: List<String>, chatRoomMemberStr: String): String {
        val chatRoomId = database.child(CHATROOM_DETAIL_INFO).push().key
        users.forEach { userid ->
            database.child(CHAT_INFO).child(userid.convertStrToBase64()).child(CHATROOM_LIST)
                .push().setValue(chatRoomId)
        }
        database.child(USER_INFO).child(myUserId.convertStrToBase64()).child("nickname").addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val nickname = snapshot.value.toString()
                Log.d("nickname", nickname)
                val title = mapOf("title" to "$chatRoomMemberStr, $nickname")
                database.child(CHATROOM_INFO).child(chatRoomId.toString()).updateChildren(title)
            }

            override fun onCancelled(error: DatabaseError) {}

        })
        database.child(CHATROOM_DETAIL_INFO).child(chatRoomId.toString()).child(CHATROOM_MEMBER)
            .setValue(users)

        return chatRoomId.toString()
    }

    override suspend fun sendMessage(chatRoomId: String, userId: String, messageContent: String) {
        withContext(Dispatchers.IO) {
            updateLastMessage(chatRoomId, userId, messageContent)
        }
        withContext(Dispatchers.IO) {
            addMessage(chatRoomId, userId, messageContent)
        }
        withContext(Dispatchers.IO) {
            sendNotificationOtherUser(chatRoomId, userId, messageContent)
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

    private fun addMessage(chatRoomId: String, userId: String, messageContent: String) {
        val message = ChatMessageItem(
            writerUserId = userId.convertStrToBase64(),
            messageContent = messageContent,
            uploadTime = ServerValue.TIMESTAMP
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
}