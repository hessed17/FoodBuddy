package com.ajou.foodbuddy.ui.chat.sharing

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.ajou.foodbuddy.data.firebase.path.Key
import com.ajou.foodbuddy.databinding.ActivitySharingRestaurantBinding
import com.ajou.foodbuddy.ui.chat.sharing.chatroom.SharingChatRoomListFragment
import com.ajou.foodbuddy.ui.chat.sharing.friend.SharingFriendListFragment
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SharingRestaurantToChatRoomActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySharingRestaurantBinding
    private var selectedTabPosition = 0
    private val chatRoomFragment = SharingChatRoomListFragment()
    private val friendFragment = SharingFriendListFragment()
    private val sharingViewModel: SharingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySharingRestaurantBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViewPager()
        initConfirmButton()
    }

    private fun initViewPager() {
        val viewPagerAdapter = ViewPagerAdapter(this)

        viewPagerAdapter.addFragment(chatRoomFragment)
        viewPagerAdapter.addFragment(friendFragment)

        binding.viewPager.apply {
            adapter = viewPagerAdapter

            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    selectedTabPosition = position
                }
            })
        }

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = "채팅방 목록"
                1 -> tab.text = "친구 목록"
            }
        }.attach()
    }

    private fun initConfirmButton() {
        binding.confirmButton.setOnClickListener {
            val sharingType = intent.getStringExtra(Key.SHARING_TYPE).toString()
            val sharingTitle = intent.getStringExtra(Key.SHARING_TITLE).toString()
            val sharingId = intent.getStringExtra(Key.SHARING_ID).toString()

            when (selectedTabPosition) {
                0 -> {
                    val chatRoomId = chatRoomFragment.getSelectedChatRoom()
                    sharingViewModel.share(chatRoomId, sharingType, sharingId, sharingTitle)
                    Toast.makeText(this, "채팅방에 공유했습니다.", Toast.LENGTH_SHORT).show()
                    finish()
                }

                1 -> {
                    val userId = friendFragment.getSelectedFriend()
                    finish()
                }
            }
        }
    }
}