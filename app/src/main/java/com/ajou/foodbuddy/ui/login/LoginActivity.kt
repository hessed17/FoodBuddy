package com.ajou.foodbuddy.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ajou.foodbuddy.MainActivity
import com.ajou.foodbuddy.R
import com.ajou.foodbuddy.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    var auth: FirebaseAuth = FirebaseAuth.getInstance()
    var database: DatabaseReference = Firebase.database.reference //실시간 파이어베이스 저장시 사용
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initLoginButton()
        initJoinButton()

    }

    private fun initLoginButton() {

        // 로그인 시도
        binding.loginButton.setOnClickListener {
            auth.signInWithEmailAndPassword(
                binding.loginIdEditTextView.text.toString(),
                binding.loginPasswordEditTextView.text.toString()
            )
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    } else {
                        binding.loginFalseTextView.visibility = View.VISIBLE
                    }
                }
        }
    }

    private fun initJoinButton() {
        binding.userJoinButton.setOnClickListener {
            val intent = Intent(this, RegisterAccountActivity::class.java)
            startActivity(intent)
        }
    }
}