package com.ajou.foodbuddy.ui.restaurant.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.net.toUri
import com.ajou.foodbuddy.BaseFragment
import com.ajou.foodbuddy.data.firebase.model.*
import com.ajou.foodbuddy.data.firebase.path.Key.CATEGORY_CHINESE
import com.ajou.foodbuddy.data.firebase.path.Key.CATEGORY_JAPANESES
import com.ajou.foodbuddy.data.firebase.path.Key.CATEGORY_KOREAN
import com.ajou.foodbuddy.data.firebase.path.Key.CATEGORY_WESTERN
import com.ajou.foodbuddy.data.firebase.path.Key.RESTAURANT
import com.ajou.foodbuddy.data.firebase.path.Key.RESTAURANT_INFO
import com.ajou.foodbuddy.databinding.FragmentRestaurantMainBinding
import com.ajou.foodbuddy.ui.restaurant.detail.RestaurantDetailActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await

class RestaurantMainFragment : BaseFragment<FragmentRestaurantMainBinding>(),
    CoroutineScope by MainScope() {

    private val storageRef = Firebase.storage.reference
    private val database = Firebase.database.reference
    private lateinit var adapter: RestaurantAdapter

    override fun getViewBinding(): FragmentRestaurantMainBinding =
        FragmentRestaurantMainBinding.inflate(layoutInflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initCategoryButton()
        initSpinner()
        initAdapter()
        bindRestaurantInfo()
    }

    private fun initCategoryButton() {
        initKoreanCategoryButton()
        initJapaneseCategoryButton()
        initChineseCategoryButton()
        initWesternCategoryButton()
    }

    private fun initKoreanCategoryButton() {
        _binding?.koreanFoodButton?.setOnClickListener {
            firstProcessedItemList.clear()
            bindRestaurantInfo(CATEGORY_KOREAN)
        }
    }

    private fun initJapaneseCategoryButton() {
        _binding?.japaneseFoodButton?.setOnClickListener {
            firstProcessedItemList.clear()
            bindRestaurantInfo(CATEGORY_JAPANESES)
        }
    }

    private fun initChineseCategoryButton() {
        _binding?.chineseFoodButton?.setOnClickListener {
            firstProcessedItemList.clear()
            bindRestaurantInfo(CATEGORY_CHINESE)
        }
    }

    private fun initWesternCategoryButton() {
        _binding?.westernFoodButton?.setOnClickListener {
            firstProcessedItemList.clear()
            bindRestaurantInfo(CATEGORY_WESTERN)
        }
    }

    private fun initSpinner() {

    }

    private fun initAdapter() {
        adapter = RestaurantAdapter { item ->
            startActivity(Intent(requireActivity(), RestaurantDetailActivity::class.java).apply {
                putExtra(RestaurantDetailActivity.RESTAURANT_NAME, item.restaurantName)
            })
        }
        _binding?.restaurantRecyclerView?.adapter = adapter
    }

    private val firstProcessedItemList = mutableListOf<FirstProcessedRestaurantItem>()

    private val valueEventListener = object: ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            for (dataSnapshot in snapshot.children) {
                val restaurantName = dataSnapshot.key.toString()
                val restaurantItem = dataSnapshot.getValue(RestaurantItem::class.java) as RestaurantItem
                val firstProcessedItem = restaurantItem.convertToFirstProcessedRestaurantItem(restaurantName)
                firstProcessedItemList.add(firstProcessedItem)
            }
            processModelAndSubmitListToAdapter()
        }
        override fun onCancelled(error: DatabaseError) {}

    }

    private fun bindRestaurantInfo(categoryNumber: String? = null) {
        if (categoryNumber.isNullOrBlank()) {
            database.child(RESTAURANT_INFO).addValueEventListener(valueEventListener)
        } else {
            database.child(RESTAURANT_INFO).orderByChild("categoryId").equalTo(categoryNumber).addValueEventListener(valueEventListener)
        }
    }

    private fun processModelAndSubmitListToAdapter() {
        launch {
            val secondProcessedItemList = mutableListOf<SecondProcessedRestaurantItem>()
            for (item in firstProcessedItemList) {
                val thumbnailImageUri =
                    withContext(Dispatchers.IO) {
                        try {
                            storageRef.child(RESTAURANT).child(item.restaurantName).child("image")
                                .child("thumbnail.jpg").downloadUrl.await().toString().toUri()
                        } catch (_: java.lang.Exception) {
                            Log.d("RestaurantName", item.restaurantName)
                            null
                        }
                    }
                secondProcessedItemList.add(item.convertToSecondProcessedRestaurantItem(thumbnailImageUri))
            }
            adapter.submitList(secondProcessedItemList)
        }
    }
}