package com.ajou.foodbuddy.ui.restaurant.detail

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil.ItemCallback
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ajou.foodbuddy.data.firebase.model.ProcessedMenuInfo
import com.ajou.foodbuddy.databinding.ItemMenuBinding

class MenuAdapter : ListAdapter<ProcessedMenuInfo, MenuAdapter.ViewHolder>(diffUtil) {

    inner class ViewHolder(private val binding: ItemMenuBinding) :
        RecyclerView.ViewHolder(binding.root) {

            fun bind(menuModel: ProcessedMenuInfo) {

            }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemMenuBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
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