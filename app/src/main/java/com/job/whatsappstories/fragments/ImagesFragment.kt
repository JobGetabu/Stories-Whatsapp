package com.job.whatsappstories.fragments


import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import com.cooltechworks.views.shimmer.ShimmerRecyclerView
import com.google.android.ads.nativetemplates.TemplateView
import com.google.android.gms.ads.InterstitialAd
import com.job.whatsappstories.R
import com.job.whatsappstories.adapters.StoriesAdapter
import com.job.whatsappstories.callbacks.StoryCallback
import com.job.whatsappstories.commoners.BaseFragment
import com.job.whatsappstories.commoners.K
import com.job.whatsappstories.commoners.StoryOverview
import com.job.whatsappstories.models.Story
import com.job.whatsappstories.utils.*
import com.job.whatsappstories.viewmodel.WhatsModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File


class ImagesFragment : BaseFragment(), StoryCallback {
    private lateinit var adapter: StoriesAdapter
    private lateinit var sharedPrefs: SharedPreferences
    private lateinit var sharedPrefsEditor: SharedPreferences.Editor
    private lateinit var fileName: String

    private lateinit var mInterstitialAd: InterstitialAd

    private var refreshing = false

    private val rv by lazy { requireActivity().findViewById<ShimmerRecyclerView>(R.id.rv) }
    private val imageEmptyView by lazy { requireActivity().findViewById<ImageView>(R.id.imageEmptyView) }
    private val my_template_bottom by lazy { requireActivity().findViewById<TemplateView>(R.id.my_template_bottom) }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_images, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
        if (!storagePermissionGranted()) {
            requestStoragePermission()
        }

        lifecycleScope.launch {
            fragObserver(vm)
        }

        if (activity?.intent != null) handleInvite(requireActivity(), requireActivity().intent)

        sharedPrefs = requireActivity().getSharedPreferences(activity?.applicationContext?.packageName, MODE_PRIVATE)
        sharedPrefsEditor = requireActivity().getSharedPreferences(activity?.applicationContext?.packageName, MODE_PRIVATE).edit()

        mInterstitialAd = InterstitialAd(context)
        initLoadAdUnit(mInterstitialAd, requireActivity())
        adBizListner(mInterstitialAd)

    }

    private suspend fun fragObserver(model: WhatsModel) {

        model.getCurrentFile().observe(viewLifecycleOwner) {
            fileName = it!!
            lifecycleScope.launch{
                loadStories(fileName)
            }

        }
    }

    private fun initViews() {
        rv.setHasFixedSize(true)
        val mLayoutManager = GridLayoutManager(requireActivity(), 3)

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


        rv.addItemDecoration(RecyclerFormatter.GridItemDecoration(requireActivity(), 3, 5))
        rv.itemAnimator = DefaultItemAnimator()
        (rv.itemAnimator as DefaultItemAnimator).supportsChangeAnimations = false

        adapter = StoriesAdapter(this, requireActivity())
        rv?.adapter = adapter
        rv?.showShimmerAdapter()

        rv.afterMeasured {
            if (this@ImagesFragment::adapter.isInitialized)
                loadNativeAds(this@ImagesFragment.adapter, this@ImagesFragment::insertAdsInStoryItems)
        }

    }

    private fun loadStories(fileName: String, async: Boolean = true) {
        if (!storagePermissionGranted()) {
            requestStoragePermission()
            return
        }

        val dir = File(fileName)

        appExecutors.diskIO().execute {

            val files = dir.listFiles { _, s ->
                s.endsWith(".png") || s.endsWith(".jpg") || s.endsWith(".jpeg")
            }

            if (files != null && files.isNotEmpty()) {
                hasStories()

                if (refreshing) adapter.clearStories()

                val stories = mutableListOf<Story>()

                for (file in files.sortedBy { it.lastModified() }.reversed()) {
                    val story = Story(K.TYPE_IMAGE, file.absolutePath)
                    stories.add(story)
                    //adapter.addStory(story)
                }

                appExecutors.mainThread().execute {
                    adapter.addStories(stories)
                    rv?.hideShimmerAdapter()
                }


                refreshing = false
            } else {
                noStories()
            }

        }
    }

    private suspend fun loadStories(fileName: String) {
        if (!storagePermissionGranted()) {
            requestStoragePermission()
            return
        }

        val dir = File(fileName)

        withContext(Dispatchers.IO){

            val files = dir.listFiles { _, s ->
                s.endsWith(".png") || s.endsWith(".jpg") || s.endsWith(".jpeg")
            }

            withContext(Dispatchers.Main){
                if (files != null && files.isNotEmpty()) {
                    hasStories()

                    if (refreshing) adapter.clearStories()

                    for (file in files.sortedBy { it.lastModified() }.reversed()) {
                        val story = Story(K.TYPE_IMAGE, file.absolutePath)
                        adapter.addStory(story)
                    }

                    rv?.hideShimmerAdapter()

                    refreshing = false
                } else {
                    noStories()
                }
            }
        }
    }

    /*private fun loadStoriesGB(fileName: String) {
        if (!storagePermissionGranted()) {
            requestStoragePermission()
            return
        }

        val dir = File(fileName)

        doAsync {
            val files = dir.listFiles { _, s ->
                s.endsWith(".png") || s.endsWith(".jpg") || s.endsWith(".jpeg")
            }

            uiThread {

                if (files.isNotEmpty()) {
                    hasStories()

                    if (refreshing) adapter.clearStories()

                    for (file in files.sortedBy { it.lastModified() }.reversed()) {
                        val story = Story(K.TYPE_IMAGE, file.absolutePath)
                        adapter.addStory(story)
                    }

                    rv?.hideShimmerAdapter()

                    refreshing = false
                } else {
                    noStories()
                }
            }
        }
    }*/

    private fun noStories() {
        rv?.hideView()
        my_template_bottom?.hideView()
        imageEmptyView?.showView()
    }

    private fun hasStories() {
        imageEmptyView?.hideView()
        rv?.showView()
    }

    override fun onStoryClicked(v: View, story: Story) {
        val overview = StoryOverview(requireActivity(), story, vm)
        overview.show()

    }

}
