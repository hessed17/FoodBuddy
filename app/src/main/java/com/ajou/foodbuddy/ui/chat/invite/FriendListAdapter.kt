package com.ajou.foodbuddy.ui.chat.invite

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ajou.foodbuddy.data.firebase.model.ChatUserInfo
import com.ajou.foodbuddy.databinding.ItemInviteUserBinding

class FriendListAdapter(): ListAdapter<ChatUserInfo, FriendListAdapter.ViewHolder>(diffUtil) {

    private val selectedFriends = mutableListOf<ChatUserInfo>()

    fun getSelectedFriendList() = selectedFriends.toList()

    inner class ViewHolder(private val binding: ItemInviteUserBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ChatUserInfo) {
            with(binding) {
                nicknameTextView.text = item.nickname

                root.setOnClickListener {
                    if (!checkBox.isChecked) {
                        selectedFriends.add(item)

                    } else {
                        selectedFriends.remove(item)
                    }
                    checkBox.isChecked = !checkBox.isChecked
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendListAdapter.ViewHolder =
        ViewHolder(ItemInviteUserBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    companion object {
        val diffUtil = object: DiffUtil.ItemCallback<ChatUserInfo>() {
            override fun areItemsTheSame(oldItem: ChatUserInfo, newItem: ChatUserInfo): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: ChatUserInfo, newItem: ChatUserInfo): Boolean {
                return oldItem.userId == newItem.userId
            }

        }
    }

}