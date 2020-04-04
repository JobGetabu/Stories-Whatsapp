package com.job.whatsappstories.fragments


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import com.job.whatsappstories.R
import com.job.whatsappstories.adapters.StoriesAdapter
import com.job.whatsappstories.callbacks.StoryCallback
import com.job.whatsappstories.commoners.AppUtils.isImage
import com.job.whatsappstories.commoners.AppUtils.isVideo
import com.job.whatsappstories.commoners.BaseFragment
import com.job.whatsappstories.commoners.K
import com.job.whatsappstories.commoners.StoryOverview
import com.job.whatsappstories.models.Story
import com.job.whatsappstories.utils.RecyclerFormatter
import com.job.whatsappstories.utils.hideView
import com.job.whatsappstories.utils.showView
import com.job.whatsappstories.viewmodel.WhatsModel
import kotlinx.android.synthetic.main.fragment_saved.*
import kotlinx.android.synthetic.main.native_bottom_ad.*
import kotlinx.android.synthetic.main.saved_empty.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.io.File

class SavedFragment : BaseFragment(), StoryCallback {
    private lateinit var adapter: StoriesAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_saved, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()

        fragObserver(vm)
        loadStories()
        loadNativeAds(adapter, this::insertAdsInStoryItems)
    }

    private fun fragObserver(model: WhatsModel) {

    }

    private fun initViews() {
        rv.setHasFixedSize(true)

        val mLayoutManager = GridLayoutManager(activity!!, 3)
        rv.layoutManager = mLayoutManager
        mLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return when (adapter.getItemViewType(position)) {
                    0 -> 1
                    1 -> 3
                    else -> 1
                }
            }
        }

        rv.addItemDecoration(RecyclerFormatter.GridItemDecoration(activity!!, 3, 5))
        rv.itemAnimator = DefaultItemAnimator()
        (rv.itemAnimator as DefaultItemAnimator).supportsChangeAnimations = false

        adapter = StoriesAdapter(this, activity!!)
        rv?.showShimmerAdapter()
        rv.adapter = adapter

    }

    private fun loadStories() {
        if (!storagePermissionGranted()) {
            requestStoragePermission()
            return
        }

        val dir = File(K.SAVED_STORIES)
        if (!dir.exists())
            dir.mkdirs()

        doAsync {
            val files = dir.listFiles { _, s ->
                s.endsWith(".png") || s.endsWith(".jpg") || s.endsWith(".jpeg") || s.endsWith(".mp4") || s.endsWith(".gif") }

            uiThread {

                if (files != null && files.isNotEmpty()) {
                    hasStories()
                    var story = Story()

                    for (file in files.sortedBy { it.lastModified() }.reversed()) {
                        if (isImage(file)) {
                            story = Story(K.TYPE_IMAGE, file.absolutePath)
                        } else if (isVideo(file)) {
                            story = Story(K.TYPE_VIDEO, file.absolutePath)
                        }

                        adapter.addStory(story)
                    }

                    rv?.hideShimmerAdapter()

                } else {
                    noStories()
                }
            }

        }

    }

    private fun noStories() {
        rv?.hideView()
        my_template_bottom?.hideView()
        savedEmptyView?.showView()
    }

    private fun hasStories() {
        savedEmptyView?.hideView()
        rv?.showView()
    }

    override fun onStoryClicked(v: View, story: Story) {
        val overview = StoryOverview(activity!!, story, vm,"TRUE")
        overview.show()
    }

}
