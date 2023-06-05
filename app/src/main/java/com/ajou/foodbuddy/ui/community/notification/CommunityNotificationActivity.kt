package com.ajou.foodbuddy.ui.community.notification

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.ajou.foodbuddy.data.firebase.model.notification.NotificationInfo
import com.ajou.foodbuddy.databinding.FragmentCommunityNotificationBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase

class CommunityNotificationActivity : AppCompatActivity() {
    private lateinit var binding: FragmentCommunityNotificationBinding
    private lateinit var notificationAdapter: NotificationAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentCommunityNotificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //리사이클러뷰 초기화
        initNotificationRecycler()
        //알림내역 저장
        setNotification()

    }

    override fun onRestart() {
        super.onRestart()
        //리사이클러뷰 초기화
        initNotificationRecycler()
        //알림내역 저장
        setNotification()


    }

    private fun initNotificationRecycler() {
        notificationAdapter = NotificationAdapter()
        binding.notifyListRecyclerView.adapter = notificationAdapter
        binding.notifyListRecyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
    }

    private fun setNotification() {
        var notificationList = ArrayList<NotificationInfo>()
        var reviewInfo = FirebaseDatabase.getInstance().reference.child("ReviewInfo")
        val notificationInfo = FirebaseDatabase.getInstance().reference.child("NotificationInfo")
        reviewInfo.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (checkReviewId in snapshot.children) {
                    //현재 리뷰ID에 해당하는게 내가 쓴 글이 맞으면 내 리뷰에 댓글, 좋아요를 기록한 사람들의 기록들을 모두 저장
                    if (checkReviewId.child("userId").value.toString() == Firebase.auth.currentUser!!.email.toString()) {
                        notificationInfo.addListenerForSingleValueEvent(object:ValueEventListener{
                            override fun onDataChange(snapshot: DataSnapshot) {
                                for(reviewIds in snapshot.children){
                                    //공지알림에서 내 리뷰에 쓴거면
                                    if(reviewIds.child("reviewId").value.toString() == checkReviewId.key.toString()){
                                        notificationList.add(
                                            NotificationInfo(
                                                reviewIds.child("reviewId").value.toString(),
                                                reviewIds.child("nickname").value.toString(),
                                                reviewIds.child("profileImage").value.toString(),
                                                reviewIds.child("reviewTitle").value.toString(),
                                                reviewIds.child("message").value.toString(),
                                                reviewIds.child("uploadTime").value.toString()
                                        ))
                                        notificationAdapter.submitList(notificationList)
                                        notificationAdapter.notifyDataSetChanged()
                                    }
                                }
                            }
                            override fun onCancelled(error: DatabaseError) {

                            }

                        })
                    }
                }

            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    companion object {
        private const val CHANNEL_ID = "my_channel_id"
        private const val CHANNEL_NAME = "My Channel"
    }
}