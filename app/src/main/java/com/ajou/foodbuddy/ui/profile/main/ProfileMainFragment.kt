package com.ajou.foodbuddy.ui.profile.main

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.PopupWindow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.ajou.foodbuddy.BaseFragment
import com.ajou.foodbuddy.R
import com.ajou.foodbuddy.data.firebase.model.restaurant.MyRestaurant
import com.ajou.foodbuddy.databinding.FragmentProfileMainBinding
import com.ajou.foodbuddy.extensions.convertStrToBase64
import com.ajou.foodbuddy.ui.login.LoginActivity
import com.ajou.foodbuddy.ui.profile.ProfileViewModel
import com.ajou.foodbuddy.ui.profile.search.SearchAddFriendListActivity
import com.ajou.foodbuddy.ui.profile.search.SearchDeleteFriendListActivity
import com.ajou.foodbuddy.ui.restaurant.detail.RestaurantDetailActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class ProfileMainFragment : BaseFragment<FragmentProfileMainBinding>() {

    private val profileViewModel: ProfileViewModel by activityViewModels()
    private val storageRef = Firebase.storage.reference
    private val database = Firebase.database.reference
    private lateinit var Profileadapter: ProfileRestaurantAdapter
    private val PICK_IMAGE_REQUEST = 1 // Request code for image picker
    private val MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 2


    override fun getViewBinding(): FragmentProfileMainBinding =
        FragmentProfileMainBinding.inflate(layoutInflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initLogoutButton()

        //친구 삭제 버튼 클릭
        deleteFriendButtonClick()

        //친구 추가 버튼 클릭
        addFriendButtonClick()

        //프로필 버튼 누를시 프로필 변경
        changeProfile()
    }

    private fun initLogoutButton() {
        _binding?.logoutButton?.setOnClickListener {
            Firebase.auth.signOut()
            requireActivity().finish()
            startActivity(Intent(requireActivity(), LoginActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()

        //프로필 갱신
        initProfile()

        //레스토랑 어뎁터 업데이트
        initRestaurantAdapter()

        //레스토랑 리사이클러뷰에 데이터 넣기 UserName 받아오기
        pushRestaurant()
    }

    private fun initProfile() {
        val userInfoRef = FirebaseDatabase.getInstance().reference.child("UserInfo")

        userInfoRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val userId = profileViewModel.getUserId()
                val myUser = dataSnapshot.child(userId.convertStrToBase64()) //해당 유저만 보여준다.

                _binding?.friendNameTextView?.text = myUser.child("nickname").value.toString()
                _binding?.navigateFriendListButton?.text =
                    myUser.child("friendCount").value.toString()
                _binding?.root?.context?.let { _binding?.profileImageButton?.let { it1 ->
                    Glide.with(it).load(myUser.child("profileImage").value).into(
                        it1
                    )
                } }

            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle the error if retrieval is canceled
                Log.d("Error", "Error retrieving data: ${databaseError.message}")
            }
        })

        _binding?.logoutButton?.visibility = View.VISIBLE
    }

    private fun initRestaurantAdapter() {
        Profileadapter = ProfileRestaurantAdapter {
            startActivity(Intent(requireActivity(), RestaurantDetailActivity::class.java).apply {
                putExtra(RestaurantDetailActivity.RESTAURANT_NAME, it.restaurantName)
            })
        }
        _binding?.myMenuListRecyclerView?.adapter = Profileadapter
        _binding?.myMenuListRecyclerView?.layoutManager = GridLayoutManager(context, 2)

    }

    private fun pushRestaurant() {
        val userId = Firebase.auth.currentUser!!.email
        var list = ArrayList<MyRestaurant>()
        var cnt = 0
        val ResInfoRef = FirebaseDatabase.getInstance().reference.child("FavoriteRestaurantInfo")
        ResInfoRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val MyUserRes = dataSnapshot.child(userId!!.convertStrToBase64()) //해당 유저의 목록 보여주기
                val totalResCount = MyUserRes.childrenCount.toInt()
                for (res in MyUserRes.children) {
                    val mainImage = FirebaseStorage.getInstance().reference
                    mainImage.child("Restaurant/${res.value.toString()}/image/thumbnail.jpg").downloadUrl.addOnSuccessListener { uri ->
                        cnt++
                        list.add(MyRestaurant(res.value.toString(), uri))
                        if (cnt == totalResCount)
                            Profileadapter.submitList(list)

                    }

                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle the error if retrieval is canceled
                Log.d("Error", "Error retrieving data: ${databaseError.message}")
            }
        })
    }

    private fun  deleteFriendButtonClick(){
        _binding?.navigateFriendListButton?.setOnClickListener {
            val intent:Intent = Intent(requireActivity(), SearchDeleteFriendListActivity::class.java)
            intent.putExtra(USER_NAME, Firebase.auth.currentUser!!.email.toString()) // 현재 유저 이름 보내기
            startActivity(intent)
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == AppCompatActivity.RESULT_OK && data != null) {
            val imageUri: Uri? = data.data
            val userInfoRef = FirebaseDatabase.getInstance().reference.child("UserInfo")
            val userId = Firebase.auth.currentUser!!.email.toString().convertStrToBase64()

            val imageBitmap: Bitmap? = imageUri?.let { getBitmapFromUri(it) }
            if (imageBitmap != null) {
                val imageFileName: String? = getImageFileName(imageUri)
                if (imageFileName != null) {
                    val fileName = "$imageFileName.jpg"
                    val file = File(requireContext().filesDir, fileName)

                    val outputStream = FileOutputStream(file)
                    imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                    outputStream.flush()
                    outputStream.close()
                    val profileImageRef = storageRef.child("Profile/userProfile").child(userId)
                    profileImageRef.putFile(Uri.fromFile(file))
                        .addOnSuccessListener {
                            profileImageRef.downloadUrl.addOnSuccessListener { uri ->
                                // Update the profileImage value in UserInfo node
                                userInfoRef.child(userId).child("profileImage").setValue(uri.toString())

                                // Load the updated image into the ImageView
                                _binding?.root?.context?.let { it1 ->
                                    _binding?.profileImageButton?.let { it2 ->
                                        Glide.with(it1)
                                            .load(uri)
                                            .into(it2)
                                    }
                                }
                            }
                        }
                        .addOnFailureListener { exception ->
                            // Handle the error if the upload fails
                            Log.d("Error", "Error uploading profile image: ${exception.message}")
                        }
                }
            }
        }
    }

    private fun getBitmapFromUri(uri: Uri): Bitmap? {
        return try {
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            bitmap
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    private fun getImageFileName(uri: Uri): String? {
        val cursor = requireContext().contentResolver.query(uri, null, null, null, null)
        return cursor?.use {
            if (it.moveToFirst()) {
                val displayNameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (displayNameIndex != -1) {
                    it.getString(displayNameIndex)
                } else {
                    null
                }
            } else {
                null
            }
        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted, perform the action
                } else {
                    // Permission denied, handle accordingly
                }
                return
            }
        }
    }

    private fun addFriendButtonClick(){
        _binding?.searchOtherUserButton?.setOnClickListener {
            val intent:Intent = Intent(requireActivity(), SearchAddFriendListActivity::class.java)
            intent.putExtra(USER_NAME, Firebase.auth.currentUser!!.email.toString()) // 현재 유저 이름 보내기
            startActivity(intent)

        }
    }
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }


    private fun changeProfile() {
        _binding?.profileImageButton?.setOnClickListener {
            val popupView = layoutInflater.inflate(R.layout.popup_change_profile, null)
            val popupWindow = PopupWindow(
                popupView,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                true
            )

            // Set popupWindow properties
            popupWindow.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) // Make the background transparent
            popupWindow.isOutsideTouchable = true // Dismiss the popup window when clicked outside

            // Show the popup window in the center
            popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0)

            val changeProfileTextView = popupView.findViewById<TextView>(R.id.changeProfileTextView)
            changeProfileTextView.setOnClickListener {
                // Handle the "Change Profile" click event here
                // You can add your logic to perform the desired action when the text is clicked
                // Dismiss the popup window after handling the click event
                openGallery()
                popupWindow.dismiss()
            }
        }

        // Rest of your code...
    }
    companion object {
        const val USER_NAME = "UserName"
    }
}