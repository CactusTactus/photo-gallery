package com.example.photogallery

import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.*
import com.example.photogallery.databinding.FragmentPhotoGalleryBinding
import com.example.photogallery.model.GalleryItem
import java.util.concurrent.TimeUnit

private const val TAG = "PhotoGalleryFragment"
private const val POLL_WORK_NAME = "poll_work"

// TODO: add loading indicator (en p. 554)
class PhotoGalleryFragment : VisibleFragment() {
    private val photoGalleryViewModel: PhotoGalleryViewModel by lazy {
        ViewModelProvider(this).get(PhotoGalleryViewModel::class.java)
    }

    private lateinit var binding: FragmentPhotoGalleryBinding
    private lateinit var thumbnailDownloader: ThumbnailDownloader<PhotoViewHolder>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        retainInstance = true
        val responseHandler = Handler(Looper.getMainLooper())
        thumbnailDownloader = ThumbnailDownloader(responseHandler) { photoViewHolder, bitmap ->
            val drawable = BitmapDrawable(resources, bitmap)
            photoViewHolder.bindDrawable(drawable)
        }
        lifecycle.addObserver(thumbnailDownloader.fragmentLifecycleObserver)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_fragment_photo_gallery, menu)

        val searchItem = menu.findItem(R.id.menu_item_search)
        val searchView = searchItem.actionView as SearchView

        searchView.apply {
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                // TODO: add submitting for empty query
                // https://medium.com/@_foso_/how-to-use-a-searchview-with-an-empty-query-text-submit-1ecdff651181
                override fun onQueryTextSubmit(query: String): Boolean {
                    Log.d(TAG, "onQueryTextSubmit() -> query: $query")
                    photoGalleryViewModel.fetchPhotos(query)
                    clearFocus()
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    Log.d(TAG, "onQueryTextChange() -> query: $newText")
                    return false
                }
            })

            setOnSearchClickListener {
                setQuery(photoGalleryViewModel.searchTerm, false)
            }

            val toggleItem = menu.findItem(R.id.menu_item_toggle_polling)
            val isPolling = QueryPreferences.isPolling(requireContext())
            toggleItem.setTitle(if (isPolling) R.string.stop_polling else R.string.start_polling)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_item_clear -> {
                photoGalleryViewModel.fetchPhotos()
                true
            }
            R.id.menu_item_toggle_polling -> {
                val isPolling = QueryPreferences.isPolling(requireContext())
                if (isPolling) {
                    WorkManager.getInstance(requireContext()).cancelUniqueWork(POLL_WORK_NAME)
                    QueryPreferences.setPolling(requireContext(), false)
                } else {
                    runWork()
                }
                activity?.invalidateOptionsMenu()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPhotoGalleryBinding.inflate(layoutInflater, container, false)
        binding.photoRecyclerView.layoutManager = GridLayoutManager(requireContext(), 3)
        viewLifecycleOwner.lifecycle.addObserver(thumbnailDownloader.viewLifecycleObserver)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        photoGalleryViewModel.galleryItemLiveData.observe(viewLifecycleOwner) { galleryItems ->
            Log.d(TAG, "Have gallery items from ViewModel $galleryItems")
            binding.photoRecyclerView.adapter = PhotoRecyclerViewAdapter(galleryItems)
        }
    }

    override fun onDestroyView() {
        viewLifecycleOwner.lifecycle.removeObserver(thumbnailDownloader.viewLifecycleObserver)
        super.onDestroyView()
    }

    override fun onDestroy() {
        lifecycle.removeObserver(thumbnailDownloader.fragmentLifecycleObserver)
        super.onDestroy()
    }

    private fun runWork() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.UNMETERED)
            .build()
        val workRequest = PeriodicWorkRequest
            .Builder(PollWorker::class.java, 15, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()
        WorkManager.getInstance(requireContext())
            .enqueueUniquePeriodicWork(POLL_WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, workRequest)
        QueryPreferences.setPolling(requireContext(), true)
    }

    companion object {
        @JvmStatic
        fun newInstance() = PhotoGalleryFragment()
    }

    private inner class PhotoRecyclerViewAdapter(private val galleryItems: List<GalleryItem>) :
        RecyclerView.Adapter<PhotoViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
            val view =
                layoutInflater.inflate(R.layout.list_item_gallery, parent, false) as ImageView
            return PhotoViewHolder(view)
        }

        override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
            val galleryItem = galleryItems[position]
            holder.bindGalleryItem(galleryItem)
            val placeholder =
                ContextCompat.getDrawable(requireContext(), R.drawable.bill_up_close)
                    ?: ColorDrawable()
            holder.bindDrawable(placeholder)
            thumbnailDownloader.queueThumbnail(holder, galleryItem.url)
        }

        override fun getItemCount() = galleryItems.size
    }

    private inner class PhotoViewHolder(private val itemImageView: ImageView) :
        RecyclerView.ViewHolder(itemImageView), View.OnClickListener {
        val bindDrawable: (Drawable) -> Unit = itemImageView::setImageDrawable

        private lateinit var galleryItem: GalleryItem

        init {
            itemImageView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            // The way to open page in browser
            // val intent = Intent(Intent.ACTION_VIEW, galleryItem.photoPageUri)
            val intent = PhotoPageActivity.newIntent(requireContext(), galleryItem.photoPageUri)
            startActivity(intent)
        }

        fun bindGalleryItem(item: GalleryItem) {
            galleryItem = item
        }
    }

    // TODO: Challenge: Paging (p. 503)
//    private class PhotoAdapter :
//        PagingDataAdapter<GalleryItem, PhotoViewHolder>(diffCallback) {
//        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
//            return PhotoViewHolder(TextView(parent.context))
//        }
//
//        override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
//            holder.bindTitle(getItem(position)?.title ?: "")
//        }
//
//        companion object {
//            val diffCallback = object : DiffUtil.ItemCallback<GalleryItem>() {
//                override fun areContentsTheSame(
//                    oldItem: GalleryItem,
//                    newItem: GalleryItem
//                ): Boolean {
//                    return oldItem == newItem
//                }
//
//                override fun areItemsTheSame(
//                    oldItem: GalleryItem,
//                    newItem: GalleryItem
//                ): Boolean {
//                    return oldItem == newItem
//                }
//            }
//        }
//    }
}