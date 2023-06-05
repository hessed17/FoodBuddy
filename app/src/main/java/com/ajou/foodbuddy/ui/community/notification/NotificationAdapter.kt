package com.ajou.foodbuddy.ui.community.notification

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ajou.foodbuddy.data.firebase.model.notification.NotificationInfo
import com.ajou.foodbuddy.databinding.ItemNotificationBinding
import com.ajou.foodbuddy.ui.community.detail.CommunityReviewDetailActivity
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class NotificationAdapter: ListAdapter<NotificationInfo, NotificationAdapter.ViewHolder>(diffUtil) {

    inner class ViewHolder(private val binding: ItemNotificationBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(notificationModel: NotificationInfo) {
            binding.notifyContentTextView.text = notificationModel.nickname+"님이 "+notificationModel.reviewTitle+"에 "+notificationModel.message+
                    "를 남기셨습니다."
            Glide.with(binding.root.context).load(notificationModel.profileImage).into(binding.notifyProfileImageView)
            binding.root.setOnClickListener {
                val notificationInfo = FirebaseDatabase.getInstance().reference.child("NotificationInfo")
                notificationInfo.addListenerForSingleValueEvent(object :ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for(remove in snapshot.children){
                            if(remove.child("reviewId").value.toString()==notificationModel.reviewId && remove.child("uploadTime").value.toString()==notificationModel.uploadTime.toString()){
                                remove.ref.removeValue()
                                break
                            }
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {



                    }
                })
                val intent = Intent(binding.root.context, CommunityReviewDetailActivity::class.java)
                intent.putExtra("reviewId",notificationModel.reviewId)
                binding.root.context.startActivity(intent)
            }
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemNotificationBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: NotificationAdapter.ViewHolder, position: Int) {
        holder.bind(currentList[position])
    }
    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<NotificationInfo>() {
            override fun areItemsTheSame(oldItem: NotificationInfo, newItem: NotificationInfo): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: NotificationInfo, newItem: NotificationInfo): Boolean {
                return oldItem.message== newItem.message
            }

        }
    }
}