package com.ajou.foodbuddy.data.firebase.model

data class MenuInfo(
    val imageName: String? = null,
    val price: String? = null
) {
    fun toProcessedMenuInfo(menuName: String) =
        ProcessedMenuInfo(
            menuName = menuName,
            imageName = imageName!!,
            price = price!!
        )
}

data class ProcessedMenuInfo(
    val menuName: String,
    val imageName: String,
    val price: String
)