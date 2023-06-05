package com.ajou.foodbuddy.ui.chat.sharing.friend

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.ajou.foodbuddy.data.firebase.model.profile.ChatUserInfo
import com.ajou.foodbuddy.databinding.FragmentTablayoutFriendBinding
import com.ajou.foodbuddy.ui.chat.sharing.SharingViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SharingFriendListFragment : Fragment() {

    private var _binding: FragmentTablayoutFriendBinding? = null
    private val binding get() = _binding!!
    private val sharingViewModel: SharingViewModel by activityViewModels()
    private lateinit var friendAdapter: FriendAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTablayoutFriendBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initSearchEditTextView()
        initRecyclerView()
    }

    private fun initSearchEditTextView() {

    }

    private fun initRecyclerView() {
        friendAdapter = FriendAdapter {

        }
        _binding?.recyclerView?.apply {
            layoutManager = LinearLayoutManager(requireActivity())
            adapter = friendAdapter
        }

        sharingViewModel.getStaticUserList()

        sharingViewModel.queriedFriendList.observe(requireActivity()) {
            when (it) {
                is FriendListUiState.Uninitialized -> {}
                is FriendListUiState.SuccessGetUserList -> bindUserList(it.userList)
            }
        }
    }

    private fun bindUserList(chatRoomList: List<ChatUserInfo>) {
        friendAdapter.submitList(chatRoomList)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    fun getSelectedFriend() = friendAdapter.currentList[friendAdapter.selectedPosition].userId
}