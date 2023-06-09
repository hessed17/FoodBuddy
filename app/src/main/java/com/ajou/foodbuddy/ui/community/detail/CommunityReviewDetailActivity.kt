package com.ajou.foodbuddy.ui.community.detail

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.ajou.foodbuddy.R
import com.ajou.foodbuddy.data.firebase.model.community.CommentInfo
import com.ajou.foodbuddy.data.firebase.model.notification.NotificationInfo
import com.ajou.foodbuddy.databinding.ActivityCommunityReviewDetailBinding
import com.ajou.foodbuddy.extensions.convertStrToBase64
import com.ajou.foodbuddy.extensions.convertTimeStampToDate
import com.ajou.foodbuddy.ui.community.singo.SingoActivity
import com.ajou.foodbuddy.ui.restaurant.detail.RestaurantDetailActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage

class CommunityReviewDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCommunityReviewDetailBinding
    private lateinit var commentAdapter: CommentAdapter
    private var comment: String? = null
    private var commentLists = ArrayList<CommentInfo>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCommunityReviewDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        var reviewId = intent.getStringExtra("reviewId").toString()
        //어뎁터 초기화
        initCommentRecycler()
        //제목, 내용, 평점 반영
        initTCR(reviewId)
        //하트버튼을 클릭시 해당 리뷰에 reviewLikeCount증가
        clickheart(reviewId)
        //하트를 자기 자신이 이 리뷰에 눌렀는지 안눌렀는지 체크
        reviewLikeListcheck(reviewId)

        //이미지뷰에 이미지 넣기
        initImageAdd(reviewId)

        //해당 식당명 클릭시 그 식당명으로 가기 -> reviewId
        clickRestaurantName(reviewId)

        //해당 리뷰에 댓글 추가시 해당 댓글 보여주기
        setComment(reviewId)

        //해당 리뷰에 저장되어 있는 코멘트들 불러오기
        initCommentCall(reviewId)

        //신고버튼 클릭시 해당 신고사유지로 이동
        clickSingoButton(reviewId)
    }

    private fun initCommentRecycler() {
        commentAdapter = CommentAdapter()
        binding.commentRecyclerView.adapter = commentAdapter
        binding.commentRecyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
    }


    private fun initTCR(reviewId: String) {
        val reviewInfo = FirebaseDatabase.getInstance().reference.child("ReviewInfo")
        reviewInfo.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (findkeys in snapshot.children) {
                    if (findkeys.key.toString() == reviewId) {
                        binding.resNameTextView.text =
                            findkeys.child("restaurantName").value.toString()
                        binding.reviewTitleTextView.text =
                            findkeys.child("reviewTitle").value.toString()
                        binding.reviewContentTextView.text =
                            findkeys.child("reviewContent").value.toString()
                        binding.reviewLikedNumberTextView.text =
                            findkeys.child("reviewLikeCount").value.toString()
                        binding.reviewRatingnumber.text =
                            findkeys.child("reviewRating").value.toString()
                        binding.reviewRatingBar.rating = findkeys.child("reviewRating")
                            .getValue(Float::class.java)!!
                            .toFloat()
                        val userInfo = FirebaseDatabase.getInstance().reference.child("UserInfo")
                            .child(findkeys.child("userId").value.toString().convertStrToBase64())
                        userInfo.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                binding.writer.text = snapshot.child("nickname").value.toString()
                                Glide.with(binding.root.context)
                                    .load(snapshot.child("profileImage").value.toString())
                                    .into(binding.profileImageView)
                            }

                            override fun onCancelled(error: DatabaseError) {


                            }

                        })
                        break
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {


            }
        })
    }

    private fun clickheart(reviewId: String) {
        binding.reviewLikedImageView.setOnClickListener {
            var check = 0
            val reviewLikeInfo = FirebaseDatabase.getInstance().reference.child("ReviewLikeInfo")
            val reviewInfo = FirebaseDatabase.getInstance().reference.child("ReviewInfo")
                .child(reviewId.toString())

            val reviewLikeInfoInner = reviewLikeInfo.child(reviewId)

            reviewInfo.child("reviewLikeCount")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        reviewLikeInfoInner.addListenerForSingleValueEvent(object :
                            ValueEventListener {
                            override fun onDataChange(snapshotinner: DataSnapshot) {
                                for (inner in snapshotinner.children) {
                                    if (inner.value.toString() == Firebase.auth.currentUser!!.email.toString()
                                    ) {
                                        Log.d("test", inner.value.toString())
                                        check = 1
                                        val count = snapshot.getValue(Int::class.java)?.minus(1)
                                        reviewInfo.child("reviewLikeCount")
                                            .setValue(count)
                                            .addOnSuccessListener {
                                                // Update successful, reflect the updated count in the XML layout
//                                binding.reviewLikedNumberTextView.text = it.toString()
                                                binding.reviewLikedNumberTextView.text =
                                                    count.toString()
                                                binding.reviewLikedImageView.setImageDrawable(
                                                    ContextCompat.getDrawable(
                                                        binding.root.context,
                                                        R.drawable.unheart
                                                    )
                                                )
                                                //reviewLikeInfoInner에 있는거 제거
                                                reviewLikeInfoInner.removeValue()
                                            }
                                            .addOnFailureListener { error ->
                                                // Handle the error if updating count fails
                                            }
                                        break
                                    }
                                }
                                if (check == 0) {

                                    //notificationInfo에 저장해주기
                                    //현재 계정
                                    var userInfo =
                                        FirebaseDatabase.getInstance().reference.child("UserInfo")
                                            .child(
                                                Firebase.auth.currentUser!!.email.toString()
                                                    .toString().convertStrToBase64()
                                            )

                                    userInfo.addListenerForSingleValueEvent(object :
                                        ValueEventListener {
                                        override fun onDataChange(snapshot: DataSnapshot) {
                                            val reviewInfo =
                                                FirebaseDatabase.getInstance().reference.child("ReviewInfo")
                                            reviewInfo.addListenerForSingleValueEvent(object :
                                                ValueEventListener {
                                                override fun onDataChange(snapshots: DataSnapshot) {
                                                    for (findkeys in snapshots.children) {
                                                        if (findkeys.key.toString() == reviewId) {
                                                            FirebaseDatabase.getInstance().reference.child(
                                                                "NotificationInfo"
                                                            )
                                                                .push()
                                                                .setValue(
                                                                    NotificationInfo(
                                                                        reviewId,
                                                                        snapshot.child("nickname").value.toString(),
                                                                        snapshot.child("profileImage").value.toString(),
                                                                        findkeys.child("reviewTitle").value.toString(),
                                                                        "좋아요",
                                                                        System.currentTimeMillis()
                                                                            .toString()
                                                                            .convertTimeStampToDate()
                                                                    )
                                                                )
                                                        }
                                                    }
                                                }

                                                override fun onCancelled(error: DatabaseError) {

                                                }
                                            })
                                        }

                                        override fun onCancelled(error: DatabaseError) {


                                        }
                                    })

                                    val count = snapshot.getValue(Int::class.java)?.plus(1)
                                    reviewInfo.child("reviewLikeCount")
                                        .setValue(count)
                                        .addOnSuccessListener {
                                            // Update successful, reflect the updated count in the XML layout
//                                binding.reviewLikedNumberTextView.text = it.toString()
                                            binding.reviewLikedNumberTextView.text =
                                                count.toString()
                                            //이떄 ReviewLikeInfo에 자기 id넣기. 다시 조회할 떄 자기 아이디가 있으면 이 하트는 빨간하트로 변경됨
                                            reviewLikeInfo.child(reviewId).push().setValue(
                                                Firebase.auth.currentUser!!.email.toString()
                                            )
                                            binding.reviewLikedImageView.setImageDrawable(
                                                ContextCompat.getDrawable(
                                                    binding.root.context,
                                                    R.drawable.heart
                                                )
                                            )
                                        }
                                        .addOnFailureListener { error ->
                                            // Handle the error if updating count fails
                                        }


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
    }

    private fun reviewLikeListcheck(reviewId: String) {
        val reviewLikeInfo = FirebaseDatabase.getInstance().reference.child("ReviewLikeInfo")
            .child(reviewId.toString())
        reviewLikeInfo.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (checkLieks in snapshot.children) {
                    if (checkLieks.value.toString() == Firebase.auth.currentUser!!.email.toString()) {
                        binding.reviewLikedImageView.setImageDrawable(
                            ContextCompat.getDrawable(
                                binding.root.context,
                                R.drawable.heart
                            )
                        )
                        break
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

    }

    private fun initImageAdd(reviewId: String) {
        val imageList = ArrayList<String>()
        val reviewInfoInner =
            FirebaseDatabase.getInstance().reference.child("ReviewInfo").child(reviewId)
        val restaurantImages = reviewInfoInner.child("restaurantImage")
        restaurantImages.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val cnt = snapshot.childrenCount
                for (child in snapshot.children) {
                    val filename = child.child("fileName").value.toString()
                    val storageRef = FirebaseStorage.getInstance().reference
                    val reviewRef = storageRef.child("review").child(filename)
                    val downloadUrlTask = reviewRef.downloadUrl
                    downloadUrlTask.addOnSuccessListener { uri ->
                        imageList.add(uri.toString())
                        if(imageList.size==cnt.toInt()){
                            val adapter = ViewPagerAdapter(binding.root.context, imageList)
                            binding.reviewImageViewPager2.adapter = adapter
                        }
                    }.addOnFailureListener { exception ->
                        // Handle download URL retrieval failure
                        // Log or display an error message
                    }
                }

                Log.d("yoosusang16", imageList.size.toString())
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun clickRestaurantName(reviewId: String) {
        binding.resNameTextView.setOnClickListener {
            val reviewInfo =
                FirebaseDatabase.getInstance().reference.child("ReviewInfo").child(reviewId)
            reviewInfo.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val intent =
                        Intent(binding.root.context, RestaurantDetailActivity::class.java)
                    intent.putExtra(
                        RESTAURANT_NAME,
                        snapshot.child("restaurantName").value.toString()
                    )
                    startActivity(intent)
                }

                override fun onCancelled(error: DatabaseError) {


                }
            })
        }
    }

    private fun setComment(reviewId: String) {
        var cnt = 0
        binding.myReviewCommentAddEditText.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE || event?.keyCode == KeyEvent.KEYCODE_ENTER) {
                if (cnt == 2)
                    cnt = 0
                cnt++
                if (cnt == 1) {
                    comment = binding.myReviewCommentAddEditText.text.toString()
                    addComment(reviewId, comment!!)
                }
                binding.myReviewCommentAddEditText.text.clear()
                true
            } else {
                false
            }
        }
    }

    private fun addComment(reviewId: String, comment: String) {
        Log.d("yoyoyyy", comment.toString())
        val userInfoInnerName = Firebase.database.reference.child("UserInfo")
            .child(Firebase.auth.currentUser!!.email.toString().convertStrToBase64())
        userInfoInnerName.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var comments = CommentInfo(
                    snapshot.child("profileImage").value.toString(),
                    snapshot.child("nickname").value.toString(),
                    comment,
                    System.currentTimeMillis().toString().convertTimeStampToDate()
                )
                FirebaseDatabase.getInstance().reference.child("CommentInfo").child(reviewId).push()
                    .setValue(comments)
                commentLists.add(comments)
                commentAdapter.submitList(commentLists)
                commentAdapter.notifyDataSetChanged()
                binding.commentNumber.text = commentLists.size.toString()

                //notificationInfo에 저장해주기
                //현재 계정
                var userInfo =
                    FirebaseDatabase.getInstance().reference.child("UserInfo")
                        .child(
                            Firebase.auth.currentUser!!.email.toString()
                                .toString().convertStrToBase64()
                        )

                userInfo.addListenerForSingleValueEvent(object :
                    ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val reviewInfo =
                            FirebaseDatabase.getInstance().reference.child("ReviewInfo")
                        reviewInfo.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshots: DataSnapshot) {
                                for (findkeys in snapshots.children) {
                                    if (findkeys.key.toString() == reviewId) {
                                        FirebaseDatabase.getInstance().reference.child("NotificationInfo")
                                            .push()
                                            .setValue(
                                                NotificationInfo(
                                                    reviewId,
                                                    snapshot.child("nickname").value.toString(),
                                                    snapshot.child("profileImage").value.toString(),
                                                    findkeys.child("reviewTitle").value.toString(),
                                                    "댓글",
                                                    System.currentTimeMillis().toString()
                                                        .convertTimeStampToDate()
                                                )
                                            )
                                        //해당 리뷰를 작성한 사람에게 알림을 날린다. 리뷰작성자 == 현재 계정시 날린다.
                                        if (findkeys.child("userId").value.toString() == Firebase.auth.currentUser!!.email.toString()) {

//                                            NotificationUtils.showNotification(binding.root.context,reviewId)

                                        }
                                    }
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

    private fun initCommentCall(reviewID: String) {
        val commentInfo =
            FirebaseDatabase.getInstance().reference.child("CommentInfo").child(reviewID)
        commentInfo.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (ch in snapshot.children) {
                    var comments = CommentInfo(
                        ch.child("profileImage").value.toString(),
                        ch.child("userId").value.toString(),
                        ch.child("userComment").value.toString(),
                        ch.child("uploadTime").value.toString()
                    )
                    commentLists.add(comments)
                    commentAdapter.submitList(commentLists)
                    commentAdapter.notifyDataSetChanged()
                    binding.commentNumber.text = commentLists.size.toString()

                }
            }

            override fun onCancelled(error: DatabaseError) {


            }
        })

    }

    private fun clickSingoButton(reviewId: String) {
        binding.singoButton.setOnClickListener {
            val intent = Intent(this, SingoActivity::class.java)
            intent.putExtra("reviewId", reviewId)
            startActivity(intent)
        }
    }

    companion object {
        const val RESTAURANT_NAME = "RESTAURANT_NAME"
    }
}
