package com.ajou.foodbuddy.ui.profile.search

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ajou.foodbuddy.data.firebase.model.FindFriendInfo
import com.ajou.foodbuddy.data.firebase.model.UserInfo
import com.ajou.foodbuddy.databinding.ItemUserBinding
import com.ajou.foodbuddy.ui.profile.main.ProfileMainFriendActivity

class FriendAddAdapter  : ListAdapter<FindFriendInfo, FriendAddAdapter.ViewHolder>(diffUtil) {

    inner class ViewHolder(private val binding: ItemUserBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(friendModel: FindFriendInfo) {
            binding.friendDetailTextView.text = friendModel.nickname
            binding.root.setOnClickListener{
                //local에서 부르기 Add intent을 보낸 이유는 button마다 작동하는 방식이 다르기 때문에
                val intent = Intent(binding.root.context, ProfileMainFriendActivity::class.java)
                Log.d("userId aaaa", friendModel.UserId)
                intent.putExtra("UserId2",friendModel.UserId)


                intent.putExtra("add","add")
                binding.root.context.startActivity(intent)
            }
        }
    }
    fun clearData() {
        submitList(emptyList())
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemUserBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: FriendAddAdapter.ViewHolder, position: Int) {
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
