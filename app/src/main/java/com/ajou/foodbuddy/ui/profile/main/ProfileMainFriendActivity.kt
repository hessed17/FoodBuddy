package com.ajou.foodbuddy.ui.profile.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.ajou.foodbuddy.data.firebase.model.restaurant.MyRestaurant
import com.ajou.foodbuddy.databinding.FragmentProfileMainBinding
import com.ajou.foodbuddy.extensions.convertBase64ToStr
import com.ajou.foodbuddy.ui.restaurant.detail.RestaurantDetailActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage

class ProfileMailFriendActivity : AppCompatActivity() {
    private lateinit var binding: FragmentProfileMainBinding
    var auth: FirebaseAuth = FirebaseAuth.getInstance()
    var database: DatabaseReference = Firebase.database.reference //실시간 파이어베이스 저장시 사용
    private lateinit var Profileadapter: ProfileRestaurantAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentProfileMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val FriendName = intent.getStringExtra("UserId2").toString()
        Log.d("test",FriendName)
        //프로필 갱신
        initProfile(FriendName)

        //리사이클러뷰 초기화

        initRestaurantAdapter()
        //친구 식당 추가하기
        pushRestaurant(FriendName)

        //deleteactivty에서 왔으면 친구 삭제 visible, addactivity에서 왔으면 친구 추가 visible
        if (intent.getStringExtra("delete").isNullOrEmpty()) {
            binding.friendAddButton.visibility = View.VISIBLE
            binding.friendRemoveButton.visibility = View.GONE
            binding.logoutButton.visibility = View.GONE

        } else if (intent.getStringExtra("add").isNullOrEmpty()) {
            binding.friendRemoveButton.visibility = View.VISIBLE
            binding.friendAddButton.visibility = View.GONE
            binding.logoutButton.visibility = View.GONE
        }
        addFriend(Firebase.auth.currentUser!!.email.toString().convertBase64ToStr(), FriendName)
        deleteFriend(Firebase.auth.currentUser!!.email.toString().convertBase64ToStr(), FriendName)
    }

    private fun initProfile(userName: String) {
        val UserInfoRef = FirebaseDatabase.getInstance().reference.child("UserInfo")

        UserInfoRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val MyUser = dataSnapshot.child(userName) // 해당친구보여주기
                binding.friendNameTextView.text = MyUser.child("nickname").value.toString()
                binding.navigateFriendListButton.text = MyUser.child("friendCount").value.toString()
                //이미지 url or string?
                //Glide.with(binding.root.context).load("profileImage").into(binding.profileImageButton)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle the error if retrieval is canceled
                Log.d("Error", "Error retrieving data: ${databaseError.message}")
            }
        })
    }

    private fun initRestaurantAdapter() {
        Profileadapter = ProfileRestaurantAdapter {
            startActivity(Intent(this, RestaurantDetailActivity::class.java).apply {
                putExtra(RestaurantDetailActivity.RESTAURANT_NAME, it.restaurantName)
            })
        }
        binding.myMenuListRecyclerView.adapter = Profileadapter
        binding.myMenuListRecyclerView.layoutManager = GridLayoutManager(binding.root.context, 2)
    }

    private fun pushRestaurant(userName: String) {
        var list = ArrayList<MyRestaurant>()
        var cnt = 0
        val ResInfoRef = FirebaseDatabase.getInstance().reference.child("FavoriteRestaurantInfo")
        ResInfoRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val MyUserRes =
                    dataSnapshot.child(userName)
                val totalResCount =
                    MyUserRes.childrenCount.toInt() // RestaurantArray 안에 있는 key:value 개수
                Log.d("test",totalResCount.toString())
                for (res in MyUserRes.children) {
                    val mainImage = FirebaseStorage.getInstance().reference
                    mainImage.child("Restaurant/${res.value.toString()}/image/thumbnail.jpg").downloadUrl.addOnSuccessListener { uri ->
                        cnt++
                        list.add(MyRestaurant(res.value.toString(), uri))
                        if (cnt == totalResCount)
                            Profileadapter.submitList(list)
                    }

                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle the error if retrieval is canceled
                Log.d("Error", "Error retrieving data: ${databaseError.message}")
            }
        })


    }
    private fun deleteFriend(myName: String, friendName: String) {
        binding.friendRemoveButton.setOnClickListener {
            val userFriendInfoRef = FirebaseDatabase.getInstance().reference
                .child("UserInfo")
                .child(myName)
                .child("UserFriendsInfo")

            // Query to find the child node with the desired value
            val query = userFriendInfoRef.orderByValue().equalTo(friendName)

            query.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (childSnapshot in dataSnapshot.children) {
                        childSnapshot.ref.removeValue()
                    }
                    val userInfoRef = FirebaseDatabase.getInstance().reference.child("UserInfo")
                    val myNameRef = userInfoRef.child(myName)
                    myNameRef.child("friendCount")
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                var count = dataSnapshot.getValue(Int::class.java)?.minus(1)
                                myNameRef.child("friendCount")
                                    .setValue(count) // Adjust only the corresponding friendCount value
                                    .addOnSuccessListener {
                                        // Update successful
                                    }
                                    .addOnFailureListener { error ->
                                        // Handle the error if updating count fails
                                    }
                            }

                            override fun onCancelled(databaseError: DatabaseError) {
                                // Handle the error if retrieval is canceled
                            }
                        })
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle the error if retrieval is canceled
                }
            })

            finish()
        }
    }

    private fun addFriend(myName: String, friendName: String) {
        binding.friendAddButton.setOnClickListener {
            database.child("UserInfo").child(myName).child("userFriendsInfo").push().setValue(friendName)
                .addOnSuccessListener {
                    // Increase friendCount in UserInfo by 1
                    database.child("UserInfo").child(myName).child("friendCount")
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                // Get the current friendCount value
                                val currentFriendCount = dataSnapshot.getValue(Int::class.java) ?: 0

                                // Update the friendCount value
                                val updatedFriendCount = currentFriendCount + 1
                                database.child("UserInfo").child(myName).child("friendCount")
                                    .setValue(updatedFriendCount)
                                    .addOnSuccessListener {
                                        // Success! The value and friendCount were updated
                                    }
                                    .addOnFailureListener { exception ->
                                        // Handle the error if the friendCount update fails
                                    }
                            }

                            override fun onCancelled(databaseError: DatabaseError) {
                                // Handle the error if the friendCount retrieval is cancelled
                            }
                        })
                }
                .addOnFailureListener { exception ->
                    // Handle the error if the value addition fails
                }
            finish()
        }
    }
}
