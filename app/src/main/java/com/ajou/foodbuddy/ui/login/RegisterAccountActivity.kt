package com.ajou.foodbuddy.ui.login

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ajou.foodbuddy.data.firebase.model.profile.LoginUserInfo
import com.ajou.foodbuddy.data.firebase.path.Key
import com.ajou.foodbuddy.databinding.ActivityRegisterAccountBinding
import com.ajou.foodbuddy.extensions.convertStrToBase64
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RegisterAccountActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterAccountBinding

    private var auth: FirebaseAuth = FirebaseAuth.getInstance()
    var database: DatabaseReference = Firebase.database.reference //실시간 파이어베이스 저장시 사용

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initPasswordEditTextView()
        initPasswordRecheckEditTextView()
        initSignInButton()
    }

    private fun initPasswordEditTextView() {
        binding.passwordEditTextView.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {

            } else {
                if (binding.passwordEditTextView.length() < 8) {
                    binding.passwordFalseTextView.visibility = View.VISIBLE
                } else {
                    binding.passwordFalseTextView.visibility = View.INVISIBLE
                }
            }
        }
    }

    private fun initPasswordRecheckEditTextView() {
        binding.passwordCheckEditText.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {

            } else {
                if (binding.passwordEditTextView.text.toString() != binding.passwordCheckEditText.text.toString())
                    binding.passwordCheckFalseEditText.visibility = View.VISIBLE
                else
                    binding.passwordCheckFalseEditText.visibility = View.INVISIBLE
            }
        }
    }

    private fun initSignInButton() {

        binding.joinButton.setOnClickListener {
            val userId = binding.idEditTextView.text.toString()
            val userInfo = LoginUserInfo(
                nickname = binding.userNickNameEditText.text.toString()
            )

            if (binding.idEditTextView.length() == 0 || binding.passwordEditTextView.length() == 0 || binding.userNickNameEditText.length() == 0
            ) {
                Toast.makeText(applicationContext, "빠진 부분이 없는지 확인해주세요.", Toast.LENGTH_LONG).show()
            } else {
                //회원가입 파이어베이스로 보내기
                auth.createUserWithEmailAndPassword(
                    binding.idEditTextView.text.toString(),
                    binding.passwordEditTextView.text.toString()
                )
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // Sign in success, update UI with the signed-in user's information
                            Toast.makeText(
                                applicationContext,
                                "회원 등록한 계정으로 로그인해주세요.",
                                Toast.LENGTH_SHORT
                            ).show()
                            insertAddressToRTDB(userId, userInfo)
//                            saveUserIdToLocal(userId)
                        } else {
                            Toast.makeText(
                                baseContext,
                                "Authentication failed.",
                                Toast.LENGTH_SHORT,
                            ).show()
                        }
                    }
            }
        }
    }

    private fun insertAddressToRTDB(userId: String, userInfo: LoginUserInfo) {
        database.child(Key.USER_INFO).child(userId.convertStrToBase64()).setValue(userInfo)
        finish()
    }
}