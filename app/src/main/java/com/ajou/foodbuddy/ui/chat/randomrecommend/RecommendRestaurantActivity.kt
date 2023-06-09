package com.ajou.foodbuddy.ui.chat.randomrecommend

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.ajou.foodbuddy.data.firebase.path.Key
import com.ajou.foodbuddy.databinding.ActivityRecommendRestaurantBinding
import com.ajou.foodbuddy.ui.restaurant.detail.RestaurantDetailActivity
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage

class RecommendRestaurantActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRecommendRestaurantBinding
    private lateinit var resName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecommendRestaurantBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bindRestaurant()
        initBackButton()
        initNavigateRestaurantDetailPage()
    }

    private fun bindRestaurant() {
        val database = Firebase.database.reference
        database.child(Key.MENU_INFO).addValueEventListener(object: ValueEventListener {
            val restaurantNameList = mutableListOf<String>()
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (snapshot in dataSnapshot.children) {
                    val restaurantName = snapshot.key.toString()

                    restaurantNameList.add(restaurantName)
                }

                resName = restaurantNameList.random()

                val mainImage = FirebaseStorage.getInstance().reference
                mainImage.child("Restaurant/$resName/image/thumbnail.jpg").downloadUrl.addOnSuccessListener { uri ->
                    // When image load is successful
                    Log.d("uriejvdiv", uri.toString())
                    Glide
                        .with(binding.root)
                        .load(uri)
                        .into(binding.restaurantImageView)
                }.addOnFailureListener { exception ->

                }

                binding.recommendRestaurantTextView.text = resName
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

    }


    private fun initBackButton() {
        binding.backButton.setOnClickListener {
            finish()
        }
    }

    private fun initNavigateRestaurantDetailPage() {
        binding.navigateRestaurantDetailPageButton.setOnClickListener {
            startActivity(Intent(this, RestaurantDetailActivity::class.java).apply {
                putExtra(RestaurantDetailActivity.RESTAURANT_NAME, resName)
            })
        }
    }

    companion object {
        const val CHATROOM_ID = "ChatRoomID"
    }
}