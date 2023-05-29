package com.ajou.foodbuddy.ui.profile.search

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ajou.foodbuddy.data.firebase.model.FindFriendInfo
import com.ajou.foodbuddy.data.firebase.model.UserInfo
import com.ajou.foodbuddy.databinding.ItemUserBinding
import com.ajou.foodbuddy.ui.profile.main.ProfileMainFriendActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class FriendDeleteAdapter  : ListAdapter<FindFriendInfo, FriendDeleteAdapter.ViewHolder>(diffUtil) {

    inner class ViewHolder(private val binding: ItemUserBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(friendModel: FindFriendInfo) {
            binding.friendDetailTextView.text = friendModel.nickname
            binding.root.setOnClickListener{

                val intent = Intent(binding.root.context, ProfileMainFriendActivity::class.java)
                intent.putExtra("UserName",friendModel.UserId)
                intent.putExtra("delete","delete")
                binding.root.context.startActivity(intent)


            }
        }
    }
    fun clearData() {
        submitList(emptyList())
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemUserBinding.inflate(LayoutInflater.from(parent.context), parent, false))




    override fun onBindViewHolder(holder: FriendDeleteAdapter.ViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<FindFriendInfo>() {
            override fun areItemsTheSame(oldItem: FindFriendInfo, newItem: FindFriendInfo): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: FindFriendInfo, newItem: FindFriendInfo): Boolean {
                return oldItem.nickname == newItem.nickname
            }

        }
    }
}
