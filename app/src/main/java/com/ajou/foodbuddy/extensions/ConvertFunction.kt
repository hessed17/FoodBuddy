package com.ajou.foodbuddy.extensions

import android.util.Base64
import java.text.SimpleDateFormat

fun String.convertBase64ToStr(): String {
    val decodedEmailBytes = Base64.decode(this, Base64.DEFAULT)
    return String(decodedEmailBytes)
}

fun String.convertStrToBase64(): String {
    return Base64.encodeToString(this.toByteArray(), Base64.DEFAULT)
}

fun String.convertDateFullToTimestamp(): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd hh:mm")
    val date = sdf.format(this.toLong())
    return date.toString()
}