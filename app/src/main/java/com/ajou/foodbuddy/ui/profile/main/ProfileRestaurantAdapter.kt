package com.ajou.foodbuddy.ui.profile.main

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ajou.foodbuddy.data.firebase.model.MyRestaurant
import com.ajou.foodbuddy.databinding.ItemRestaurantBinding
import com.ajou.foodbuddy.ui.restaurant.detail.RestaurantDetailActivity
import com.bumptech.glide.Glide


class ProfileRestaurantAdapter(
    private val restaurantClickListener: (MyRestaurant) -> Unit
) : ListAdapter<MyRestaurant, ProfileRestaurantAdapter.ViewHolder>(diffUtil) {

    inner class ViewHolder(private val binding: ItemRestaurantBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(resModel: MyRestaurant) {
            binding.restaurantNameTextView.text = resModel.restaurantName
            Glide.with(binding.root.context).load(resModel.thumbnailImage).into(binding.restaurantImageView)

            binding.MymenuContraintLayout.setOnClickListener {
                restaurantClickListener(resModel)
                //Send Intent Review Id?.. when clicked 해당 식당 intent보내서 처리하기
//                val intent = Intent(binding.root.context, RestaurantDetailActivity::class.java)
//                intent.putExtra("restaurantname",resModel.restaurantName)
//                binding.root.context.startActivity(int0

            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
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

    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<MyRestaurant>() {
            override fun areItemsTheSame(oldItem: MyRestaurant, newItem: MyRestaurant): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: MyRestaurant, newItem: MyRestaurant): Boolean {
                return oldItem.restaurantName == newItem.restaurantName
            }
        }
    }
}
