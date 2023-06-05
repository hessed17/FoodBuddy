package com.ajou.foodbuddy.ui.community.singo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ajou.foodbuddy.data.firebase.model.community.SingoInfo

import com.ajou.foodbuddy.databinding.ActivitySingoBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.ktx.Firebase

class SingoActivity: AppCompatActivity() {
    private lateinit var binding: ActivitySingoBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySingoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        var reviewId = intent.getStringExtra("reviewId").toString()

        //신고 작성후 버튼 누르기
        clickSingoRegister(reviewId)
    }


    private fun clickSingoRegister(reviewId:String){
        binding.registerButton.setOnClickListener {
            FirebaseDatabase.getInstance().reference.child("SingoInfo").push().setValue(SingoInfo(
                reviewId,
                Firebase.auth.currentUser!!.email.toString(),
                binding.singoReasonEditText.text.toString(),
                ServerValue.TIMESTAMP.toString()
            ))
            finish()
        }
    }
}