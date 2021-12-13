package com.simplation.gallery

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.simplation.gallery.databinding.FragmentGalleryBinding


class GalleryFragment : Fragment() {
    lateinit var galleryViewModel: GalleryViewModel
    lateinit var galleryBinding: FragmentGalleryBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        galleryBinding = FragmentGalleryBinding.inflate(inflater, container, false)
        return galleryBinding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.swipeIndicator -> {
                galleryBinding.swipeLayoutGallery.isRefreshing = true
                // 模拟网络加载
                // Android 11（即 API 30:Android R）弃用了 Handler 默认的无参构造方法
                // Handler(Looper.getMainLooper()).postDelayed({ galleryViewModel.resetQuery() }, 1000)

                galleryViewModel.resetQuery()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        galleryViewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory(requireActivity().application)
        )[GalleryViewModel::class.java]

        // 打开 menu，否则的话不显示
        setHasOptionsMenu(true)

        val galleryAdapter = GalleryAdapter(galleryViewModel)
        galleryBinding.recyclerView.apply {
            adapter = galleryAdapter
            // layoutManager = GridLayoutManager(this@GalleryFragment.context, 2)

            // 使用瀑布流
            layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        }



        galleryViewModel.dataStatusLive.observe(viewLifecycleOwner, {
            galleryAdapter.footerViewStatus = it
            galleryAdapter.notifyItemChanged(galleryAdapter.itemCount - 1)
            if (it == DATA_STATUS_NETWORK_ERROR) galleryBinding.swipeLayoutGallery.isRefreshing =
                false
        })

        galleryViewModel.photoListLive.observe(viewLifecycleOwner, {
            if (galleryViewModel.needToScrollToTop) {
                galleryBinding.recyclerView.scrollToPosition(0)
                galleryViewModel.needToScrollToTop = false
            }
            galleryAdapter.submitList(it)
            galleryBinding.swipeLayoutGallery.isRefreshing = false
        })


//        galleryViewModel.photoListLive.value ?: galleryViewModel.resetQuery()

        galleryBinding.swipeLayoutGallery.setOnRefreshListener {
            galleryViewModel.resetQuery()
        }

        galleryBinding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                if (dy < 0) return
                val layoutManager = recyclerView.layoutManager as StaggeredGridLayoutManager
                val intArray = IntArray(2)
                layoutManager.findLastVisibleItemPositions(intArray)
                if (intArray[0] == galleryAdapter.itemCount - 1) {
                    galleryViewModel.fetchData()
                }
            }
        })
    }

}