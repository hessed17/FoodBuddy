package com.ajou.foodbuddy.ui.restaurant.detail

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.PersistableBundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.UiThread
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.ajou.foodbuddy.databinding.ActivityMapBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.overlay.InfoWindow
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import com.ajou.foodbuddy.R

class MapActivity: AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityMapBinding
    private lateinit var naverMap: NaverMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) ||
                    permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                checkLocationPermission()
            }
            else -> {
                showPermissionInfoDialog()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.mapView.getMapAsync(this)
        binding.mapView.onCreate(savedInstanceState)

        checkLocationPermission()
        bindMarkerOfRestaurant()
    }

    private fun bindMarkerOfRestaurant() {

        val restaurantName = intent.getStringExtra(RESTAURANT_NAME).toString()
        val lat = intent.getStringExtra(RESTAURANT_LATITUDE)?.toDouble()
        val lng = intent.getStringExtra(RESTAURANT_LONGITUDE)?.toDouble()

        naverMap.minZoom = 5.0
        naverMap.maxZoom = 18.0

        zoomInTo(15.0)

        val marker = Marker()
        marker.apply {
            position = LatLng(lat!!, lng!!)
            map = naverMap
            icon = OverlayImage.fromResource(R.drawable.locationpin)
            width = 140
            height = 140
            val infoWindow = InfoWindow()
            infoWindow.adapter = object : InfoWindow.DefaultTextAdapter(this@MapActivity) {
                override fun getText(p0: InfoWindow): CharSequence {
                    return restaurantName
                }
            }
        }

    }

    private fun checkLocationPermission() {

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPositionPermission()
            return
        }
        requestStoresByCurrentPos()
    }

    @SuppressLint("MissingPermission")
    private fun requestStoresByCurrentPos() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        fusedLocationClient.lastLocation
            .addOnSuccessListener {
                moveToPos(it.latitude, it.longitude)
            }
    }

    private fun moveToPos(lat: Double, lng: Double) {
        val cameraUpdate =
            CameraUpdate.scrollTo(LatLng(lat, lng))
                .animate(CameraAnimation.Easing)
        naverMap.moveCamera(cameraUpdate)
    }

    private fun zoomInTo(zoomValue: Double) {
        val cameraUpdate =
            CameraUpdate.zoomTo(zoomValue)
        naverMap.moveCamera(cameraUpdate)
    }

    private fun showPermissionInfoDialog() {
        AlertDialog.Builder(this).apply {
            setMessage("위치 정보를 가져오기 위해서, 위치 권한이 필요합니다.")
            setNegativeButton("취소", null)
            setPositiveButton("동의") { _, _ ->
                requestPositionPermission()
            }
        }.show()
    }

    private fun requestPositionPermission() {
        locationPermissionRequest.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    override fun onStart() {
        super.onStart()
        binding.mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        super.onSaveInstanceState(outState, outPersistentState)
        binding.mapView.onSaveInstanceState(outState)
    }

    override fun onStop() {
        super.onStop()
        binding.mapView.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }

    @UiThread
    override fun onMapReady(map: NaverMap) {
        naverMap = map
    }

    companion object {
        const val RESTAURANT_NAME = "RESTAURANT_NAME"
        const val RESTAURANT_LATITUDE = "RESTAURANT_LATITUDE"
        const val RESTAURANT_LONGITUDE = "RESTAURANT_LONGITUDE"
    }
}