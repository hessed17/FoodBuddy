package com.ajou.foodbuddy.ui.restaurant.detail

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil.ItemCallback
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ajou.foodbuddy.data.firebase.model.ProcessedReviewInfo
import com.ajou.foodbuddy.databinding.ItemReviewBinding

class ReviewAdapter : ListAdapter<ProcessedReviewInfo, ReviewAdapter.ViewHolder>(diffUtil) {

    inner class ViewHolder(private val binding: ItemReviewBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(reviewModel: ProcessedReviewInfo) {
            binding.reviewTitleTextView.text = reviewModel.reviewTitle //제목
            binding.reviewContentText.text = reviewModel.reviewContent// 내용
            binding.categoryTextView.text = reviewModel.categoryId// 카테고리
            binding.likeNumberTextview.text = reviewModel.reviewLikeCount.toString() // 좋아요 수
            binding.reviewNumberText.text = reviewModel.reviewRating.toString()// 리뷰평점 수
            binding.reviewRatingBar.rating = reviewModel.reviewRating //평점 rating
            binding.reviewContraintLayout.setOnClickListener{
                //Intent 보내기 Review Id?..



            }
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemReviewBinding.inflate(LayoutInflater.from(parent.context), parent, false))




    override fun onBindViewHolder(holder: ReviewAdapter.ViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    companion object {
        val diffUtil = object : ItemCallback<ProcessedReviewInfo>() {
            override fun areItemsTheSame(oldItem: ProcessedReviewInfo, newItem: ProcessedReviewInfo): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: ProcessedReviewInfo, newItem: ProcessedReviewInfo): Boolean {
                return oldItem.reviewId == newItem.reviewId
            }

        }
    }
}