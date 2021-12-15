package com.simplation.gallerypaging

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.simplation.gallerypaging.databinding.FragmentGalleryBinding

class GalleryFragment : Fragment() {
    private lateinit var galleryBinding: FragmentGalleryBinding

    // by viewModels() 和 activityViewModels()
    private val galleryViewModel: GalleryViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
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

                // galleryViewModel.resetQuery()
                galleryViewModel.resetQuery()
            }
            R.id.menuRetry -> {
                galleryViewModel.retry()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // onActivityCreated() 被废弃

        // 设置开启菜单选项
        setHasOptionsMenu(true)

        // 创建 ViewModel
        /*galleryViewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory(requireActivity().application)
        )[GalleryViewModel::class.java]*/

        val galleryAdapter = GalleryAdapter(galleryViewModel)
        galleryBinding.recyclerView.apply {
            adapter = galleryAdapter
            // layoutManager = GridLayoutManager(this@GalleryFragment.context, 2)

            // 使用瀑布流
            layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        }

        galleryViewModel.pageListLiveData.observe(viewLifecycleOwner, {
            galleryAdapter.submitList(it)
        })

        galleryBinding.swipeLayoutGallery.setOnClickListener {
            galleryViewModel.resetQuery()
        }

        galleryViewModel.networkStatus.observe(viewLifecycleOwner, {
            Log.d("TAG", "onActivityCreated: $it")
            galleryAdapter.updateNetworkStatus(it)
            galleryBinding.swipeLayoutGallery.isRefreshing = it == NetworkStatus.INITIAL_LOADING
        })
    }
}