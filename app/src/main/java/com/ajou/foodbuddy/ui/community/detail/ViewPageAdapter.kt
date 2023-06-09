package com.ajou.foodbuddy.ui.community.detail

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.ajou.foodbuddy.R
import com.bumptech.glide.Glide.*

class ViewPagerAdapter(private val context: Context, private val images: ArrayList<String>) :
    RecyclerView.Adapter<ViewPagerAdapter.ViewPagerViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewPagerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.item_image_slide,
            parent,
            false
        )
        return ViewPagerViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewPagerViewHolder, position: Int) {
        holder.bind(images[position])
    }

    override fun getItemCount(): Int {
        return images.size
    }

    inner class ViewPagerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.reviewImageView)

        fun bind(imageUri: String) {
            with(context)
                .load(imageUri)
                .into(imageView)
        }
    }
}