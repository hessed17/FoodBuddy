package com.ajou.foodbuddy

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.ajou.foodbuddy.data.firebase.path.Key.CHAT_INFO
import com.ajou.foodbuddy.data.firebase.path.Key.FCM_TOKEN
import com.ajou.foodbuddy.data.firebase.path.Key.USER_FRIEND_INFO
import com.ajou.foodbuddy.data.firebase.path.Key.USER_INFO
import com.ajou.foodbuddy.extensions.convertStrToBase64
import com.ajou.foodbuddy.ui.login.LoginActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

//    private val viewModel: MainViewModel by viewModels()
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (checkLogin()) {
            initNavigation()
            askNotificationPermission()

//            Firebase.database.reference.child(USER_INFO).child(Firebase.auth.currentUser!!.email.toString().convertStrToBase64())
//                .child(USER_FRIEND_INFO).push().setValue("YmJiQGJiYi5iYmI=")
        } else {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun checkLogin(): Boolean {
        return Firebase.auth.currentUser != null
    }

    private fun initNavigation() {

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        val navHostFragment = supportFragmentManager.findFragmentById(
            R.id.mainNavigationHostContainer
        ) as NavHostFragment
        navController = navHostFragment.navController

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.setupWithNavController(navController)

        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.restaurant_dest, R.id.chat_dest, R.id.community_dest, R.id.profile_dest)
        )

        setupActionBarWithNavController(navController, appBarConfiguration)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.restaurant_dest || destination.id == R.id.chat_dest || destination.id == R.id.community_dest || destination.id == R.id.profile_dest) {
                bottomNavigationView.visibility = View.VISIBLE
            } else {
                bottomNavigationView.visibility = View.GONE
            }
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted: Boolean ->
        if (isGranted) {
            getFcmTokenAndRegisterToRTDB()
        } else {
            Toast.makeText(this, "채팅 알림을 받을 수 없습니다", Toast.LENGTH_SHORT).show()
        }
    }

    private fun askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                // FCM SDK (and your app) can post notifications.
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                showPermissionRationalDialog()
            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            getFcmTokenAndRegisterToRTDB()
        }
    }

    private fun showPermissionRationalDialog() {
        AlertDialog.Builder(this).setMessage("알림 권한이 없으면 알림을 받을 수 없습니다.")
            .setPositiveButton("권한 허용하기") { _, _ ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }.setNegativeButton("취소") { dialogInterface, _ -> dialogInterface.cancel() }.show()
    }

    private fun getFcmTokenAndRegisterToRTDB() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                return@addOnCompleteListener
            }

            val userId = Firebase.auth.currentUser!!.email.toString()
            val token = task.result
            val tokenMap = mutableMapOf<String, Any>()
            tokenMap[FCM_TOKEN] = token
            Firebase.database.reference.child(CHAT_INFO)
                .child(userId.convertStrToBase64())
                .updateChildren(tokenMap)
        }
    }
}