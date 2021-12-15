package com.simplation.gallerypaging

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.simplation.gallerypaging.databinding.FragmentPagerPhotoBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PagerPhotoFragment : Fragment() {
	private val galleryViewModel: GalleryViewModel by activityViewModels()
	lateinit var pagerPhotoBinding: FragmentPagerPhotoBinding

	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		pagerPhotoBinding = FragmentPagerPhotoBinding.inflate(layoutInflater, container, false)
		return pagerPhotoBinding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		val adapter = PagerPhotoListAdapter()
		pagerPhotoBinding.viewPager2.adapter = adapter

		galleryViewModel.pageListLiveData.observe(viewLifecycleOwner, {
			adapter.submitList(it)
			// 设置点击跳转的值
			/**
			 * 参数 1: 当前点击的 item
			 * 参数 2: 滑动的动画
			 */
			pagerPhotoBinding.viewPager2.setCurrentItem(
				arguments?.getInt("PHOTO_POSITION") ?: 0,
				false
			)
		})

		pagerPhotoBinding.viewPager2.registerOnPageChangeCallback(object :
			ViewPager2.OnPageChangeCallback() {
			override fun onPageSelected(position: Int) {
				super.onPageSelected(position)
				pagerPhotoBinding.photoTag.text =
					resources.getString(
						R.string.photo_tag,
						position + 1,
						galleryViewModel.pageListLiveData.value?.size
					)
			}
		})

		// 设置 viewPager2 为垂直方向
		// pagerPhotoBinding.viewPager2.orientation = ViewPager2.ORIENTATION_VERTICAL

		pagerPhotoBinding.saveButton.setOnClickListener {
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
			(pagerPhotoBinding.viewPager2[0] as RecyclerView).findViewHolderForAdapterPosition(
				pagerPhotoBinding.viewPager2.currentItem
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
				(pagerPhotoBinding.viewPager2[0] as RecyclerView).findViewHolderForAdapterPosition(
					pagerPhotoBinding.viewPager2.currentItem
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