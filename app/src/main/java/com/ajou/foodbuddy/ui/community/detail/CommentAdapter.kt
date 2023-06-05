package com.ajou.foodbuddy.ui.community.detail

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ajou.foodbuddy.data.firebase.model.community.CommentInfo
import com.ajou.foodbuddy.databinding.ItemCommentBinding
import com.ajou.foodbuddy.databinding.ItemMenuBinding
import com.bumptech.glide.Glide

class CommentAdapter:ListAdapter<CommentInfo, CommentAdapter.ViewHolder>(diffUtil) {

    inner class ViewHolder(private val binding: ItemCommentBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(commentModel: CommentInfo) {
            binding.usernameTextView.text = commentModel.userId // 이름
            binding.commentTextView.text = commentModel.userComment // 코멘트
            binding.uploadTextView.text = commentModel.uploadTime
            Glide.with(binding.root.context).load(commentModel.profileImage).into(binding.userProfileImageView) //프로필
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemCommentBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: CommentAdapter.ViewHolder, position: Int) {
        holder.bind(currentList[position])
    }
    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<CommentInfo>() {
            override fun areItemsTheSame(oldItem: CommentInfo, newItem: CommentInfo): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: CommentInfo, newItem: CommentInfo): Boolean {
                return oldItem.userId== newItem.userId
            }

        }
    }
}