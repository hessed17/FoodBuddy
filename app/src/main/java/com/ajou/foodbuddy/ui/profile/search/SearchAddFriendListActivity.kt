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
import com.ajou.foodbuddy.data.firebase.path.Key
import com.ajou.foodbuddy.databinding.FragmentSearchUserBinding
import com.ajou.foodbuddy.extensions.convertStrToBase64
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage

class SearchAddFriendListActivity : AppCompatActivity() {
    private lateinit var binding: FragmentSearchUserBinding
    private val storageRef = Firebase.storage.reference
    private val database = Firebase.database.reference
    private lateinit var adapter: FriendAddAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentSearchUserBinding.inflate(layoutInflater)
        binding.friendSearchEditText.hint = "유저 검색"
        setContentView(binding.root)
        val myUserId: String = intent.getStringExtra("UserName").toString()

        //리사이클러뷰 초기화
        initFriendAdapter()

        //등록된 인원 모두 조회
        //initListAll(UserId)
        //유저 검색
        //친구검색인데
        setupEditText(myUserId)
        initQueryButton()
    }

    private fun initQueryButton() {
        searchNickname(Firebase.auth.currentUser!!.email.toString())
    }

    override fun onRestart() {
        super.onRestart()
        binding = FragmentSearchUserBinding.inflate(layoutInflater)
        binding.friendSearchEditText.hint = "유저 검색"
        setContentView(binding.root)
        val UserId: String = intent.getStringExtra("UserName").toString()

        //리사이클러뷰 초기화
        initFriendAdapter()

        //등록된 인원 조회
        initListAll(UserId)

        setupEditText(UserId)

    }


    private fun initListAll(myId: String) {
        //모든 친구 리스트 출력
        var alljoinList = ArrayList<FindFriendInfo>()
        // 이미 친추된 인원 확인
        var addedFriendList = ArrayMap<String, Int>()
        var addedFriendAll = FirebaseDatabase.getInstance().reference.child("UserInfo").child(myId.convertStrToBase64())
            .child("userFriendsInfo")
0
        addedFriendAll.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                //자기자신과 친구인 경우 1로 표시
                for (exist in snapshot.children) {
                    addedFriendList[exist.value.toString()] = 1
                }
                val a = FirebaseDatabase.getInstance().reference.child("UserInfo")
                a.addListenerForSingleValueEvent(object:ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (exist in snapshot.children) {
                            if(addedFriendList[exist.key.toString()]==1)
                                addedFriendList.remove(exist.key.toString())
                            else{
                                addedFriendList[exist.key.toString()]=1
                            }
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {

                    }

                })
                //자기자신과 친구가 아닌경우 화면에 보여주기
                var friendAll = FirebaseDatabase.getInstance().reference.child("UserInfo")
                friendAll.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (addOkUser in snapshot.children) {
                            val myFriend = snapshot.child(addOkUser.key.toString())
                            //자기자신 및 이미 친구추가된 친구 제외
                            if (addOkUser.key.toString() == myId.convertStrToBase64()) {

                            } else {
                                if (addedFriendList[addOkUser.key.toString()] == 1) {
                                    alljoinList.add(
                                        FindFriendInfo(
                                            myFriend.key.toString(),
                                            myFriend.child("nickname").value.toString(),
                                            myFriend.child("friendCount")
                                                .getValue(Int::class.java)!!
                                                .toInt()
                                        )
                                    )
                                }

                            }
                        }
                        adapter.submitList(alljoinList)
                        adapter.notifyDataSetChanged()
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }

                })


            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun initFriendAdapter() {
        adapter = FriendAddAdapter()
        binding.frientSearchListRecyclerView.adapter = adapter
        binding.frientSearchListRecyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

    }

    private fun setupEditText(myUserId: String) {
        binding.friendSearchEditText.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE || event?.keyCode == KeyEvent.KEYCODE_ENTER) {
                searchNickname(myUserId)
                true
            } else {
                false
            }
        }
    }

    private fun searchNickname(myUserId: String) {
        val searchName = binding.friendSearchEditText.text.toString()
        friendSearch(myUserId, searchName)
    }

    private fun friendSearch(myUserId: String, searchName: String) {
        val searchAdmin = FirebaseDatabase.getInstance().reference.child("UserInfo")
        var findName: String = "null"

        var UserInfoList = FirebaseDatabase.getInstance().reference.child("UserInfo")
        var myFriendList = ArrayList<FindFriendInfo>()
        var addedUserList = ArrayMap<String, Int>()
        var addedFriendAll =
            FirebaseDatabase.getInstance().reference.child("UserInfo").child(myUserId.convertStrToBase64())
                .child("userFriendsInfo")

        searchAdmin.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (UserInfos in snapshot.children) {
                    if (UserInfos.child("nickname").value.toString().contains(searchName)) {
                        addedUserList[UserInfos.key.toString()] = 1
                    }
                }
                addedFriendAll.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        //자기자신과 친구인 경우 1로 표시
                        for (exist in snapshot.children) {
                            addedUserList.remove(exist.value.toString())
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }
                })
                //자기자신과 친구목록이 없는 경우 출력하기
                UserInfoList.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        var totalUserCount = addedUserList.size
                        for (usersCount in snapshot.children) {
                            if (usersCount.key.toString().contains(findName) && addedUserList[usersCount.key.toString()] != 1)
                                totalUserCount++
                        }
                        addedFriendAll.addListenerForSingleValueEvent(object :
                            ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (totalUserCount == 0) {
                                    adapter.clearData()
                                    adapter.notifyDataSetChanged()
                                    binding.notSearchNameText.visibility = View.VISIBLE

                                } else {
                                    binding.notSearchNameText.visibility = View.GONE
                                    val friendSelectedInfoRef =
                                        FirebaseDatabase.getInstance().reference.child("UserInfo")
                                    friendSelectedInfoRef.addListenerForSingleValueEvent(
                                        object :
                                            ValueEventListener {
                                            override fun onDataChange(snapshot: DataSnapshot) {
                                                for (addOkUser in snapshot.children) {
                                                    val myFriend =
                                                        snapshot.child(addOkUser.key.toString())
                                                    //자기자신 및 이미 친구추가된 친구 제외
                                                    if (addOkUser.key.toString() == myUserId.convertStrToBase64()) {
                                                        Log.d("testest",addOkUser.key.toString())

                                                    } else {
                                                        if (addedUserList[addOkUser.key.toString()] == 1) {
                                                            myFriendList.add(
                                                                FindFriendInfo(
                                                                    myFriend.key.toString(),
                                                                    myFriend.child("nickname").value.toString(),
                                                                    myFriend.child("friendCount")
                                                                        .getValue(Int::class.java)!!
                                                                        .toInt()
                                                                )
                                                            )
                                                        }
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

                    override fun onCancelled(error: DatabaseError) {

                    }

                })




            }

            override fun onCancelled(error: DatabaseError) {
            }

        })

    }

}