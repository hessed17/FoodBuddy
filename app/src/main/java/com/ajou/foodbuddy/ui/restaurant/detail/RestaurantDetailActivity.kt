package com.ajou.foodbuddy.ui.restaurant.detail

import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.ajou.foodbuddy.R
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.ajou.foodbuddy.data.firebase.model.ProcessedMenuInfo
import com.ajou.foodbuddy.data.firebase.model.ProcessedReviewInfo
import com.ajou.foodbuddy.databinding.ActivityRestaurantDetailBinding
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ListResult
import com.google.firebase.storage.StorageReference

class RestaurantDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRestaurantDetailBinding
    private lateinit var menuadapter: MenuAdapter
    private lateinit var reviewadapater: ReviewAdapter
    var database: DatabaseReference = Firebase.database.reference
    private lateinit var restaurantName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRestaurantDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initRestaurantName()
        //reviewAdapter 초기화
        initReviewAdapters()
        //menuAdapter 초기화
        initMenuAdapters()

        //이미지업로드
        setMainIamge()
        setMenuList()

        //스피너 적용
        initSpinner()

        //해당 버튼 클릭시 뷰 변경
        binding.reviewButton.setOnClickListener {
            reviewButtonSelect()

        }
        binding.menuButton.setOnClickListener {
            menuButtonSelect()
        }
    }

    private fun initRestaurantName() {
        restaurantName = intent.getStringExtra(RESTAURANT_NAME).toString()
    }

    //어뎁터 및 리사이클러뷰 설정
    private fun initReviewAdapters() {
        reviewadapater = ReviewAdapter()
        binding.reviewListRecyclerView.adapter = reviewadapater
        binding.reviewListRecyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
    }

    private fun initMenuAdapters() {
        menuadapter = MenuAdapter()
        binding.menuListRecyclerView.adapter = menuadapter
        binding.menuListRecyclerView.layoutManager = GridLayoutManager(this, 2)
    }

    //버튼 클릭시 이벤트
    private fun reviewButtonSelect() {
        binding.reviewListRecyclerView.visibility = View.VISIBLE
        binding.menuListRecyclerView.visibility = View.INVISIBLE
        binding.reviewButton.setTextColor(Color.BLACK)
        binding.menuButton.setTextColor(Color.GRAY)
        binding.categoriesSpinner.visibility = View.VISIBLE
    }

    private fun menuButtonSelect() {
        binding.reviewListRecyclerView.visibility = View.INVISIBLE
        binding.menuListRecyclerView.visibility = View.VISIBLE
        binding.reviewButton.setTextColor(Color.GRAY)
        binding.menuButton.setTextColor(Color.BLACK)
        binding.categoriesSpinner.visibility = View.INVISIBLE
    }

    // 메뉴 스토리지 업로드
    private fun setMenuList() {
        val folderRef: StorageReference =
            FirebaseStorage.getInstance().reference.child("Restaurant/$restaurantName/image/menu")
        binding.restaurantNameTextView.text = restaurantName

        folderRef.listAll().addOnSuccessListener { listResult: ListResult ->
            val menuList = ArrayList<ProcessedMenuInfo>()
            val tasks = ArrayList<Task<Uri>>()
            for (item in listResult.items) {
                tasks.add(item.downloadUrl)
            }
            Tasks.whenAllSuccess<Uri>(tasks).addOnSuccessListener { downloadUrls ->
                for (i in downloadUrls.indices) {
                    val downloadUrl = downloadUrls[i].toString()

                    // Create the menu model and add it to the list
                    val menuModel = ProcessedMenuInfo(
                        listResult.items[i].name, downloadUrl, "8000"
                    )

                    menuList.add(menuModel)
                }
                // Submit the menu list to the adapter
                menuadapter.submitList(menuList)
            }.addOnFailureListener { exception: Exception ->
                // Handle any errors that occur during fetching the download URLs
                Log.e(
                    "RestaurantDetailActivity",
                    "Failed to fetch download URLs: ${exception.message}"
                )
            }
        }.addOnFailureListener { exception: Exception ->
            // Handle any errors that occur during fetching the menu items
            Log.e("RestaurantDetailActivity", "Failed to fetch menu items: ${exception.message}")
        }
    }


    //메인 메뉴 설정
    private fun setMainIamge() {
        val mainImage = FirebaseStorage.getInstance().reference
        mainImage.child("Restaurant/$restaurantName/image/thumbnail/main2.jpg").downloadUrl.addOnSuccessListener { uri ->
            // When image load is successful
            Glide.with(applicationContext).load(uri).into(binding.restaurantImageView)
        }.addOnFailureListener { exception ->

        }
    }

    //스피너 적용
    private fun initSpinner() {
        var SpinnerList = ArrayList<ProcessedReviewInfo>()
        binding.categoriesSpinner.adapter = ArrayAdapter.createFromResource(
            binding.root.context, R.array.spinner_restaurant_review, R.layout.spin_color
        )
        binding.categoriesSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {

                }

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long,
                ) {
                    when (position) {

                        //최신순이므로 초기 설정
                        0 -> {
                            var reviewTotal = 0f
                            SpinnerList.clear()
                            val query = database.child("ReviewInfo").orderByChild("uploadTime")
                            query.addChildEventListener(object : ChildEventListener {
                                override fun onChildAdded(
                                    snapshot: DataSnapshot, previousChildName: String?
                                ) {
                                    val reviewId = snapshot.key.toString()
                                    val userId = snapshot.child("userId").value.toString()
                                    val reviewTitle = snapshot.child("reviewTitle").value.toString()
                                    val reviewContent =
                                        snapshot.child("reviewContent").value.toString()
                                    val categoryId = snapshot.child("categoryId").value.toString()
                                    val restaurantName =
                                        snapshot.child("restaurantName").value.toString()
                                    val reviewRating =
                                        snapshot.child("reviewRating").getValue(Float::class.java)!!
                                            .toFloat()
                                    val reviewLikeCount = snapshot.child("reviewLikeCount")
                                        .getValue(Int::class.java)!!.toInt()
                                    val uploadTime = snapshot.child("uploadTime").value.toString()

                                    val processedReviewInfo = ProcessedReviewInfo(
                                        reviewId,
                                        userId,
                                        reviewTitle,
                                        reviewContent,
                                        categoryId,
                                        restaurantName,
                                        reviewRating,
                                        reviewLikeCount,
                                        uploadTime
                                    )

                                    reviewTotal += reviewRating
                                    // Add the processed review to the list
                                    SpinnerList.add(processedReviewInfo)

                                    // Sort the list in ascending order based on reviewRating
                                    SpinnerList.sortByDescending { it.uploadTime }
                                    // Update the review list here
                                    reviewadapater.submitList(SpinnerList)
                                    reviewadapater.notifyDataSetChanged()
                                    binding.restaurantReviewNumberTextView.text =
                                        (reviewTotal / SpinnerList.size).toString()
                                }

                                override fun onChildChanged(
                                    snapshot: DataSnapshot, previousChildName: String?
                                ) {

                                }

                                override fun onChildRemoved(snapshot: DataSnapshot) {

                                }

                                override fun onChildMoved(
                                    snapshot: DataSnapshot, previousChildName: String?
                                ) {

                                }

                                override fun onCancelled(error: DatabaseError) {

                                }
                            })
                        }
                        //평점순
                        1 -> {
                            SpinnerList.clear()
                            val query = database.child("ReviewInfo").orderByChild("reviewRating")
                            query.addChildEventListener(object : ChildEventListener {
                                override fun onChildAdded(
                                    snapshot: DataSnapshot, previousChildName: String?
                                ) {

                                    val reviewId = snapshot.key.toString()
                                    val userId = snapshot.child("userId").value.toString()
                                    val reviewTitle = snapshot.child("reviewTitle").value.toString()
                                    val reviewContent =
                                        snapshot.child("reviewContent").value.toString()
                                    val categoryId = snapshot.child("categoryId").value.toString()
                                    val restaurantName =
                                        snapshot.child("restaurantName").value.toString()
                                    val reviewRating =
                                        snapshot.child("reviewRating").getValue(Float::class.java)!!
                                            .toFloat()
                                    val reviewLikeCount = snapshot.child("reviewLikeCount")
                                        .getValue(Int::class.java)!!.toInt()
                                    val uploadTime = snapshot.child("uploadTime").value.toString()

                                    val processedReviewInfo = ProcessedReviewInfo(
                                        reviewId,
                                        userId,
                                        reviewTitle,
                                        reviewContent,
                                        categoryId,
                                        restaurantName,
                                        reviewRating,
                                        reviewLikeCount,
                                        uploadTime
                                    )
//
                                    SpinnerList.add(processedReviewInfo)
                                    // Sort the list in ascending order based on reviewRating
                                    SpinnerList.sortByDescending { it.reviewRating }
                                    // Update the review list here
                                    reviewadapater.submitList(SpinnerList)
                                    reviewadapater.notifyDataSetChanged()

                                }

                                override fun onChildChanged(
                                    snapshot: DataSnapshot, previousChildName: String?
                                ) {

                                }

                                override fun onChildRemoved(snapshot: DataSnapshot) {

                                }

                                override fun onChildMoved(
                                    snapshot: DataSnapshot, previousChildName: String?
                                ) {

                                }

                                override fun onCancelled(error: DatabaseError) {

                                }
                            })
                        }
                        //좋아요순
                        2 -> {
                            SpinnerList.clear()
                            val query = database.child("ReviewInfo").orderByChild("reviewLikeCount")
                            query.addChildEventListener(object : ChildEventListener {
                                override fun onChildAdded(
                                    snapshot: DataSnapshot, previousChildName: String?
                                ) {
                                    val reviewId = snapshot.key.toString()
                                    val userId = snapshot.child("userId").value.toString()
                                    val reviewTitle = snapshot.child("reviewTitle").value.toString()
                                    val reviewContent =
                                        snapshot.child("reviewContent").value.toString()
                                    val categoryId = snapshot.child("categoryId").value.toString()
                                    val restaurantName =
                                        snapshot.child("restaurantName").value.toString()
                                    val reviewRating =
                                        snapshot.child("reviewRating").getValue(Float::class.java)!!
                                            .toFloat()
                                    val reviewLikeCount = snapshot.child("reviewLikeCount")
                                        .getValue(Int::class.java)!!.toInt()
                                    val uploadTime = snapshot.child("uploadTime").value.toString()

                                    val processedReviewInfo = ProcessedReviewInfo(
                                        reviewId,
                                        userId,
                                        reviewTitle,
                                        reviewContent,
                                        categoryId,
                                        restaurantName,
                                        reviewRating,
                                        reviewLikeCount,
                                        uploadTime
                                    )
                                    // Add the processed review to the list
                                    SpinnerList.add(processedReviewInfo)
                                    // Sort the list in ascending order based on reviewRating
                                    SpinnerList.sortByDescending { it.reviewLikeCount }
                                    // Update the review list here
                                    reviewadapater.submitList(SpinnerList)
                                    reviewadapater.notifyDataSetChanged()

                                }

                                override fun onChildChanged(
                                    snapshot: DataSnapshot, previousChildName: String?
                                ) {

                                }

                                override fun onChildRemoved(snapshot: DataSnapshot) {

                                }

                                override fun onChildMoved(
                                    snapshot: DataSnapshot, previousChildName: String?
                                ) {

                                }

                                override fun onCancelled(error: DatabaseError) {

                                }
                            })
                        }
                    }
                }

            }
    }

    companion object {
        const val RESTAURANT_NAME = "RESTAURANT_NAME"
    }
}