package com.simplation.gallery

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import io.supercharge.shimmerlayout.ShimmerLayout

/**
 * @作者: Simplation
 * @日期: 2021/12/07 14:05
 * @描述:
 * @更新:
 */
class GalleryAdapter(val galleryViewModel: GalleryViewModel) :
    ListAdapter<PhotoItem, GalleryAdapter.MyViewHolder>(DIFFCALLBACK) {
    var footerViewStatus = DATA_STATUS_CAN_LOAD_MORE

    companion object {
        const val NORMAL_VIEW_TYPE = 0
        const val FOOTER_VIEW_TYPE = 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val holder: MyViewHolder
        if (viewType == NORMAL_VIEW_TYPE) {
            holder = MyViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.item_gallery, parent, false)
            )
            holder.itemView.setOnClickListener {
                Bundle().apply {
                    putParcelableArrayList("PHOTO_LIST", ArrayList(currentList))
                    putInt("PHOTO_POSITION", holder.adapterPosition)
                    holder.itemView.findNavController()
                        .navigate(R.id.action_galleryFragment_to_pagerPhotoFragment, this)
                }
            }
        } else {
            holder = MyViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_gallery_footer, parent, false)
                    .also {
                        // 设置居中展示
                        (it.layoutParams as StaggeredGridLayoutManager.LayoutParams).isFullSpan =
                            true
                        it.setOnClickListener { itemView ->
                            itemView.findViewById<ProgressBar>(R.id.progressBar).visibility = View.VISIBLE
                            itemView.findViewById<TextView>(R.id.textView).text = "正在加载..."
                            galleryViewModel.fetchData()
                        }
                    }
            )
        }

        return holder
    }

    override fun getItemCount(): Int {
        return super.getItemCount() + 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == itemCount - 1) {
            FOOTER_VIEW_TYPE
        } else {
            NORMAL_VIEW_TYPE
        }
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        // 如果滑动到最后一项，则直接返回
        if (position == itemCount - 1) {
            with(holder.itemView) {
                when (footerViewStatus) {
                    DATA_STATUS_CAN_LOAD_MORE -> {
                        findViewById<ProgressBar>(R.id.progressBar).visibility = View.VISIBLE
                        findViewById<TextView>(R.id.textView).text = "正在加载"
                        isClickable = false
                    }

                    DATA_STATUS_NO_MORE -> {
                        findViewById<ProgressBar>(R.id.progressBar).visibility = View.GONE
                        findViewById<TextView>(R.id.textView).text = "已全部加载完毕"
                        isClickable = false
                    }

                    DATA_STATUS_NETWORK_ERROR -> {
                        findViewById<ProgressBar>(R.id.progressBar).visibility = View.GONE
                        findViewById<TextView>(R.id.textView).text = "网络错误，点击重试"
                        isClickable = true
                    }
                }
            }
            return
        }

        val photoItem = getItem(position)

        with(holder) {
            holder.itemView.findViewById<ShimmerLayout>(R.id.shimmerLayoutItem).apply {
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
            .placeholder(R.drawable.ic_photo_placeholder)
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
                        holder.itemView.findViewById<ShimmerLayout>(R.id.shimmerLayoutItem)
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
