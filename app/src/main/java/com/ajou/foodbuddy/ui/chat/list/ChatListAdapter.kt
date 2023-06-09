package com.ajou.foodbuddy.ui.chat.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil.ItemCallback
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ajou.foodbuddy.data.firebase.model.chat.ProcessedChatRoomItem
import com.ajou.foodbuddy.databinding.ItemChatRoomBinding
import com.ajou.foodbuddy.extensions.convertTimeStampToDate


class ChatListAdapter(
    private val chatRoomClickListener: (ProcessedChatRoomItem) -> Unit
): ListAdapter<ProcessedChatRoomItem, ChatListAdapter.ViewHolder>(diffUtil) {

    inner class ViewHolder(private val binding: ItemChatRoomBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ProcessedChatRoomItem) {
            binding.apply {
                chatRoomTitleTextView.text = item.title
                chatRoomContentTextView.text = item.lastMessageContent
                chatLastMessageTimeTextView.text = item.lastUploadTime.toString().convertTimeStampToDate()

                root.setOnClickListener {
                    chatRoomClickListener(item)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatListAdapter.ViewHolder =
        ViewHolder(ItemChatRoomBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    companion object {
        val diffUtil = object: ItemCallback<ProcessedChatRoomItem>() {
            override fun areItemsTheSame(oldItem: ProcessedChatRoomItem, newItem: ProcessedChatRoomItem): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: ProcessedChatRoomItem, newItem: ProcessedChatRoomItem): Boolean {
                return oldItem.chatRoomId == newItem.chatRoomId
            }

        }
    }

}