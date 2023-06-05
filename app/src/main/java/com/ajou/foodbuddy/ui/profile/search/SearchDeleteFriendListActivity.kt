package com.ajou.foodbuddy.ui.profile.search

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.ajou.foodbuddy.data.firebase.model.profile.FindFriendInfo
import com.ajou.foodbuddy.databinding.FragmentSearchUserBinding
import com.ajou.foodbuddy.extensions.convertStrToBase64
import com.google.firebase.auth.ktx.auth
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
    private lateinit var listAdapter: ArrayAdapter<String>
    private val dataList = ArrayList<String>()
    private var filteredData = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentSearchUserBinding.inflate(layoutInflater)
        binding.friendSearchEditText.queryHint = "친구 검색"
        setContentView(binding.root)

        val UserId: String = intent.getStringExtra("UserName").toString() //String 계정으로 들어가기
        //리사이클러뷰 초기화
        initFriendAdapter()
        initFriendListall(UserId)
        //친구검색
        setupEditText()
        setupFriendList()
    }

    override fun onRestart() {
        super.onRestart()
        binding = FragmentSearchUserBinding.inflate(layoutInflater)
        binding.friendSearchEditText.queryHint = "친구 검색"
        setContentView(binding.root)

        val UserId: String = intent.getStringExtra("UserName").toString()

        //리사이클러뷰 초기화
        initFriendAdapter()
        Thread.sleep(300)
        initFriendListall(UserId)
        //친구검색
        setupEditText()

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
                                    myFriend.child("profileImage").value.toString(),
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

    private fun setupEditText() {
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
                Log.d("yoosusang5",dataList.filter { it.contains(query, ignoreCase = true) }.toString())
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                filterData(newText)
                return true
            }
        })
    }
    private fun filterData(query: String) {
        filteredData.clear()
        if(query!="")
            filteredData.addAll(dataList.filter { it.contains(query, ignoreCase = true) })
        listAdapter.notifyDataSetChanged()

        Log.d("yoosusang1",filteredData.toString())
        if (filteredData.isNotEmpty()) {
            binding.listView.visibility = View.VISIBLE
        } else {
            binding.listView.visibility = View.GONE
        }
    }

    private fun friendSearch(searchName: String) {
        var subSearchName = searchName.substring(1,searchName.length-1)
        var myFriendList = ArrayList<FindFriendInfo>()
        val userInfos = FirebaseDatabase.getInstance().reference.child("UserInfo")
        userInfos.addListenerForSingleValueEvent(object:ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for(addedFriend in snapshot.children){
                    if(subSearchName == addedFriend.child("nickname").value.toString()){
                        myFriendList.add(
                            FindFriendInfo(
                                addedFriend.key.toString(),
                                addedFriend.child("profileImage").value.toString(),
                                addedFriend.child("nickname").value.toString(),
                                addedFriend.child("friendCount")
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
    private fun setupFriendList(){

        val myBase =Firebase.auth.currentUser!!.email.toString().convertStrToBase64()
        val friendnInfoRef =
            FirebaseDatabase.getInstance().reference.child("UserInfo")
                .child(myBase)
                .child("userFriendsInfo")
        friendnInfoRef.addListenerForSingleValueEvent(object:ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("yoosusang4",snapshot.toString())
                for(child in snapshot.children){
                    FirebaseDatabase.getInstance().reference.child("UserInfo").child(child.value.toString()).
                        addListenerForSingleValueEvent(object :ValueEventListener{
                            override fun onDataChange(snapshot: DataSnapshot) {
                                dataList.add(snapshot.child("nickname").value.toString())
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