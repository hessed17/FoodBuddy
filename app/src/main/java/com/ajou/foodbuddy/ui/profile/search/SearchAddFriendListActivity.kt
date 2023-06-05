package com.ajou.foodbuddy.ui.profile.search

import android.os.Bundle
import android.util.ArrayMap
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.ajou.foodbuddy.data.firebase.model.profile.FindFriendInfo
import com.ajou.foodbuddy.databinding.FragmentSearchUserBinding
import com.ajou.foodbuddy.extensions.convertBase64ToStr
import com.ajou.foodbuddy.extensions.convertStrToBase64
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase

class SearchAddFriendListActivity : AppCompatActivity() {
    private lateinit var binding: FragmentSearchUserBinding
    private lateinit var adapter: FriendAddAdapter
    private lateinit var listAdapter: ArrayAdapter<String>
    private val dataList = ArrayList<String>()
    private var filteredData = ArrayList<String>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentSearchUserBinding.inflate(layoutInflater)
        binding.friendSearchEditText.queryHint = "유저 검색"
        setContentView(binding.root)
        val myUserId: String = intent.getStringExtra("UserName").toString()

        //리사이클러뷰 초기화
        initFriendAdapter()
        //등록된 인원 모두 조회
        //initListAll(UserId)
        //유저 검색
        setupEditText(myUserId)

        setupFriendList()

        //initQueryButton() -> 필요없다.
    }

    override fun onRestart() {
        super.onRestart()
        binding = FragmentSearchUserBinding.inflate(layoutInflater)
        binding.friendSearchEditText.queryHint = "유저 검색"
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
        var addedFriendAll = FirebaseDatabase.getInstance().reference.child("UserInfo")
            .child(myId.convertStrToBase64())
            .child("userFriendsInfo")
        0
        addedFriendAll.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                //자기자신과 친구인 경우 1로 표시
                for (exist in snapshot.children) {
                    addedFriendList[exist.value.toString()] = 1
                }
                val a = FirebaseDatabase.getInstance().reference.child("UserInfo")
                a.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (exist in snapshot.children) {
                            if (addedFriendList[exist.key.toString().convertBase64ToStr()] == 1)
                                addedFriendList.remove(exist.key.toString())
                            else {
                                addedFriendList[exist.key.toString()] = 1
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
                                if (addedFriendList[addOkUser.key.toString()
                                        .convertBase64ToStr()] == 1
                                ) {
                                    alljoinList.add(
                                        FindFriendInfo(
                                            myFriend.key.toString(),
                                            myFriend.child("profileImage").value.toString(),
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
        listAdapter = binding.root.context.let {
            ArrayAdapter(
                it,
                android.R.layout.simple_list_item_1,
                filteredData
            )
        }!!
        binding.listView.adapter = listAdapter

        binding.friendSearchEditText.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                binding.friendSearchEditText.setQuery("", false)
                binding.listView?.visibility = View.GONE
                friendSearch(dataList.filter { it.contains(query, ignoreCase = true) }.toString())
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                filterData(newText)
                return true
            }
        })

    }

    private fun filterData(query: String) {
        filteredData.clear()
        filteredData.addAll(dataList.filter { it.contains(query, ignoreCase = true) })
        listAdapter.notifyDataSetChanged()
        Log.d("yoosusang1", filteredData.toString())

    }

    private fun friendSearch(searchName: String) {
        var subSearchName = searchName.substring(1,searchName.length-1)
        var myFriendList = ArrayList<FindFriendInfo>()
        val userInfo =  FirebaseDatabase.getInstance().reference.child("UserInfo")
        userInfo.addListenerForSingleValueEvent(object:ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for(addFriends in snapshot.children){
                    if(subSearchName == addFriends.child("nickname").value.toString()){
                        Log.d("yoosusang3",subSearchName)
                        myFriendList.add(
                            FindFriendInfo(
                                addFriends.key.toString(),
                                addFriends.child("profileImage").value.toString(),
                                addFriends.child("nickname").value.toString(),
                                addFriends.child("friendCount")
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

    private fun setupFriendList() {
        val friendList = ArrayMap<String, Int>()
        val myBase = Firebase.auth.currentUser!!.email.toString().convertStrToBase64()
        val userInfo = FirebaseDatabase.getInstance().reference.child("UserInfo")
        val userFriendInfo = userInfo.child(myBase).child("userFriendsInfo")
        userFriendInfo.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                friendList[myBase] = 1
                for (child in snapshot.children) {
                    friendList[child.value.toString()] = 1 //해당 계정이 base로 저장
                }
                userInfo.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (child in snapshot.children) {
                            if (friendList[child.key.toString()]!=1) { //친구 리스트에 존재하지 않으면 나와 친구가 아니다.
                                dataList.add(child.child("nickname").value.toString())
                            }
                        }
                        filteredData.addAll(dataList)
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