package com.job.whatsappstories.fragments


import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.GridLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.job.whatsappstories.R
import com.job.whatsappstories.adapters.StoriesAdapter
import com.job.whatsappstories.callbacks.StoryCallback
import com.job.whatsappstories.commoners.BaseFragment
import com.job.whatsappstories.commoners.K
import com.job.whatsappstories.commoners.StoryOverview
import com.job.whatsappstories.models.Story
import com.job.whatsappstories.utils.RecyclerFormatter
import com.job.whatsappstories.utils.hideView
import com.job.whatsappstories.utils.multipleOfTwo
import com.job.whatsappstories.utils.showView
import kotlinx.android.synthetic.main.fragment_images.*
import kotlinx.android.synthetic.main.image_empty.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import timber.log.Timber
import java.io.File


class ImagesFragment : BaseFragment(), StoryCallback {
    private lateinit var adapter: StoriesAdapter
    private lateinit var sharedPrefs: SharedPreferences
    private lateinit var sharedPrefsEditor: SharedPreferences.Editor
    private var refreshing = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_images, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
        loadStories()
        sharedPrefs = activity!!.getSharedPreferences(activity?.applicationContext?.packageName,MODE_PRIVATE)
        sharedPrefsEditor = activity!!.getSharedPreferences(activity?.applicationContext?.packageName,MODE_PRIVATE).edit()


        testMyAdUtil()

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
        if (!dir.exists())
            dir.mkdirs()

        doAsync {
            val files = dir.listFiles { _, s ->
                s.endsWith(".png") || s.endsWith(".jpg") || s.endsWith(".jpeg") }

            uiThread {

                if (files.isNotEmpty()) {
                    hasStories()

                    if (refreshing) adapter.clearStories()

                    for (file in files.sortedBy { it.lastModified() }.reversed()) {
                        val story = Story(K.TYPE_IMAGE, file.absolutePath)
                        adapter.addStory(story)
                    }

                    refreshing = false
                } else {
                    noStories()
                }
            }

        }

    }

    private fun noStories() {
        rv?.hideView()
        imageEmptyView?.showView()
    }

    private fun hasStories() {
        imageEmptyView?.hideView()
        rv?.showView()
    }

    override fun onStoryClicked(v: View, story: Story) {
        val overview = StoryOverview(activity!!, story)
        overview.show()
    }

    private fun testMyAdUtil(){

        val numList = 1..20

        for (n in numList){
            Timber.tag("nums").d("is $n divisible by 2 ${multipleOfTwo(n)}")
            //Timber.d("is $n divisible by 5 ${multipleOfFive(n)}")

        }

    }

}
