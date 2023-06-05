package com.ajou.foodbuddy.ui.chat.sharing.friend

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ajou.foodbuddy.data.firebase.model.profile.ChatUserInfo
import com.ajou.foodbuddy.databinding.ItemInviteUserBinding

class FriendAdapter(
    private val itemClickListener: (ChatUserInfo) -> Unit
) :
    ListAdapter<ChatUserInfo, FriendAdapter.ViewHolder>(diffUtil) {

    var selectedPosition = RecyclerView.NO_POSITION

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendAdapter.ViewHolder =
        ViewHolder(
            ItemInviteUserBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: FriendAdapter.ViewHolder, position: Int) {
        holder.bind(currentList[position], position) { clickedPosition ->
            if (selectedPosition != clickedPosition) {
                selectedPosition = clickedPosition
                notifyDataSetChanged()
            }
        }

    }

    inner class ViewHolder(private val binding: ItemInviteUserBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ChatUserInfo, position: Int, itemClicked: (Int) -> Unit) {
            binding.nicknameTextView.text = item.nickname
            binding.radioButton.isChecked = selectedPosition == position

            binding.root.setOnClickListener {
                itemClicked(position)
            }
        }
    }

    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<ChatUserInfo>() {
            override fun areItemsTheSame(
                oldItem: ChatUserInfo,
                newItem: ChatUserInfo
            ): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(
                oldItem: ChatUserInfo,
                newItem: ChatUserInfo
            ): Boolean {

                return oldItem.userId == newItem.userId
            }

        }
    }

}