package com.job.whatsappstories.fragments


import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.ads.nativetemplates.NativeTemplateStyle
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.ads.formats.UnifiedNativeAd
import com.job.whatsappstories.R
import com.job.whatsappstories.adapters.StoriesAdapter
import com.job.whatsappstories.callbacks.StoryCallback
import com.job.whatsappstories.commoners.BaseFragment
import com.job.whatsappstories.commoners.K
import com.job.whatsappstories.commoners.StoryOverview
import com.job.whatsappstories.models.Story
import com.job.whatsappstories.utils.*
import com.job.whatsappstories.viewmodel.WhatsModel
import kotlinx.android.synthetic.main.fragment_images.*
import kotlinx.android.synthetic.main.image_empty.*
import kotlinx.android.synthetic.main.native_bottom_ad.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import timber.log.Timber
import java.io.File


class ImagesFragment : BaseFragment(), StoryCallback {
    private lateinit var adapter: StoriesAdapter
    private lateinit var sharedPrefs: SharedPreferences
    private lateinit var sharedPrefsEditor: SharedPreferences.Editor
    private lateinit var fileName: String

    private lateinit var mInterstitialAd: InterstitialAd

    private var refreshing = false

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

        fragObserver(vm)

        if (activity?.intent != null) handleInvite(activity!!, activity!!.intent)

        sharedPrefs = activity!!.getSharedPreferences(activity?.applicationContext?.packageName, MODE_PRIVATE)
        sharedPrefsEditor = activity!!.getSharedPreferences(activity?.applicationContext?.packageName, MODE_PRIVATE).edit()

        mInterstitialAd = InterstitialAd(context)
        initLoadAdUnit(mInterstitialAd, activity!!)
        adBizListner(mInterstitialAd)

    }

    private fun fragObserver(model: WhatsModel) {

        model.getCurrentFile().observe(this, Observer {
            fileName = it!!

            loadStories(fileName)
            loadNativeAds(adapter, this::insertAdsInStoryItems)
        })
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
        rv?.adapter = adapter
        rv?.showShimmerAdapter()

    }

    private fun loadStories(fileName: String) {
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

    private fun loadStoriesGB(fileName: String) {
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
    }

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
        val overview = StoryOverview(activity!!, story, vm)
        overview.show()

    }

}
