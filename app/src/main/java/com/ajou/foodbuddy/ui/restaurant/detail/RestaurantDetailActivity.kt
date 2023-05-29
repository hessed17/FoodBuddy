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
import com.ajou.foodbuddy.extensions.convertStrToBase64
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.ktx.auth
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
        var restaurantName = intent.getStringExtra(RESTAURANT_NAME).toString()
        val userId = Firebase.auth.currentUser!!.email.toString()


        initRestaurantName(restaurantName)
        //reviewAdapter 초기화
        initReviewAdapters()
        //menuAdapter 초기화
        initMenuAdapters()

        //이미지업로드
        setMainIamge(userId, restaurantName)
        setMenuList(restaurantName)

        //스피너 적용
        initSpinner(restaurantName)

        //북마크 버튼 누를 시
        selectBookMark(userId, restaurantName)

        //해당 버튼 클릭시 뷰 변경
        binding.reviewButton.setOnClickListener {
            reviewButtonSelect()

        }
        binding.menuButton.setOnClickListener {
            menuButtonSelect()
        }
    }

    private fun initRestaurantName(Name: String) {
        restaurantName = intent.getStringExtra(Name).toString()
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
    private fun setMenuList(Name: String) {
        val folderRef: StorageReference =
            FirebaseStorage.getInstance().reference.child("Restaurant/$Name/image/menu")
        binding.restaurantNameTextView.text = Name

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
    private fun setMainIamge(myName: String, resName: String) {
        val mainImage = FirebaseStorage.getInstance().reference
        mainImage.child("Restaurant/$resName/image/thumbnail.jpg").downloadUrl.addOnSuccessListener { uri ->
            // When image load is successful
            Glide.with(applicationContext).load(uri).into(binding.restaurantImageView)
        }.addOnFailureListener { exception ->

        }

        //북마크 체크
        checkbookmark(myName, resName)
    }

    //스피너 적용
    private fun initSpinner(Name: String) {
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
                            SpinnerList.clear()
                            var reviewTotal = 0f
                            val headquery = database.child("ReviewPostingInfo").child(Name)
                            headquery.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    val subquery =
                                        database.child("ReviewInfo").orderByChild("uploadTime")
                                    subquery.addChildEventListener(object : ChildEventListener {
                                        override fun onChildAdded(
                                            snapshot: DataSnapshot, previousChildName: String?
                                        ) {
                                            val reviewId = snapshot.key.toString()
                                            val userId =
                                                snapshot.child("userId").value.toString()
                                            val reviewTitle =
                                                snapshot.child("reviewTitle").value.toString()
                                            val reviewContent =
                                                snapshot.child("reviewContent").value.toString()
                                            val categoryId =
                                                snapshot.child("categoryId").value.toString()
                                            val restaurantName =
                                                snapshot.child("restaurantName").value.toString()
                                            val reviewRating =
                                                snapshot.child("reviewRating")
                                                    .getValue(Float::class.java)!!
                                                    .toFloat()
                                            val reviewLikeCount =
                                                snapshot.child("reviewLikeCount")
                                                    .getValue(Int::class.java)!!.toInt()
                                            val uploadTime =
                                                snapshot.child("uploadTime").value.toString()
                                            if (restaurantName == Name) {
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

                                override fun onCancelled(error: DatabaseError) {

                                }

                            })
                        }
                        //평점순
                        1 -> {
                            SpinnerList.clear()
                            val headquery = database.child("ReviewPostingInfo").child(Name)
                            headquery.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    val subquery =
                                        database.child("ReviewInfo").orderByChild("reviewRating")
                                    subquery.addChildEventListener(object : ChildEventListener {
                                        override fun onChildAdded(
                                            snapshot: DataSnapshot, previousChildName: String?
                                        ) {
                                            val reviewId = snapshot.key.toString()
                                            val userId =
                                                snapshot.child("userId").value.toString()
                                            val reviewTitle =
                                                snapshot.child("reviewTitle").value.toString()
                                            val reviewContent =
                                                snapshot.child("reviewContent").value.toString()
                                            val categoryId =
                                                snapshot.child("categoryId").value.toString()
                                            val restaurantName =
                                                snapshot.child("restaurantName").value.toString()
                                            val reviewRating =
                                                snapshot.child("reviewRating")
                                                    .getValue(Float::class.java)!!
                                                    .toFloat()
                                            val reviewLikeCount =
                                                snapshot.child("reviewLikeCount")
                                                    .getValue(Int::class.java)!!.toInt()
                                            val uploadTime =
                                                snapshot.child("uploadTime").value.toString()
                                            if (restaurantName == Name) {
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

                                                SpinnerList.sortByDescending { it.reviewRating }
                                                // Update the review list here
                                                reviewadapater.submitList(SpinnerList)
                                                reviewadapater.notifyDataSetChanged()

                                            }
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

                                override fun onCancelled(error: DatabaseError) {

                                }

                            })
                        }
                        //좋아요순
                        2 -> {
                            SpinnerList.clear()
                            val headquery = database.child("ReviewPostingInfo").child(Name)
                            headquery.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    val subquery =
                                        database.child("ReviewInfo").orderByChild("reviewLikeCount")
                                    subquery.addChildEventListener(object : ChildEventListener {
                                        override fun onChildAdded(
                                            snapshot: DataSnapshot, previousChildName: String?
                                        ) {
                                            val reviewId = snapshot.key.toString()
                                            val userId =
                                                snapshot.child("userId").value.toString()
                                            val reviewTitle =
                                                snapshot.child("reviewTitle").value.toString()
                                            val reviewContent =
                                                snapshot.child("reviewContent").value.toString()
                                            val categoryId =
                                                snapshot.child("categoryId").value.toString()
                                            val restaurantName =
                                                snapshot.child("restaurantName").value.toString()
                                            val reviewRating =
                                                snapshot.child("reviewRating")
                                                    .getValue(Float::class.java)!!
                                                    .toFloat()
                                            val reviewLikeCount =
                                                snapshot.child("reviewLikeCount")
                                                    .getValue(Int::class.java)!!.toInt()
                                            val uploadTime =
                                                snapshot.child("uploadTime").value.toString()
                                            if (restaurantName == Name) {
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

                                                SpinnerList.sortByDescending { it.reviewLikeCount }
                                                // Update the review list here
                                                reviewadapater.submitList(SpinnerList)
                                                reviewadapater.notifyDataSetChanged()
                                            }
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

                                override fun onCancelled(error: DatabaseError) {

                                }

                            })
                        }
                    }
                }

            }
    }

    private fun checkbookmark(myName: String, res: String) {
        val favoriteResDataBase =
            FirebaseDatabase.getInstance().reference.child("FavoriteRestaurantInfo").child(myName.convertStrToBase64())
        favoriteResDataBase.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (bookCheckValue in snapshot.children) {
                    if (res == bookCheckValue.value.toString()) {
                        binding.restaurantBookmarkButton.setBackgroundResource(R.drawable.bookmark_unchecked)
                        break
                    } else {
                        binding.restaurantBookmarkButton.setBackgroundResource(R.drawable.bookmark_checked)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {


            }

        })

    }

    private fun selectBookMark(myName: String, res: String) {
        binding.restaurantBookmarkButton.setOnClickListener {
            val favoriteResDataBase =
                FirebaseDatabase.getInstance().reference.child("FavoriteRestaurantInfo")
                    .child(myName.convertStrToBase64())
            favoriteResDataBase.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var boolean = false
                    for (bookCheckValue in snapshot.children) {
                        if (res == bookCheckValue.value.toString()) {
                            val query = favoriteResDataBase.orderByValue().equalTo(res)
                            query.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(dataSnapshot: DataSnapshot) {
                                    for (childSnapshot in dataSnapshot.children) {
                                        childSnapshot.ref.removeValue()
                                    }
                                }
                                override fun onCancelled(error: DatabaseError) {
                                    TODO("Not yet implemented")
                                }
                            })
                            binding.restaurantBookmarkButton.setBackgroundResource(R.drawable.bookmark_checked)
                            boolean = true //해당 식당이 있으면 true
                        }
                    }
                    //해당 식당을 bookmark하지 않았으면 추가해서 북마크 표시하기
                    if(!boolean){
                        favoriteResDataBase.push().setValue(res)
                        binding.restaurantBookmarkButton.setBackgroundResource(R.drawable.bookmark_unchecked)

                    }
                }

                override fun onCancelled(error: DatabaseError) {


                }

            })

        }

    }

    companion object {
        const val RESTAURANT_NAME = "RESTAURANT_NAME"
    }
}