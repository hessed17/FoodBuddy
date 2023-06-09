package com.ajou.foodbuddy.ui.community.main

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.ajou.foodbuddy.BaseFragment
import com.ajou.foodbuddy.R
import com.ajou.foodbuddy.data.firebase.model.community.AddedProcessReviewInfo
import com.ajou.foodbuddy.databinding.FragmentCommunityMainBinding
import com.ajou.foodbuddy.ui.community.notification.CommunityNotificationActivity
import com.ajou.foodbuddy.ui.community.register.CommunityRegisterNewActivity
import com.ajou.foodbuddy.ui.restaurant.detail.ReviewAdapter
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class CommunityMainFragment : BaseFragment<FragmentCommunityMainBinding>(),ReviewAdapter.ReviewDeleteListener  {
    private lateinit var reviewadapater: ReviewAdapter
    var database: DatabaseReference = Firebase.database.reference
    override fun getViewBinding(): FragmentCommunityMainBinding =
        FragmentCommunityMainBinding.inflate(layoutInflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
            clickNewRegister()
        //모든 커뮤니티들 리뷰 조회
        reviewInfoListAll()
        //리사이클러뷰 초기화
        initReviewAdapters()
        //스피너 적용
        initSpinner()
        //알림내역 클륵시
        seleckNotification()
        //커뮤니티 삭제
        deleteCommunity()

    }

    override fun onResume() {
        super.onResume()
        clickNewRegister()
        //모든 커뮤니티들 리뷰 조회
        reviewInfoListAll()
        //리사이클러뷰 초기화
        initReviewAdapters()
        //스피너 적용
        initSpinner()


    }

    private fun clickNewRegister() {
        _binding?.reviewRegisterActionButton?.setOnClickListener {
            val intent = Intent(_binding?.root?.context, CommunityRegisterNewActivity::class.java)
            startActivity(intent)
        }
    }

    private fun reviewInfoListAll() {
        val reviewInfo = FirebaseDatabase.getInstance().reference.child("ReviewInfo")

    }

    private fun initReviewAdapters() {
        reviewadapater = ReviewAdapter(requireContext(),this)
        _binding?.chattingroomListRecyclerView?.adapter = reviewadapater
        _binding?.chattingroomListRecyclerView?.layoutManager =
            LinearLayoutManager(_binding?.root?.context, LinearLayoutManager.VERTICAL, false)
    }

    override fun onReviewDeleted() {
        // Implement the logic to refresh the RecyclerView
        // Reload the data, update the UI, or perform any other required operations
        clickNewRegister()
        //모든 커뮤니티들 리뷰 조회
        reviewInfoListAll()
        //리사이클러뷰 초기화
        initReviewAdapters()
        //스피너 적용
        initSpinner()

    }
    //스피너 적용
    private fun initSpinner() {
        var SpinnerList = ArrayList<AddedProcessReviewInfo>()
        _binding?.categoriesSpinner?.adapter = _binding?.root?.context?.let {
            ArrayAdapter.createFromResource(
                it, R.array.spinner_community_review, R.layout.spin_color
            )
        }
        _binding?.categoriesSpinner?.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {

                }

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long,
                ) {
                    when (position) {

                        //최신순이므로 초기 설정
                        0 -> {
                            SpinnerList.clear()
                            val headquery = database.child("ReviewInfo")
                            headquery.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    val subquery =
                                        database.child("ReviewInfo").orderByChild("uploadTime")
                                    subquery.addChildEventListener(object : ChildEventListener {
                                        override fun onChildAdded(
                                            snapshot: DataSnapshot, previousChildName: String?
                                        ) {
                                            val reviewId = snapshot.key.toString()
                                            val userId =
                                                snapshot.child("userId").value.toString()
                                            val reviewTitle =
                                                snapshot.child("reviewTitle").value.toString().substring(0,if(snapshot.child("reviewTitle").value.toString().length>8) 8 else snapshot.child("reviewTitle").value.toString().length)
                                            val reviewContent =
                                                snapshot.child("reviewContent").value.toString().substring(0,if(snapshot.child("reviewContent").value.toString().length>8) 8 else snapshot.child("reviewContent").value.toString().length)
                                            val categoryId =
                                                snapshot.child("categoryId").value.toString()
                                            val restaurantName =
                                                snapshot.child("restaurantName").value.toString()
                                            val reviewRating =
                                                snapshot.child("reviewRating")
                                                    .getValue(Float::class.java)!!
                                                    .toFloat()
                                            val reviewLikeCount =
                                                snapshot.child("reviewLikeCount")
                                                    .getValue(Int::class.java)!!.toInt()
                                            val uploadTime =
                                                snapshot.child("uploadTime").value.toString()
                                            val processedReviewInfo = AddedProcessReviewInfo(
                                                reviewId,
                                                userId,
                                                reviewTitle,
                                                reviewContent,
                                                categoryId,
                                                restaurantName,
                                                reviewRating,
                                                reviewLikeCount,
                                                uploadTime
                                            )
                                            // Add the processed review to the list
                                            SpinnerList.add(processedReviewInfo)

                                            // Sort the list in ascending order based on reviewRating
                                            SpinnerList.sortByDescending { it.uploadTime }
                                            // Update the review list here
                                            reviewadapater.submitList(SpinnerList)
                                            reviewadapater.notifyDataSetChanged()

                                        }

                                        override fun onChildChanged(
                                            snapshot: DataSnapshot, previousChildName: String?
                                        ) {

                                        }

                                        override fun onChildRemoved(snapshot: DataSnapshot) {

                                        }

                                        override fun onChildMoved(
                                            snapshot: DataSnapshot, previousChildName: String?
                                        ) {

                                        }

                                        override fun onCancelled(error: DatabaseError) {

                                        }
                                    })

                                }

                                override fun onCancelled(error: DatabaseError) {

                                }

                            })
                        }
                        //평점순
                        1 -> {
                            SpinnerList.clear()
                            val headquery = database.child("ReviewInfo")
                            headquery.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    val subquery =
                                        database.child("ReviewInfo").orderByChild("reviewRating")
                                    subquery.addChildEventListener(object : ChildEventListener {
                                        override fun onChildAdded(
                                            snapshot: DataSnapshot, previousChildName: String?
                                        ) {
                                            val reviewId = snapshot.key.toString()
                                            val userId =
                                                snapshot.child("userId").value.toString()
                                            val reviewTitle =
                                                snapshot.child("reviewTitle").value.toString().substring(0,if(snapshot.child("reviewTitle").value.toString().length>8) 8 else snapshot.child("reviewTitle").value.toString().length)
                                            val reviewContent =
                                                snapshot.child("reviewContent").value.toString().substring(0,if(snapshot.child("reviewContent").value.toString().length>8) 8 else snapshot.child("reviewContent").value.toString().length)
                                            val categoryId =
                                                snapshot.child("categoryId").value.toString()
                                            val restaurantName =
                                                snapshot.child("restaurantName").value.toString()
                                            val reviewRating =
                                                snapshot.child("reviewRating")
                                                    .getValue(Float::class.java)!!
                                                    .toFloat()
                                            val reviewLikeCount =
                                                snapshot.child("reviewLikeCount")
                                                    .getValue(Int::class.java)!!.toInt()
                                            val uploadTime =
                                                snapshot.child("uploadTime").value.toString()
                                            val processedReviewInfo = AddedProcessReviewInfo(
                                                reviewId,
                                                userId,
                                                reviewTitle,
                                                reviewContent,
                                                categoryId,
                                                restaurantName,
                                                reviewRating,
                                                reviewLikeCount,
                                                uploadTime
                                            )

                                            // Add the processed review to the list
                                            SpinnerList.add(processedReviewInfo)

                                            SpinnerList.sortByDescending { it.reviewRating }
                                            // Update the review list here
                                            reviewadapater.submitList(SpinnerList)
                                            reviewadapater.notifyDataSetChanged()

                                        }

                                        override fun onChildChanged(
                                            snapshot: DataSnapshot, previousChildName: String?
                                        ) {

                                        }

                                        override fun onChildRemoved(snapshot: DataSnapshot) {

                                        }

                                        override fun onChildMoved(
                                            snapshot: DataSnapshot, previousChildName: String?
                                        ) {

                                        }

                                        override fun onCancelled(error: DatabaseError) {

                                        }
                                    })

                                }

                                override fun onCancelled(error: DatabaseError) {

                                }

                            })
                        }
                        //좋아요순
                        2 -> {
                            SpinnerList.clear()
                            val headquery = database.child("ReviewInfo")
                            headquery.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    val subquery =
                                        database.child("ReviewInfo").orderByChild("reviewLikeCount")
                                    subquery.addChildEventListener(object : ChildEventListener {
                                        override fun onChildAdded(
                                            snapshot: DataSnapshot, previousChildName: String?
                                        ) {
                                            val reviewId = snapshot.key.toString()
                                            val userId =
                                                snapshot.child("userId").value.toString()
                                            val reviewTitle =
                                                snapshot.child("reviewTitle").value.toString().substring(0,if(snapshot.child("reviewTitle").value.toString().length>8) 8 else snapshot.child("reviewTitle").value.toString().length)
                                            val reviewContent =
                                                snapshot.child("reviewContent").value.toString().substring(0,if(snapshot.child("reviewContent").value.toString().length>8) 8 else snapshot.child("reviewContent").value.toString().length)
                                            val categoryId =
                                                snapshot.child("categoryId").value.toString()
                                            val restaurantName =
                                                snapshot.child("restaurantName").value.toString()
                                            val reviewRating =
                                                snapshot.child("reviewRating")
                                                    .getValue(Float::class.java)!!
                                                    .toFloat()
                                            val reviewLikeCount =
                                                snapshot.child("reviewLikeCount")
                                                    .getValue(Int::class.java)!!.toInt()
                                            val uploadTime =
                                                snapshot.child("uploadTime").value.toString()
                                            val processedReviewInfo = AddedProcessReviewInfo(
                                                reviewId,
                                                userId,
                                                reviewTitle,
                                                reviewContent,
                                                categoryId,
                                                restaurantName,
                                                reviewRating,
                                                reviewLikeCount,
                                                uploadTime
                                            )
                                            // Add the processed review to the list
                                            SpinnerList.add(processedReviewInfo)

                                            SpinnerList.sortByDescending { it.reviewLikeCount }
                                            // Update the review list here
                                            reviewadapater.submitList(SpinnerList)
                                            reviewadapater.notifyDataSetChanged()
                                        }

                                        override fun onChildChanged(
                                            snapshot: DataSnapshot, previousChildName: String?
                                        ) {

                                        }

                                        override fun onChildRemoved(snapshot: DataSnapshot) {

                                        }

                                        override fun onChildMoved(
                                            snapshot: DataSnapshot, previousChildName: String?
                                        ) {

                                        }

                                        override fun onCancelled(error: DatabaseError) {

                                        }
                                    })

                                }

                                override fun onCancelled(error: DatabaseError) {

                                }

                            })
                        }
                        //한식
                        3 -> {
                            categoryNames("한식")
                        }
                        //일식
                        4 -> {
                            categoryNames("일식")
                        }
                        //중식
                        5 -> {
                            categoryNames("중식")
                        }
                        //양식
                        6 -> {
                            categoryNames("양식")
                        }
                    }
                }

            }
    }
    private fun categoryNames(name:String){
        var SpinnerList = ArrayList<AddedProcessReviewInfo>()
        SpinnerList.clear()
        val headquery = database.child("ReviewInfo")
        headquery.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val subquery =
                    database.child("ReviewInfo").orderByChild("categoryId")
                subquery.addChildEventListener(object : ChildEventListener {
                    override fun onChildAdded(
                        snapshot: DataSnapshot, previousChildName: String?
                    ) {
                        val reviewId = snapshot.key.toString()
                        val userId =
                            snapshot.child("userId").value.toString()
                        val reviewTitle =
                            snapshot.child("reviewTitle").value.toString()
                        val reviewContent =
                            snapshot.child("reviewContent").value.toString()
                        val categoryId =
                            snapshot.child("categoryId").value.toString()
                        val restaurantName =
                            snapshot.child("restaurantName").value.toString()
                        val reviewRating =
                            snapshot.child("reviewRating")
                                .getValue(Float::class.java)!!
                                .toFloat()
                        val reviewLikeCount =
                            snapshot.child("reviewLikeCount")
                                .getValue(Int::class.java)!!.toInt()
                        val uploadTime =
                            snapshot.child("uploadTime").value.toString()
                        if (categoryId == name) {
                            val processedReviewInfo = AddedProcessReviewInfo(
                                reviewId,
                                userId,
                                reviewTitle,
                                reviewContent,
                                categoryId,
                                restaurantName,
                                reviewRating,
                                reviewLikeCount,
                                uploadTime
                            )
                            // Add the processed review to the list
                            SpinnerList.add(processedReviewInfo)
                            SpinnerList.sortByDescending { it.uploadTime }
                            // Update the review list here
                            reviewadapater.submitList(SpinnerList)
                            reviewadapater.notifyDataSetChanged()
                        }else{
                            reviewadapater.submitList(SpinnerList)
                            reviewadapater.notifyDataSetChanged()
                        }
                    }

                    override fun onChildChanged(
                        snapshot: DataSnapshot, previousChildName: String?
                    ) {

                    }

                    override fun onChildRemoved(snapshot: DataSnapshot) {

                    }

                    override fun onChildMoved(
                        snapshot: DataSnapshot, previousChildName: String?
                    ) {

                    }

                    override fun onCancelled(error: DatabaseError) {

                    }
                })
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }
    private fun seleckNotification(){
        _binding?.notificationButton?.setOnClickListener {
            var intent = Intent(_binding?.root?.context, CommunityNotificationActivity::class.java)
            startActivity(intent)


        }
    }
    private fun deleteCommunity(){


    }


}