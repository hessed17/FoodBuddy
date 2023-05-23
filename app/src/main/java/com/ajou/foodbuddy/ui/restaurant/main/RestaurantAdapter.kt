package com.ajou.foodbuddy.ui.restaurant.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ajou.foodbuddy.data.firebase.model.SecondProcessedRestaurantItem
import com.ajou.foodbuddy.databinding.ItemRestaurantBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy

class RestaurantAdapter(
    private val itemClickListener: (SecondProcessedRestaurantItem) -> Unit
) : ListAdapter<SecondProcessedRestaurantItem, RestaurantAdapter.ViewHolder>(diffUtil) {

    inner class ViewHolder(private val binding: ItemRestaurantBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SecondProcessedRestaurantItem) {
            with(binding) {
                // thumbnailImage load
                Glide.with(binding.root)
                    .load(item.thumbnailImage)
//                    .diskCacheStrategy(DiskCacheStrategy.NONE)
//                    .skipMemoryCache(true)
                    .into(binding.restaurantImageView)
                restaurantNameTextView.text = item.restaurantName
                root.setOnClickListener {
                    itemClickListener(item)
                }
            }
        }
    }

    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<SecondProcessedRestaurantItem>() {
            override fun areItemsTheSame(
                oldItem: SecondProcessedRestaurantItem,
                newItem: SecondProcessedRestaurantItem
            ): Boolean {
                return oldItem.restaurantName == newItem.restaurantName
            }

            override fun areContentsTheSame(
                oldItem: SecondProcessedRestaurantItem,
                newItem: SecondProcessedRestaurantItem
            ): Boolean {
                return oldItem == newItem
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(
            ItemRestaurantBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(currentList[position])
    }
}