package com.simplation.gallery

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.get
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.simplation.gallery.databinding.FragmentPagerPhotoBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

const val PERMISSION_REQUEST_CODE = -100

class PagerPhotoFragment : Fragment() {
    private lateinit var pagerPhotoBinging: FragmentPagerPhotoBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        pagerPhotoBinging = FragmentPagerPhotoBinding.inflate(layoutInflater, container, false)
        return pagerPhotoBinging.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val photoList = arguments?.getParcelableArrayList<PhotoItem>("PHOTO_LIST")

        PagerPhotoListAdapter().apply {
            pagerPhotoBinging.viewPager2.adapter = this
            submitList(photoList)
        }

        pagerPhotoBinging.viewPager2.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                pagerPhotoBinging.photoTag.text =
                    resources.getString(R.string.photo_tag, position + 1, photoList?.size)
            }
        })

        // 设置点击跳转的值
        /**
         * 参数 1: 当前点击的 item
         * 参数 2: 滑动的动画
         */
        pagerPhotoBinging.viewPager2.setCurrentItem(arguments?.getInt("PHOTO_POSITION") ?: 0, false)
        pagerPhotoBinging.viewPager2.orientation = ViewPager2.ORIENTATION_VERTICAL

        pagerPhotoBinging.saveButton.setOnClickListener {
            if (Build.VERSION.SDK_INT < 29 && ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // registerForActivityResult()
                requestPermissions(
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    PERMISSION_REQUEST_CODE
                )

            } else {
                viewLifecycleOwner.lifecycleScope.launch {
                    savePhoto()
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                if (grantResults != null && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    viewLifecycleOwner.lifecycleScope.launch {
                        savePhoto()
                    }
                } else {
                    Toast.makeText(requireContext(), "存储失败", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    /*private fun savePhoto() {
        val holder =
            (pagerPhotoBinging.viewPager2[0] as RecyclerView).findViewHolderForAdapterPosition(
                pagerPhotoBinging.viewPager2.currentItem
            ) as PagerPhotoListAdapter.PagerPhotoListViewHolder
        val bitmap = holder.itemView.findViewById<ImageView>(R.id.pagerPhoto).drawable.toBitmap()

        // API 29 以前的写法
        *//*if (MediaStore.Images.Media.insertImage(
                requireContext().contentResolver, bitmap, "", ""
            ) == null
        ) {
            Toast.makeText(requireContext(), "存储失败", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), "存储成功", Toast.LENGTH_SHORT).show()
        }*//*

        val saveUri = requireContext().contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            ContentValues()
        ) ?: kotlin.run {
            return
        }

        requireContext().contentResolver.openOutputStream(saveUri).use {
            // 需要放在工作线程中
            if (bitmap.compress(Bitmap.CompressFormat.JPEG, 90, it)) {
                Toast.makeText(requireContext(), "存储成功", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "存储失败", Toast.LENGTH_SHORT).show()
            }
        }
    }*/

    // 以上方法的改造
    private suspend fun savePhoto() {
        withContext(Dispatchers.IO) {
            val holder =
                (pagerPhotoBinging.viewPager2[0] as RecyclerView).findViewHolderForAdapterPosition(
                    pagerPhotoBinging.viewPager2.currentItem
                ) as PagerPhotoListAdapter.PagerPhotoListViewHolder
            val bitmap =
                holder.itemView.findViewById<ImageView>(R.id.pagerPhoto).drawable.toBitmap()

            // API 29 以前的写法
            if (MediaStore.Images.Media.insertImage(
                    requireContext().contentResolver, bitmap, "", ""
                ) == null
            ) {
                MainScope().launch {
                    Toast.makeText(requireContext(), "存储失败", Toast.LENGTH_SHORT).show()
                }
            } else {
                MainScope().launch {
                    Toast.makeText(requireContext(), "存储成功", Toast.LENGTH_SHORT).show()
                }
            }

            val saveUri = requireContext().contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                ContentValues()
            ) ?: kotlin.run {
                return@withContext
            }

            requireContext().contentResolver.openOutputStream(saveUri).use {
                // user{}  不需要手动关闭
                // 需要放在工作线程中
                if (bitmap.compress(Bitmap.CompressFormat.JPEG, 90, it)) {
                    MainScope().launch {
                        Toast.makeText(requireContext(), "存储成功", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    MainScope().launch {
                        Toast.makeText(requireContext(), "存储失败", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

}