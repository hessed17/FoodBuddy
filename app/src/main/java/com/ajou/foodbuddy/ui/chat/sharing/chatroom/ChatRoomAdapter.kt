package com.ajou.foodbuddy.ui.chat.sharing.chatroom

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil.ItemCallback
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ajou.foodbuddy.databinding.ItemInviteChatRoomBinding

class ChatRoomAdapter :
    ListAdapter<InviteChatRoomItem, ChatRoomAdapter.ViewHolder>(diffUtil) {

    var selectedPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatRoomAdapter.ViewHolder =
        ViewHolder(
            ItemInviteChatRoomBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(currentList[position], position) { clickedPosition ->
            if (selectedPosition != clickedPosition) {
                selectedPosition = clickedPosition
                notifyDataSetChanged()
            }
        }
    }

    inner class ViewHolder(private val binding: ItemInviteChatRoomBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: InviteChatRoomItem, position: Int, itemClicked: (Int) -> Unit) {
            binding.chatRoomTitleTextView.text = item.title
            binding.chatRoomContentTextView.text = item.lastMessage
            binding.radioButton.isChecked = selectedPosition == position

            binding.root.setOnClickListener {
                itemClicked(position)
            }
        }
    }

    companion object {
        val diffUtil = object : ItemCallback<InviteChatRoomItem>() {
            override fun areItemsTheSame(
                oldItem: InviteChatRoomItem,
                newItem: InviteChatRoomItem
            ): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(
                oldItem: InviteChatRoomItem,
                newItem: InviteChatRoomItem
            ): Boolean {

                return oldItem.chatRoomId == newItem.chatRoomId
            }

        }
    }

}

data class InviteChatRoomItem(
    val chatRoomId: String,
    val title: String? = null,
    val lastMessage: String? = null
) {
//    fun toInviteChatRoomItem(chatRoomId: String) =
//        InviteChatRoomItem(
//            chatRoomId = chatRoomId,
//            title = this.title!!,
//            lastMessage = this.lastMessage!!
//        )
}

//data class InviteChatRoomItem(
//    val chatRoomId: String,
//    val title: String,
//    val lastMessage: String
//)