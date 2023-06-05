package com.ajou.foodbuddy.ui.community.register
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ajou.foodbuddy.data.firebase.model.community.ImageInfo
import com.ajou.foodbuddy.databinding.ItemRegisterReviewImageBinding

class CommunityImageAdapter(private val onDeleteImageClickListener: OnDeleteImageClickListener): ListAdapter<ImageInfo, CommunityImageAdapter.ViewHolder>(diffUtil) {
    interface OnDeleteImageClickListener {
        fun onDeleteImageClick(imageInfo: ImageInfo)
    }
    inner class ViewHolder(private val binding: ItemRegisterReviewImageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(communityImageUris: ImageInfo) {
            binding.previewImageView.setImageURI(communityImageUris.ImageUri)
            binding.deleteImageButton.setOnClickListener {
                onDeleteImageClickListener.onDeleteImageClick(communityImageUris)
            }
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemRegisterReviewImageBinding.inflate(LayoutInflater.from(parent.context), parent, false))




    override fun onBindViewHolder(holder: CommunityImageAdapter.ViewHolder, position: Int) {
        holder.bind(currentList[position])
    }
    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<ImageInfo>() {
            override fun areItemsTheSame(oldItem: ImageInfo, newItem: ImageInfo): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: ImageInfo, newItem: ImageInfo): Boolean {
                return oldItem.ImageUri == newItem.ImageUri
            }

        }
    }
}