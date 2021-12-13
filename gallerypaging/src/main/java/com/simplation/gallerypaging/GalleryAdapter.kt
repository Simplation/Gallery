package com.simplation.gallerypaging

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import io.supercharge.shimmerlayout.ShimmerLayout

/**
 * @作者: Simplation
 * @日期: 2021/12/13 14:53
 * @描述:
 * @更新:
 */
class GalleryAdapter :
    PagedListAdapter<PhotoItem, GalleryAdapter.MyViewHolder>(DIFFCALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val holder = MyViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_gallery, parent, false)
        )
        holder.itemView.setOnClickListener {
            Bundle().apply {
                putParcelableArrayList("PHOTO_LIST", ArrayList(currentList!!))
                putInt("PHOTO_POSITION", holder.adapterPosition)
                holder.itemView.findNavController()
                    .navigate(R.id.action_galleryFragment_to_pagerPhotoFragment, this)
            }
        }

        return holder
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        val photoItem = getItem(position) ?: return

        with(holder) {
            holder.itemView.findViewById<ShimmerLayout>(R.id.shimmerLayoutCell).apply {
                setShimmerColor(0x55FFFFFF)
                setShimmerAngle(0)
                startShimmerAnimation()
            }

            itemView.findViewById<TextView>(R.id.textViewUser).text = photoItem.photoUser
            itemView.findViewById<TextView>(R.id.textViewFavorites).text =
                photoItem.photoFavorites.toString()
            itemView.findViewById<TextView>(R.id.textViewLikes).text =
                photoItem.photoLikes.toString()

            // 设定图片的固定高度
            itemView.findViewById<ImageView>(R.id.imageView).layoutParams.height =
                photoItem.photoHeight
        }

        Glide.with(holder.itemView)
            .load(photoItem.previewUrl)
            .placeholder(R.drawable.ic_photo_gray_24dp)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    return false.also {
                        holder.itemView.findViewById<ShimmerLayout>(R.id.shimmerLayoutCell)
                            ?.stopShimmerAnimation()
                    }
                }
            })
            .into(holder.itemView.findViewById(R.id.imageView))
    }

    object DIFFCALLBACK : DiffUtil.ItemCallback<PhotoItem>() {
        override fun areItemsTheSame(oldItem: PhotoItem, newItem: PhotoItem): Boolean {
            return oldItem.photoId == newItem.photoId
        }

        override fun areContentsTheSame(oldItem: PhotoItem, newItem: PhotoItem): Boolean {
            return oldItem == newItem
        }

    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
