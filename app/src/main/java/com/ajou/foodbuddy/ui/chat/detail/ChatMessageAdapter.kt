package com.ajou.foodbuddy.ui.chat.detail

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.get
import androidx.core.view.marginTop
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ajou.foodbuddy.data.firebase.model.chat.ProcessedChatMessageItem
import com.ajou.foodbuddy.data.firebase.path.Key
import com.ajou.foodbuddy.databinding.ItemChatroomDetailMyMessageBinding
import com.ajou.foodbuddy.databinding.ItemChatroomDetailMySharingBinding
import com.ajou.foodbuddy.databinding.ItemChatroomDetailOpponentMessageBinding
import com.ajou.foodbuddy.databinding.ItemChatroomDetailOpponentSharingBinding
import com.ajou.foodbuddy.extensions.convertBase64ToStr
import com.ajou.foodbuddy.extensions.convertStrToBase64
import com.bumptech.glide.Glide
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class ChatMessageAdapter(
    private val sharingItemClickListener: (ProcessedChatMessageItem) -> Unit,
) :
    ListAdapter<ProcessedChatMessageItem, RecyclerView.ViewHolder>(diffUtil) {

    inner class MyViewHolder(private val binding: ItemChatroomDetailMyMessageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ProcessedChatMessageItem) {
            binding.messageTextView.text = item.messageContent.toString()
        }
    }

    inner class MySharingViewHolder(private val binding: ItemChatroomDetailMySharingBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ProcessedChatMessageItem) {
            binding.sharingTitleTextView.text = item.messageContent

            binding.navigateSharingLayout.setOnClickListener {
                sharingItemClickListener(item)
            }
        }
    }

    inner class OpponentViewHolder(private val binding: ItemChatroomDetailOpponentMessageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ProcessedChatMessageItem) {
            val index = currentList.indexOf(item)

            if (index == 0) {
                binding.profileImageView.visibility = View.VISIBLE
                binding.nicknameTextView.visibility = View.VISIBLE
                Glide
                    .with(binding.root)
                    .load(item.profileImageUrl)
                    .into(binding.profileImageView)
            } else {
                val beforeItem = currentList[index - 1]
                if (beforeItem.nickname == item.nickname) {
                    binding.profileImageView.visibility = View.GONE
                    binding.nicknameTextView.visibility = View.GONE
                } else {
                    Glide
                        .with(binding.root)
                        .load(item.profileImageUrl)
                        .into(binding.profileImageView)

                    binding.profileImageView.visibility = View.VISIBLE
                    binding.nicknameTextView.visibility = View.VISIBLE
                }
            }

            binding.nicknameTextView.text = item.nickname
            binding.messageTextView.text = item.messageContent
        }
    }

    inner class OpponentSharingViewHolder(private val binding: ItemChatroomDetailOpponentSharingBinding) :
        RecyclerView.ViewHolder(binding.root) {

            fun bind(item: ProcessedChatMessageItem) {
                val index = currentList.indexOf(item)

                if (index == 0) {
                    binding.profileImageView.visibility = View.VISIBLE
                    Glide
                        .with(binding.root)
                        .load(item.profileImageUrl)
                        .into(binding.profileImageView)
                } else {
                    val beforeItem = currentList[index - 1]
                    if (beforeItem.nickname == item.nickname) {
                        binding.profileImageView.visibility = View.GONE
                        binding.nicknameTextView.visibility = View.GONE
                    } else {
                        Glide
                            .with(binding.root)
                            .load(item.profileImageUrl)
                            .into(binding.profileImageView)
                        binding.profileImageView.visibility = View.VISIBLE
                        binding.nicknameTextView.visibility = View.VISIBLE
                        binding.nicknameTextView.text = item.nickname
                    }
                }

                binding.sharingTitleTextView.text = item.messageContent

                binding.navigateSharingLayout.setOnClickListener {
                    sharingItemClickListener(item)
                }
            }
    }

    override fun getItemViewType(position: Int): Int {
        val item = currentList[position]

        return if (item.userId == Firebase.auth.currentUser!!.email.toString()) {
            if (item.messageType == Key.SHARING_NORMAL) {
                MY
            } else {
                MY_SHARING
            }
        } else {
            if (item.messageType == Key.SHARING_NORMAL) {
                OPPONENT
            } else {
                OPPONENT_SHARING
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        val inflater =
            parent.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        return when (viewType) {
            MY -> {
                val binding = ItemChatroomDetailMyMessageBinding.inflate(inflater, parent, false)
                MyViewHolder(binding)
            }
            OPPONENT -> {
                val binding =
                    ItemChatroomDetailOpponentMessageBinding.inflate(inflater, parent, false)
                OpponentViewHolder(binding)
            }
            MY_SHARING -> {
                val binding = ItemChatroomDetailMySharingBinding.inflate(inflater, parent, false)
                MySharingViewHolder(binding)
            }
            else -> {
                val binding = ItemChatroomDetailOpponentSharingBinding.inflate(inflater, parent, false)
                OpponentSharingViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is MyViewHolder -> {
                holder.bind(currentList[position])
            }
            is OpponentViewHolder -> {
                holder.bind(currentList[position])
            }
            is MySharingViewHolder -> {
                holder.bind(currentList[position])
            }
            is OpponentSharingViewHolder -> {
                holder.bind(currentList[position])
            }
        }
    }

    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<ProcessedChatMessageItem>() {
            override fun areItemsTheSame(
                oldItem: ProcessedChatMessageItem,
                newItem: ProcessedChatMessageItem
            ): Boolean {
                return oldItem == newItem
            }

            @SuppressLint("DiffUtilEquals")
            override fun areContentsTheSame(
                oldItem: ProcessedChatMessageItem,
                newItem: ProcessedChatMessageItem
            ): Boolean {
                return oldItem.uploadTime == newItem.uploadTime
            }
        }

        const val MY = 0
        const val OPPONENT = 1
        const val MY_SHARING = 2
        const val OPPONENT_SHARING = 3
    }

}