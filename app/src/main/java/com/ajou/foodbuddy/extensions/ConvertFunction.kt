package com.ajou.foodbuddy.extensions

import java.net.URLDecoder
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.*

fun String.convertUtf8ToStr(): String {
    return URLDecoder.decode(this, "UTF-8")
}

fun String.convertStrToUtf8(): String {
    return URLEncoder.encode(this, "UTF-8")
}

fun String.convertDateFullToTimestamp(): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd hh:mm")
    val date = sdf.format(this.toLong())
    return date.toString()
}