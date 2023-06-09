package com.ajou.foodbuddy.ui.restaurant.detail

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.PopupWindow
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil.ItemCallback
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ajou.foodbuddy.R
import com.ajou.foodbuddy.data.firebase.model.community.AddedProcessReviewInfo
import com.ajou.foodbuddy.databinding.ItemReviewBinding
import com.ajou.foodbuddy.ui.community.detail.CommunityReviewDetailActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.text.DecimalFormat

class ReviewAdapter(refreshListener1: Context, private val refreshListener: ReviewDeleteListener) :
    ListAdapter<AddedProcessReviewInfo, ReviewAdapter.ViewHolder>(diffUtil) {
    interface ReviewDeleteListener {
        fun onReviewDeleted()
    }

    inner class ViewHolder(private val binding: ItemReviewBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(reviewModel: AddedProcessReviewInfo) {
            binding.reviewTitleTextView.text = reviewModel.reviewTitle //제목
            binding.reviewContentText.text = reviewModel.reviewContent// 내용
            binding.categoryTextView.text = reviewModel.categoryId// 카테고리
            binding.likeNumberTextview.text = reviewModel.reviewLikeCount.toString() // 좋아요 수
            binding.reviewNumberText.text = DecimalFormat("#.#").format(reviewModel.reviewRating)// 리뷰평점 수
            binding.reviewRatingBar.rating = DecimalFormat("#.#").format(reviewModel.reviewRating).toFloat()//평점 rating
            if (reviewModel.userId == Firebase.auth.currentUser!!.email.toString()
            ) {
                binding.deleteImageButton.visibility = View.VISIBLE
            }
            binding.deleteImageButton.setOnClickListener {
                val inflater = LayoutInflater.from(binding.root.context)
                val popupView = inflater.inflate(R.layout.popup_communitydelete, null)
                val popupWindow = PopupWindow(
                    popupView,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    true
                )

                // Set popupWindow properties
                popupWindow.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) // Make the background transparent
                popupWindow.isOutsideTouchable =
                    true // Dismiss the popup window when clicked outside

                // Show the popup window in the center
                popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0)

                val deleteCommunityButton = popupView.findViewById<Button>(R.id.deleteButton)
                deleteCommunityButton.setOnClickListener {
                    FirebaseDatabase.getInstance().reference.child("ReviewInfo")
                        .addListenerForSingleValueEvent(
                            object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    for (child in snapshot.children) {
                                        if (child.child("userId").value.toString() == reviewModel.userId && child.child(
                                                "uploadTime"
                                            ).value.toString() == reviewModel.uploadTime
                                        ) {
                                            child.ref.removeValue()
                                            FirebaseDatabase.getInstance().reference.child("CommentInfo")
                                                .addListenerForSingleValueEvent(
                                                    object : ValueEventListener {
                                                        override fun onDataChange(snapshot: DataSnapshot) {
                                                            for (child in snapshot.children) {
                                                                if (child.key.toString() == reviewModel.reviewid) {
                                                                    child.ref.removeValue()
                                                                    break
                                                                }
                                                            }
                                                        }

                                                        override fun onCancelled(error: DatabaseError) {

                                                        }
                                                    }
                                                )
                                            FirebaseDatabase.getInstance().reference.child("ReviewLikeInfo")
                                                .addListenerForSingleValueEvent(
                                                    object : ValueEventListener {
                                                        override fun onDataChange(snapshot: DataSnapshot) {
                                                            for (child in snapshot.children) {
                                                                if (child.key.toString() == reviewModel.reviewid) {
                                                                    child.ref.removeValue()
                                                                    break
                                                                }
                                                            }
                                                        }

                                                        override fun onCancelled(error: DatabaseError) {

                                                        }
                                                    }
                                                )
                                            break
                                        }
                                    }

                                }

                                override fun onCancelled(error: DatabaseError) {

                                }
                            }
                        )
                    refreshListener.onReviewDeleted()
                    popupWindow.dismiss()
                }

                val nodeleteCommunityButton = popupView.findViewById<Button>(R.id.nodeleteButton)
                nodeleteCommunityButton.setOnClickListener {
                    popupWindow.dismiss()

                }
            }

            //없어도 된다.
            val mainImage = FirebaseStorage.getInstance().reference
            mainImage.child("Restaurant/${reviewModel.restaurantName}/image/thumbnail.jpg").downloadUrl.addOnSuccessListener { uri ->
                // When image load is successful
                Glide.with(binding.root.context).load(uri).into(binding.profileImageView)
            }.addOnFailureListener { exception ->

            }


            binding.reviewContraintLayout.setOnClickListener {
                //해당 리뷰들을 보여줘야 한다. 클릭시 자기자신의 reviewKey 값을 보낸다.
                var intent = Intent(binding.root.context, CommunityReviewDetailActivity::class.java)
                intent.putExtra("reviewId", reviewModel.reviewid)
                binding.root.context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemReviewBinding.inflate(LayoutInflater.from(parent.context), parent, false))


    override fun onBindViewHolder(holder: ReviewAdapter.ViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    companion object {
        val diffUtil = object : ItemCallback<AddedProcessReviewInfo>() {
            override fun areItemsTheSame(
                oldItem: AddedProcessReviewInfo,
                newItem: AddedProcessReviewInfo
            ): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(
                oldItem: AddedProcessReviewInfo,
                newItem: AddedProcessReviewInfo
            ): Boolean {
                return oldItem.reviewid == newItem.reviewid
            }

        }
    }
}
