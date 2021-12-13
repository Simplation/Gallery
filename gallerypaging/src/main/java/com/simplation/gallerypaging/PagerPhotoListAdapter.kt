package com.simplation.gallerypaging

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import uk.co.senab.photoview.PhotoView

/**
 * @作者: Simplation
 * @日期: 2021/12/13 15:44
 * @描述:
 * @更新:
 */
class PagerPhotoListAdapter :
    ListAdapter<PhotoItem, PagerPhotoListAdapter.PagerPhotoListViewHolder>(DIFFCALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PagerPhotoListViewHolder {
        return PagerPhotoListViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.pager_photo_view, parent, false)
        )
    }

    override fun onBindViewHolder(holder: PagerPhotoListViewHolder, position: Int) {
        val data = getItem(position)

        // 设置图片的真是高度
        holder.pagerPhoto.layoutParams.height = data.photoHeight

        Glide.with(holder.itemView)
            .load(data.previewUrl)
            .placeholder(R.drawable.ic_photo_gray_24dp)
            .into(holder.pagerPhoto)
    }

    object DIFFCALLBACK : DiffUtil.ItemCallback<PhotoItem>() {
        override fun areItemsTheSame(oldItem: PhotoItem, newItem: PhotoItem): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: PhotoItem, newItem: PhotoItem): Boolean {
            return oldItem.photoId == newItem.photoId
        }

    }

    class PagerPhotoListViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        val pagerPhoto: PhotoView = itemView.findViewById(R.id.pagerPhoto)
        // val pagerPhoto: ImageView = itemView.findViewById(R.id.pagerPhoto)
    }
}