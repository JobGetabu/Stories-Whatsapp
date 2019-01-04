package com.job.whatsappstories.fragments


import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.GridLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import kotlinx.android.synthetic.main.fragment_videos.*
import kotlinx.android.synthetic.main.video_empty.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import timber.log.Timber
import java.io.File

class VideosFragment : BaseFragment(), StoryCallback, RewardedVideoAdListener {

    private lateinit var adapter: StoriesAdapter
    private lateinit var mRewardedVideoAd: RewardedVideoAd
    private lateinit var sharedPrefs: SharedPreferences
    private lateinit var sharedPrefsEditor: SharedPreferences.Editor

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_videos, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
        val dirGB = File(K.GBWHATSAPP_STORIES)
        val dir = File(K.WHATSAPP_STORIES)

        if (!dir.exists()) {
            if (dirGB.exists()) loadStoriesGB()

        }else if(!dirGB.exists()) {
            if (dir.exists()) loadStories()
        }

        sharedPrefs = activity!!.getSharedPreferences(activity?.applicationContext?.packageName, Context.MODE_PRIVATE)
        sharedPrefsEditor = activity!!.getSharedPreferences(activity?.applicationContext?.packageName, Context.MODE_PRIVATE).edit()

        mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(activity)
        mRewardedVideoAd.rewardedVideoAdListener = this

        initLoadVideoAdUnit(mRewardedVideoAd,activity!!)
    }

    private fun initViews() {
        rv.setHasFixedSize(true)
        rv.layoutManager = GridLayoutManager(activity!!, 3)
        rv.addItemDecoration(RecyclerFormatter.GridItemDecoration(activity!!, 3, 5))
        rv.itemAnimator = DefaultItemAnimator()
        (rv.itemAnimator as DefaultItemAnimator).supportsChangeAnimations = false

        adapter = StoriesAdapter(this, activity!!)
        rv.adapter = adapter

    }

    private fun loadStories() {
        if (!storagePermissionGranted()) {
            requestStoragePermission()
            return
        }

        val dir = File(K.WHATSAPP_STORIES)

        doAsync {
            val files = dir.listFiles { _, s ->
                s.endsWith(".mp4") || s.endsWith(".gif") }

            uiThread {

                if (files.isNotEmpty()) {
                    hasStories()


                    for (file in files.sortedBy { it.lastModified() }.reversed()) {
                        val story = Story(K.TYPE_VIDEO, file.absolutePath)
                        adapter.addStory(story)
                    }

                } else {
                    noStories()
                }
            }

        }

    }

    private fun loadStoriesGB() {
        if (!storagePermissionGranted()) {
            requestStoragePermission()
            return
        }

        val dir = File(K.GBWHATSAPP_STORIES)

        doAsync {
            val files = dir.listFiles { _, s ->
                s.endsWith(".mp4") || s.endsWith(".gif") }

            uiThread {

                if (files.isNotEmpty()) {
                    hasStories()


                    for (file in files.sortedBy { it.lastModified() }.reversed()) {
                        val story = Story(K.TYPE_VIDEO, file.absolutePath)
                        adapter.addStory(story)
                    }

                } else {
                    noStories()
                }
            }

        }

    }

    private fun noStories() {
        rv?.hideView()
        videoEmptyView?.showView()
    }

    private fun hasStories() {
        videoEmptyView?.hideView()
        rv?.showView()
    }

    override fun onStoryClicked(v: View, story: Story) {
        val overview = StoryOverview(activity!!, story)
        overview.show()

        adBizLogicVideo(mRewardedVideoAd,story, sharedPrefsEditor,sharedPrefs)

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
        initLoadVideoAdUnit(mRewardedVideoAd,activity!!)
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
        initLoadVideoAdUnit(mRewardedVideoAd,activity!!)
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
