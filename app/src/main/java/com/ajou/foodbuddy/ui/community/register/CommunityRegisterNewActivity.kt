package com.ajou.foodbuddy.ui.community.register

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.ajou.foodbuddy.data.firebase.model.community.ImageInfo
import com.ajou.foodbuddy.data.firebase.model.community.ProcessedReviewInfo
import com.ajou.foodbuddy.databinding.FragmentCommunityRegisterNewReviewBinding
import com.ajou.foodbuddy.extensions.convertTimeStampToDate
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class CommunityRegisterNewActivity : AppCompatActivity(),
    CommunityImageAdapter.OnDeleteImageClickListener {
    private lateinit var binding: FragmentCommunityRegisterNewReviewBinding
    var resName: String? = null
    var reviewRating: Float = 0f
    var title: String? = null
    var content: String? = null
    var addpictureList = ArrayList<ImageInfo>()
    private lateinit var Imageadapter: CommunityImageAdapter

    private val PICK_IMAGE_REQUEST = 1 // Request code for image picker
    private val MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentCommunityRegisterNewReviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //식당명 찾기
        setResEdit()
        //제목
        setTitleEdit()
        //내용
        setContentEdit()

        //리사이클러뷰 초기화
        initImageRecyclerView()

        //평점, 사진추가, 제목 및 내용 작성하여 등록하기
        allSummit()


    }

    private fun initImageRecyclerView() {
        Imageadapter = CommunityImageAdapter(this)
        binding.imageRecyclerView.adapter = Imageadapter
        binding.imageRecyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
    }

    override fun onDeleteImageClick(imageInfo: ImageInfo) {
        addpictureList.remove(imageInfo)
        Imageadapter.submitList(addpictureList.toList())
        Imageadapter.notifyDataSetChanged()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            val imageUri: Uri? = data.data

            val imageBitmap: Bitmap? = imageUri?.let { getBitmapFromUri(it) }
            if (imageBitmap != null) {
                val imageFileName: String? = getImageFileName(imageUri)
                if (imageFileName != null) {
                    val fileName = "$imageFileName.jpg"
                    val file = File(applicationContext.filesDir, fileName)

                    val outputStream = FileOutputStream(file)
                    imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                    outputStream.flush()
                    outputStream.close()
                    addpictureList.add(ImageInfo(Uri.fromFile(file)))
                    Imageadapter.submitList(addpictureList)
                    Imageadapter.notifyDataSetChanged()
                }
            }
        }
    }

    private fun getBitmapFromUri(uri: Uri): Bitmap? {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            bitmap
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    private fun getImageFileName(uri: Uri): String? {
        val cursor = contentResolver.query(uri, null, null, null, null)
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

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    private fun addPicture() {
        binding.restaurantPictureButton.setOnClickListener {
            openGallery()
        }


    }

    private fun setRatingbar() {
        binding.reviewRatingBar.setOnRatingBarChangeListener { ratingBar, rating, fromUser ->
            binding.reviewRatingBar.rating = rating
            reviewRating = rating
        }
    }

    private fun setResEdit() {
        binding.searchRestaurantEditTextView.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE || event?.keyCode == KeyEvent.KEYCODE_ENTER) {
                findRestaurant(binding.searchRestaurantEditTextView.text.toString())
                true
            } else {
                false
            }
        }
    }

    private fun setTitleEdit() {
        binding.reviewTitleEditText.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE || event?.keyCode == KeyEvent.KEYCODE_ENTER) {
                title = binding.reviewTitleEditText.text.toString()
                true
            } else {
                false
            }
        }

    }

    private fun setContentEdit() {
        binding.restaurantContentEditText.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE || event?.keyCode == KeyEvent.KEYCODE_ENTER) {
                content = binding.restaurantContentEditText.text.toString()
                true
            } else {
                false
            }
        }

    }

    private fun findRestaurant(findName: String) {
        val restaruantInfo = FirebaseDatabase.getInstance().reference.child("RestaurantInfo")
        restaruantInfo.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (name in snapshot.children) {
                    if (name.key.toString() == findName) {
                        resName = name.key.toString()
                        break;
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

    }

    private fun allSummit() {
        //레이팅바 설정
        setRatingbar()
        //사진추가
        addPicture()
        val reviewInfo = FirebaseDatabase.getInstance().reference.child("ReviewInfo")
        val reviewPostingInfo = FirebaseDatabase.getInstance().reference.child("ReviewPostingInfo")
        var uploadTime: String? = null
        binding.registerButton.setOnClickListener {
            Log.d("resName", resName.toString())
            val resCategorys = FirebaseDatabase.getInstance().reference.child("RestaurantInfo").child(resName.toString())
            resCategorys.addListenerForSingleValueEvent(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    for(cate in snapshot.children){
                        val processedReviewInfo = ProcessedReviewInfo(
                            Firebase.auth.currentUser!!.email.toString(), //string계정으로 전달
                            title.toString(),
                            content.toString(),
                            cate.value.toString(),
                            resName.toString(),
                            reviewRating,
                            0,
                            System.currentTimeMillis().toString().convertTimeStampToDate()
                        )
                        Log.d("reviewInfo", processedReviewInfo.toString())
                        uploadTime = System.currentTimeMillis().toString().convertTimeStampToDate()
                        reviewPostingInfo.child(resName.toString()).push()
                            .setValue(Firebase.auth.currentUser!!.email.toString())
                        reviewInfo.push().setValue(processedReviewInfo)
                        Thread.sleep(200)
                        reviewInfo.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                for (reviews in snapshot.children) {
                                    if (reviews.child("userId").value.toString() == Firebase.auth.currentUser!!.email.toString()
                                        && reviews.child("uploadTime").value.toString() == uploadTime
                                    ) {
                                        for (list in addpictureList) {
                                            reviewInfo.child(reviews.key.toString()).child("restaurantImage")
                                                .push().setValue(list.ImageUri.toString())
                                        }
                                        break
                                    }
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {

                            }

                        })
                        break
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
            finish()
        }
    }
}