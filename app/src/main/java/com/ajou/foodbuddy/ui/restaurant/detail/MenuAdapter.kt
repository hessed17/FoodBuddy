package com.ajou.foodbuddy.ui.restaurant.detail

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil.ItemCallback
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ajou.foodbuddy.R
import com.ajou.foodbuddy.data.firebase.model.ProcessedMenuInfo
import com.ajou.foodbuddy.databinding.ItemMenuBinding
import com.bumptech.glide.Glide

class MenuAdapter : ListAdapter<ProcessedMenuInfo, MenuAdapter.ViewHolder>(diffUtil) {

    inner class ViewHolder(private val binding: ItemMenuBinding) :
        RecyclerView.ViewHolder(binding.root) {
            fun bind(menuModel: ProcessedMenuInfo) {
                val priceRegex = Regex("\\d+")
                binding.menuNameText.text = menuModel.menuName.substringBeforeLast(priceRegex.find(menuModel.menuName)?.value.toString())
                binding.menuPriceText.text = priceRegex.find(menuModel.menuName)?.value
                Glide.with(binding.root.context).load(menuModel.imageName).into(binding.menuImageImage)

            }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemMenuBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: MenuAdapter.ViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    companion object {
        val diffUtil = object : ItemCallback<ProcessedMenuInfo>() {
            override fun areItemsTheSame(oldItem: ProcessedMenuInfo, newItem: ProcessedMenuInfo): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: ProcessedMenuInfo, newItem: ProcessedMenuInfo): Boolean {
                    return oldItem.menuName == newItem.menuName
            }

        }
    }
}