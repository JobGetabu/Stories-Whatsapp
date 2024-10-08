package com.job.whatsappstories.fragments


import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import com.cooltechworks.views.shimmer.ShimmerRecyclerView
import com.google.android.ads.nativetemplates.TemplateView
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.reward.RewardItem
import com.google.android.gms.ads.reward.RewardedVideoAd
import com.google.android.gms.ads.reward.RewardedVideoAdListener
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
import timber.log.Timber
import java.io.File

class VideosFragment : BaseFragment(), StoryCallback, RewardedVideoAdListener {

    private lateinit var adapter: StoriesAdapter
    private lateinit var mRewardedVideoAd: RewardedVideoAd
    private lateinit var sharedPrefs: SharedPreferences
    private lateinit var sharedPrefsEditor: SharedPreferences.Editor
    private lateinit var fileName: String

    private val rv by lazy { requireActivity().findViewById<ShimmerRecyclerView>(R.id.rv) }
    private val videoEmptyView by lazy { requireActivity().findViewById<ConstraintLayout>(R.id.videoEmptyView) }
    private val my_template_bottom by lazy { requireActivity().findViewById<TemplateView>(R.id.my_template_bottom) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_videos, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()

        fragObserver(vm)

        sharedPrefs = requireActivity().getSharedPreferences(activity?.applicationContext?.packageName, Context.MODE_PRIVATE)
        sharedPrefsEditor = requireActivity().getSharedPreferences(activity?.applicationContext?.packageName, Context.MODE_PRIVATE).edit()

        mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(activity)
        mRewardedVideoAd.rewardedVideoAdListener = this

        initLoadVideoAdUnit(mRewardedVideoAd, requireActivity())
    }

    private fun fragObserver(model: WhatsModel) {

        model.getCurrentFile().observe(viewLifecycleOwner) {
            fileName = it!!

            lifecycleScope.launch {
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
        rv?.showShimmerAdapter()
        rv.adapter = adapter

        rv.afterMeasured {
            if (this@VideosFragment::adapter.isInitialized)
                loadNativeAds(this@VideosFragment.adapter, this@VideosFragment::insertAdsInStoryItems)
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
                s.endsWith(".mp4") || s.endsWith(".gif")
            }

            withContext(Dispatchers.Main){
                if (files != null && files.isNotEmpty()) {
                    hasStories()


                    for (file in files.sortedBy { it.lastModified() }.reversed()) {
                        val story = Story(K.TYPE_VIDEO, file.absolutePath)
                        adapter.addStory(story)
                    }
                    rv?.hideShimmerAdapter()

                } else {
                    noStories()
                }
            }
        }

    }

   /* private fun loadStoriesGB(fileName: String) {
        if (!storagePermissionGranted()) {
            requestStoragePermission()
            return
        }

        val dir = File(fileName)

        doAsync {
            val files = dir.listFiles { _, s ->
                s.endsWith(".mp4") || s.endsWith(".gif")
            }

            uiThread {

                if (files != null && files.isNotEmpty()) {
                    hasStories()


                    for (file in files.sortedBy { it.lastModified() }.reversed()) {
                        val story = Story(K.TYPE_VIDEO, file.absolutePath)
                        adapter.addStory(story)
                    }

                    rv.hideShimmerAdapter()

                } else {
                    noStories()
                }
            }

        }

    }*/

    private fun noStories() {
        rv?.hideView()
        my_template_bottom?.hideView()
        videoEmptyView?.showView()
    }

    private fun hasStories() {
        videoEmptyView?.hideView()
        rv?.showView()
    }

    override fun onStoryClicked(v: View, story: Story) {
        val overview = StoryOverview(requireActivity(), story, vm)
        overview.show()

        //adBizLogicVideo(mRewardedVideoAd, story, sharedPrefsEditor, sharedPrefs)

    }

    //region VIDEO_REWARDED IMPLEMENTATION
    override fun onRewarded(reward: RewardItem) {
        // Reward the user. no reward

    }

    override fun onRewardedVideoAdLeftApplication() {
        Timber.tag("AdUtil").d("onRewardedVideoAdLeftApplication")
    }

    override fun onRewardedVideoAdClosed() {
        Timber.tag("AdUtil").d("onRewardedVideoAdClosed")
        //good place to reload
        initLoadVideoAdUnit(mRewardedVideoAd, requireActivity())
    }

    override fun onRewardedVideoAdFailedToLoad(errorCode: Int) {
        Timber.tag("AdUtil").d("onRewardedVideoAdFailedToLoad $errorCode")

    }

    override fun onRewardedVideoAdLoaded() {
        Timber.tag("AdUtil").d("onRewardedVideoAdLoaded")
    }

    override fun onRewardedVideoAdOpened() {
        Timber.tag("AdUtil").d("onRewardedVideoAdOpened")
    }

    override fun onRewardedVideoStarted() {
        Timber.tag("AdUtil").d("onRewardedVideoStarted")
    }

    override fun onRewardedVideoCompleted() {
        Timber.tag("AdUtil").d("onRewardedVideoCompleted")
        //good place to reload
        initLoadVideoAdUnit(mRewardedVideoAd, requireActivity())
    }


    //endregion

    override fun onPause() {
        super.onPause()
        mRewardedVideoAd.pause(activity)
    }

    override fun onResume() {
        super.onResume()
        mRewardedVideoAd.resume(activity)
    }

    override fun onDestroy() {
        super.onDestroy()
        mRewardedVideoAd.destroy(activity)
    }
}
