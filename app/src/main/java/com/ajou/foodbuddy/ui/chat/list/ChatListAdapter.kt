package com.ajou.foodbuddy.ui.chat.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil.ItemCallback
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ajou.foodbuddy.data.firebase.model.ProcessedChatItem
import com.ajou.foodbuddy.databinding.ItemChatRoomBinding


class ChatListAdapter(
    private val chatRoomClickListener: (ProcessedChatItem) -> Unit
): ListAdapter<ProcessedChatItem, ChatListAdapter.ViewHolder>(diffUtil) {

    inner class ViewHolder(private val binding: ItemChatRoomBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ProcessedChatItem) {
            binding.apply {
                chatRoomTitleTextView.text = item.title
                chatRoomContentTextView.text = item.lastMessageContent
                chatLastMessageTimeTextView.text = item.lastUploadTime.toString()

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
        val diffUtil = object: ItemCallback<ProcessedChatItem>() {
            override fun areItemsTheSame(oldItem: ProcessedChatItem, newItem: ProcessedChatItem): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: ProcessedChatItem, newItem: ProcessedChatItem): Boolean {
                return oldItem.chatRoomId == newItem.chatRoomId
            }

        }
    }

}