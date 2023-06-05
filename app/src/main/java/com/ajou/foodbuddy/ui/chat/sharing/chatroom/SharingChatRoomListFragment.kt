package com.ajou.foodbuddy.ui.chat.sharing.chatroom

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.ajou.foodbuddy.databinding.FragmentTablayoutChatroomBinding
import com.ajou.foodbuddy.ui.chat.sharing.SharingViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SharingChatRoomListFragment : Fragment() {

    private var _binding: FragmentTablayoutChatroomBinding? = null
    private val binding get() = _binding!!
    private lateinit var chatRoomAdapter: ChatRoomAdapter
    private val sharingViewModel: SharingViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTablayoutChatroomBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initRecyclerView()
    }

    private fun initRecyclerView() {
        chatRoomAdapter = ChatRoomAdapter()
        _binding?.recyclerView?.apply {
            layoutManager = LinearLayoutManager(requireActivity())
            adapter = chatRoomAdapter
        }

        sharingViewModel.getStaticChatRoomList()

        sharingViewModel.queriedChatRoomList.observe(requireActivity()) {
            when (it) {
                is ChatRoomListUiState.Uninitialized -> {}
                is ChatRoomListUiState.SuccessGetChatRoomList -> bindChatRoomList(it.chatRoomList)
            }
        }
    }

    private fun bindChatRoomList(chatRoomList: List<InviteChatRoomItem>) {
        chatRoomAdapter.submitList(chatRoomList)
    }

    fun getSelectedChatRoom() =
        chatRoomAdapter.currentList[chatRoomAdapter.selectedPosition].chatRoomId
}