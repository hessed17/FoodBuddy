package com.ajou.foodbuddy.ui.profile.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.ajou.foodbuddy.BaseFragment
import com.ajou.foodbuddy.data.firebase.model.MyRestaurant
import com.ajou.foodbuddy.databinding.FragmentProfileMainBinding
import com.ajou.foodbuddy.ui.profile.search.SearchAddFriendListActivity
import com.ajou.foodbuddy.ui.profile.search.SearchDeleteFriendListActivity
import com.ajou.foodbuddy.ui.restaurant.detail.RestaurantDetailActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope

class ProfileMailFragment : BaseFragment<FragmentProfileMainBinding>(),
    CoroutineScope by MainScope() {
    private val storageRef = Firebase.storage.reference
    private val database = Firebase.database.reference
    private lateinit var Profileadapter: ProfileRestaurantAdapter

    override fun getViewBinding(): FragmentProfileMainBinding =
        FragmentProfileMainBinding.inflate(layoutInflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //프로필 갱신
        initProfile()

        //레스토랑 어뎁터 업데이트
        initRestaurantAdapter()

        //레스토랑 리사이클러뷰에 데이터 넣기 UserName 받아오기
        pushRestaurant()

        //친구 삭제 버튼 클릭
        deleteFriendButtonClick()

        //친구 추가 버튼 클릭
        addFriendButtonClick()

    }
    override fun onResume() {
        super.onResume()
        //프로필 갱신
        initProfile()

        //레스토랑 어뎁터 업데이트
        initRestaurantAdapter()

        //레스토랑 리사이클러뷰에 데이터 넣기 UserName 받아오기
        pushRestaurant()

        //친구 삭제 버튼 클릭
        deleteFriendButtonClick()

        //친구 추가 버튼 클릭
        addFriendButtonClick()

    }
    private fun initProfile(){
        val UserInfoRef = FirebaseDatabase.getInstance().reference.child("UserInfo")

        UserInfoRef.addListenerForSingleValueEvent(object : ValueEventListener {
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
    private fun initRestaurantAdapter(){
        Profileadapter = ProfileRestaurantAdapter {
            startActivity(Intent(requireActivity(), RestaurantDetailActivity::class.java).apply {
                putExtra(RestaurantDetailActivity.RESTAURANT_NAME, it.restaurantName)
            })
        }
        _binding?.myMenuListRecyclerView?.adapter = Profileadapter
        _binding?.myMenuListRecyclerView?.layoutManager = GridLayoutManager(context,2)

    }
    private fun pushRestaurant(){
        var list = ArrayList<MyRestaurant>()
        var cnt =0
        val ResInfoRef = FirebaseDatabase.getInstance().reference.child("FavoriteRestaurantInfo")
        ResInfoRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val MyUserRes = dataSnapshot.child("UserId1") //해당 유저의 목록 보여주기
                val totalResCount = MyUserRes.childrenCount.toInt()
                for(res in MyUserRes.children) {
                    val mainImage = FirebaseStorage.getInstance().reference
                    mainImage.child("Restaurant/${res.value.toString()}/image/thumbnail.jpg").downloadUrl.addOnSuccessListener { uri ->
                        cnt++
                        list.add(MyRestaurant(res.value.toString(),uri))
                        if(cnt==totalResCount)
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
    private fun  deleteFriendButtonClick(){
        _binding?.navigateFriendListButton?.setOnClickListener {
            val intent:Intent = Intent(requireActivity(),SearchDeleteFriendListActivity::class.java)
            intent.putExtra("UserName","UserId1") // 현재 유저 이름 보내기
            startActivity(intent)
        }
    }
    private fun  addFriendButtonClick(){
        _binding?.searchOtherUserButton?.setOnClickListener {
            val intent:Intent = Intent(requireActivity(), SearchAddFriendListActivity::class.java)
            intent.putExtra("UserName","UserId1") // 현재 유저 이름 보내기
            startActivity(intent)

        }
    }
}