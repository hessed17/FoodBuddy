package com.ajou.foodbuddy.ui.profile.search

import android.os.Bundle
import android.util.ArrayMap
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.ajou.foodbuddy.data.firebase.model.FindFriendInfo
import com.ajou.foodbuddy.data.firebase.model.UserInfo
import com.ajou.foodbuddy.databinding.FragmentSearchUserBinding
import com.ajou.foodbuddy.extensions.convertBase64ToStr
import com.ajou.foodbuddy.extensions.convertStrToBase64
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class SearchDeleteFriendListActivity : AppCompatActivity() {
    private lateinit var binding: FragmentSearchUserBinding
    private val storageRef = Firebase.storage.reference
    private val database = Firebase.database.reference
    private lateinit var adapter: FriendDeleteAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentSearchUserBinding.inflate(layoutInflater)
        binding.friendSearchEditText.hint = "친구 검색"
        setContentView(binding.root)

        val UserId: String = intent.getStringExtra("UserName").toString()

        //리사이클러뷰 초기화
        initFriendAdapter()
        //initFriendListall(UserId)
        //친구검색
        setupEditText(UserId)
    }

    override fun onRestart() {
        super.onRestart()
        binding = FragmentSearchUserBinding.inflate(layoutInflater)
        binding.friendSearchEditText.hint = "친구 검색"
        setContentView(binding.root)

        val UserId: String = intent.getStringExtra("UserName").toString()

        //리사이클러뷰 초기화
        initFriendAdapter()
        Thread.sleep(300)
        initFriendListall(UserId)
        //친구검색
        setupEditText(UserId)

    }

    private fun initFriendAdapter() {
        adapter = FriendDeleteAdapter()
        binding.frientSearchListRecyclerView.adapter = adapter
        binding.frientSearchListRecyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

    }

    private fun initFriendListall(UserId: String) {
        var cnt = 0
        val friendnInfoRef =
            FirebaseDatabase.getInstance().reference.child("UserInfo")
                .child(UserId.convertStrToBase64())
                .child("userFriendsInfo")
        friendnInfoRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var myFriendList = ArrayList<FindFriendInfo>()
                val totalFriendCount = dataSnapshot.childrenCount.toInt()
                for (friends in dataSnapshot.children) {
                    cnt++
                    val friendSelectedInfoRef =
                        FirebaseDatabase.getInstance().reference.child("UserInfo")
                    friendSelectedInfoRef.addListenerForSingleValueEvent(object :
                        ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val myFriend = snapshot.child(friends.value.toString())

                            myFriendList.add(
                                FindFriendInfo(
                                    myFriend.key.toString(),
                                    myFriend.child("nickname").value.toString(),
                                    myFriend.child("friendCount").getValue(Int::class.java)!!
                                        .toInt()
                                )
                            )
                            if (totalFriendCount == cnt) {
                                adapter.submitList(myFriendList)
                                adapter.notifyDataSetChanged()

                            }

                        }

                        override fun onCancelled(error: DatabaseError) {


                        }
                    })
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle the error if retrieval is canceled
                Log.d("Error", "Error retrieving data: ${databaseError.message}")
            }
        })
    }

    private fun setupEditText(myName: String) {
        var searchName: String? = null
        binding.friendSearchEditText.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE || event?.keyCode == KeyEvent.KEYCODE_ENTER) {
                searchName = binding.friendSearchEditText.text.toString()
                friendSearch(myName, searchName!!)
                true
            } else {
                false
            }
        }
    }

    private fun friendSearch(myName: String, searchName: String) {
        val UserInfos = FirebaseDatabase.getInstance().reference.child("UserInfo")
        val userFriendsRef =
            FirebaseDatabase.getInstance().reference.child("UserInfo")
                .child(myName.convertStrToBase64())
                .child("userFriendsInfo")
        var myFriendList = ArrayList<FindFriendInfo>()
        var deleteMap = ArrayMap<String, Int>()
        var rdeleteMap = ArrayMap<String, Int>()

        userFriendsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (child in snapshot.children) {
                    deleteMap[child.value.toString().convertBase64ToStr()] = 1 // 현재 해당 id값을 넣는다.
                }
                UserInfos.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (UserChild in snapshot.children) {
                            if (UserChild.child("nickname").toString().contains(searchName)) {
                                if (deleteMap[UserChild.key.toString().convertBase64ToStr()] == 1) {
                                    rdeleteMap[UserChild.key.toString().convertBase64ToStr()] = 1
                                }
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }

                })

                var totalFriendCount = rdeleteMap.size
                if (totalFriendCount == 0) {
                    adapter.clearData()
                    adapter.notifyDataSetChanged()
                    binding.notSearchNameText.visibility = View.VISIBLE
                } else {
                    binding.notSearchNameText.visibility = View.GONE
                    val friendSelectedInfoRef =
                        FirebaseDatabase.getInstance().reference.child("UserInfo")
                    friendSelectedInfoRef.addListenerForSingleValueEvent(object :
                        ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            for(myf in snapshot.children){
                                val myFriend = myf.key.toString().convertBase64ToStr()
                                if (rdeleteMap[myFriend] == 1) {
                                    myFriendList.add(
                                        FindFriendInfo(
                                            myFriend,
                                            myf.child("nickname").value.toString(),
                                            myf.child("friendCount")
                                                .getValue(Int::class.java)!!
                                                .toInt()
                                        )
                                    )

                                }

                            }
                            adapter.submitList(myFriendList)
                            adapter.notifyDataSetChanged()


                        }

                        override fun onCancelled(error: DatabaseError) {

                        }
                    })

                }

            }

            override fun onCancelled(error: DatabaseError) {

            }
        })


    }
}