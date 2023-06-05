package com.ajou.foodbuddy.ui.restaurant.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SearchView
import androidx.core.net.toUri
import com.ajou.foodbuddy.BaseFragment
import com.ajou.foodbuddy.R
import com.ajou.foodbuddy.data.firebase.model.*
import com.ajou.foodbuddy.data.firebase.model.restaurant.FirstProcessedRestaurantItem
import com.ajou.foodbuddy.data.firebase.model.restaurant.RestaurantItem
import com.ajou.foodbuddy.data.firebase.model.restaurant.SecondProcessedRestaurantItem
import com.ajou.foodbuddy.data.firebase.path.Key.RESTAURANT
import com.ajou.foodbuddy.data.firebase.path.Key.RESTAURANT_INFO
import com.ajou.foodbuddy.databinding.FragmentRestaurantMainBinding
import com.ajou.foodbuddy.ui.restaurant.detail.RestaurantDetailActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
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
    private lateinit var listAdapter: ArrayAdapter<String>
    private val dataList =ArrayList<String>()
    private var filteredData =ArrayList<String>()
    var allNumber=0
    var kNumber=0
    var jNumber=0
    var cNumber=0
    var wNumber=0
    var reviewSelect=0
    var ratingSelect=0
    override fun getViewBinding(): FragmentRestaurantMainBinding =
        FragmentRestaurantMainBinding.inflate(layoutInflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initCategoryButton()
        initSpinner()
        initAdapter()
        bindRestaurantInfo()

        //가게명 입력시 해당 가게명 보여주기
        restaurantEdit()
        setTRestaurantList()
    }
    private fun initCategoryButton() {
        initKoreanCategoryButton()
        initJapaneseCategoryButton()
        initChineseCategoryButton()
        initWesternCategoryButton()
        initAllCategoryButton()
    }

    private fun initAllCategoryButton(){
        _binding?.allFoodButton?.setOnClickListener {
            firstProcessedItemList.clear()
            bindRestaurantInfo()
            allNumber=1
            kNumber=0
            jNumber=0
            cNumber=0
            wNumber=0

        }

    }
    private fun initKoreanCategoryButton() {
        _binding?.koreanFoodButton?.setOnClickListener {
            firstProcessedItemList.clear()
            bindRestaurantInfo("한식")
            allNumber=0
            kNumber=1
            jNumber=0
            cNumber=0
            wNumber=0
        }
    }

    private fun initJapaneseCategoryButton() {
        _binding?.japaneseFoodButton?.setOnClickListener {
            firstProcessedItemList.clear()
            bindRestaurantInfo("일식")
            allNumber=0
            kNumber=0
            jNumber=1
            cNumber=0
            wNumber=0
        }
    }

    private fun initChineseCategoryButton() {
        _binding?.chineseFoodButton?.setOnClickListener {
            firstProcessedItemList.clear()
            bindRestaurantInfo("중식")
            allNumber=0
            kNumber=0
            jNumber=0
            cNumber=1
            wNumber=0
        }
    }

    private fun initWesternCategoryButton() {
        _binding?.westernFoodButton?.setOnClickListener {
            firstProcessedItemList.clear()
            bindRestaurantInfo("양식")
            allNumber=0
            kNumber=0
            jNumber=0
            cNumber=0
            wNumber=1
        }
    }

    private fun initSpinner() {
        _binding?.restaurantArraySpinner?.adapter = _binding?.root?.context?.let {
            ArrayAdapter.createFromResource(
                it, R.array.spinner_main_restaurant, R.layout.spin_color
            )
        }
        _binding?.restaurantArraySpinner?.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {

                }

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long,
                ) {
//                    firstProcessedItemList.clear()
                    when (position) {
                        //평점순
                        1->{
                            reviewSelect=0
                            ratingSelect=1
                            firstProcessedItemList.clear()
                            spinnerBindRestaurantInfo(allNumber,kNumber,jNumber,cNumber,wNumber)

                        }
                        //리뷰순
                        2->{
                            reviewSelect=1
                            ratingSelect=0
                            firstProcessedItemList.clear()
                            spinnerBindRestaurantInfo(allNumber,kNumber,jNumber,cNumber,wNumber)
                        }

                    }
                }
            }
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

    private fun spinnerBindRestaurantInfo(all:Int,k:Int,j:Int,c:Int,w:Int){
        val restaurantDB = database.child(RESTAURANT_INFO).limitToFirst(10)
        if(all==1){
            restaurantDB.orderByChild("categoryId").equalTo(null).addValueEventListener(valueEventListener)
        }else if(k==1){
            restaurantDB.orderByChild("categoryId").equalTo("한식").addValueEventListener(valueEventListener)
        }else if(j==1){
            restaurantDB.orderByChild("categoryId").equalTo("일식").addValueEventListener(valueEventListener)
        }else if(c==1){
            restaurantDB.orderByChild("categoryId").equalTo("중식").addValueEventListener(valueEventListener)
        }else{
            restaurantDB.orderByChild("categoryId").equalTo("양식").addValueEventListener(valueEventListener)
        }


    }
    private fun bindRestaurantInfo(categoryNumber: String? = null) {
        firstProcessedItemList.clear()
        val restaurantDB = database.child(RESTAURANT_INFO).limitToFirst(10)
        if (categoryNumber.isNullOrBlank()) {
            allNumber=1
            kNumber=0
            jNumber=0
            cNumber=0
            wNumber=0
            restaurantDB.addValueEventListener(valueEventListener)
        } else {
            restaurantDB.orderByChild("categoryId").equalTo(categoryNumber).addValueEventListener(valueEventListener)
        }
    }

    private fun showProgressbar() {
        _binding?.progressbar?.visibility = View.VISIBLE
    }

    private fun hideProgressbar() {
        _binding?.progressbar?.visibility = View.INVISIBLE
    }

    private fun processModelAndSubmitListToAdapter(resName:String?=null) {

        launch {
            showProgressbar()
            Log.d("yoosusang1",dataList.size.toString())
            var totalRatingNumber=0f
            var totalReviewCount=0
            val secondProcessedItemList = mutableListOf<SecondProcessedRestaurantItem>()
            val reviewInfo = FirebaseDatabase.getInstance().reference.child("ReviewInfo")
            if(resName.isNullOrEmpty()){
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
                    reviewInfo.addListenerForSingleValueEvent(object:ValueEventListener{
                        override fun onDataChange(snapshot: DataSnapshot) {
                            for(child in snapshot.children){
                                if(item.restaurantName==child.child("restaurantName").value.toString()){
                                    totalRatingNumber+=child.child("reviewRating").getValue(Float::class.java)!!
                                        .toFloat()
                                    totalReviewCount++
                                }
                            }
                            if(totalReviewCount!=0)
                                totalRatingNumber/=totalReviewCount
                            secondProcessedItemList.add(item.convertToSecondProcessedRestaurantItem(thumbnailImageUri,totalRatingNumber,totalReviewCount))
                            Log.d("totalRatingNumber", totalRatingNumber.toString())
                            Log.d("totalReviewCount", totalReviewCount.toString())
                            totalRatingNumber=0f
                            totalReviewCount=0
                        }
                        override fun onCancelled(error: DatabaseError) {

                        }
                    })
                }
                if(reviewSelect==1){
                    secondProcessedItemList.sortByDescending { it.reviewNumber }
                }else if(ratingSelect==1){
                    secondProcessedItemList.sortByDescending {it.ratingNumber }
                }else{
                }
                adapter.submitList(secondProcessedItemList)
            }else{
                for (item in firstProcessedItemList) {
                    if(item.restaurantName==resName.toString()){
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
                        reviewInfo.addListenerForSingleValueEvent(object:ValueEventListener{
                            override fun onDataChange(snapshot: DataSnapshot) {
                                for(child in snapshot.children){
                                    if(item.restaurantName==child.child("restaurantName").value.toString()){
                                        totalRatingNumber+=child.child("reviewRating").getValue(Float::class.java)!!
                                            .toFloat()
                                        totalReviewCount++
                                    }
                                }
                                if(totalReviewCount!=0)
                                    totalRatingNumber/=totalReviewCount
                                if(secondProcessedItemList.size==0){
                                    secondProcessedItemList.add(item.convertToSecondProcessedRestaurantItem(thumbnailImageUri,totalRatingNumber,totalReviewCount))
                                    Log.d("yoosusang",secondProcessedItemList.size.toString())
                                    totalRatingNumber=0f
                                    totalReviewCount=0
                                    adapter.submitList(secondProcessedItemList)
                                }
                            }
                            override fun onCancelled(error: DatabaseError) {

                            }
                        })
                    }

                }
            }
            hideProgressbar()
        }
    }
    private fun restaurantEdit() {
        listAdapter = _binding?.root?.context?.let { ArrayAdapter(it, android.R.layout.simple_list_item_1, filteredData) }!!
        _binding?.listView?.adapter = listAdapter

        _binding?.restaurantSearchName?.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                _binding?.restaurantSearchName?.setQuery("",false)
                _binding?.listView?.visibility= View.GONE
                processModelAndSubmitListToAdapter(query)
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                filterData(newText)
                return true
            }
        })
    }

    private fun filterData(query: String) {
        filteredData.clear()
        if(query!="")
            filteredData.addAll(dataList.filter { it.contains(query, ignoreCase = true) })
        listAdapter.notifyDataSetChanged()
        Log.d("yoosusang1",filteredData.toString())
        if (filteredData.isNotEmpty()) {
            _binding?.listView?.visibility = View.VISIBLE
        } else {
            _binding?.listView?.visibility = View.GONE
        }
    }
    private fun setTRestaurantList(){
        dataList.clear()
        val restaurantInfos = FirebaseDatabase.getInstance().reference.child("RestaurantInfo").addListenerForSingleValueEvent(object:ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for(shas in snapshot.children){
                    dataList.add(shas.key.toString())
                }

            }
            override fun onCancelled(error: DatabaseError) {

            }

        })
    }
}