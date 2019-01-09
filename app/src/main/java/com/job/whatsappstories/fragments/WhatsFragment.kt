package com.job.whatsappstories.fragments

import android.os.Bundle
import android.support.design.widget.TabLayout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.job.whatsappstories.R
import com.job.whatsappstories.commoners.BaseFragment
import com.job.whatsappstories.utils.PagerAdapter
import kotlinx.android.synthetic.main.activity_main.*
import timber.log.Timber

class WhatsFragment : BaseFragment(), TabLayout.OnTabSelectedListener {
    private var doubleBackToExit = false
    private lateinit var adapter: PagerAdapter

     companion object {
         private const val IMAGES = "IMAGES"
         private const val VIDEOS = "VIDEOS"
         private const val SAVED = "SAVED"

         private const val EXTRA_FILE_TEXT = "EXTRA_FILE_TEXT"

         fun createFor(text: String): WhatsFragment {
             val fragment = WhatsFragment()
             val args = Bundle()
             args.putString(EXTRA_FILE_TEXT, text)
             fragment.setArguments(args)
             return fragment
         }
     }



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.activity_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
    }

    private fun initViews() {

        setupViewPager()
        setupTabs()
    }

    //region SETUP TABS

    private fun setupViewPager() {
        adapter = PagerAdapter(childFragmentManager, context)
        val images = ImagesFragment()
        val videos = VideosFragment()
        val saved = SavedFragment()

        adapter.addAllFrags(images, videos, saved)
        adapter.addAllTitles(IMAGES, VIDEOS, SAVED)
        viewpager.offscreenPageLimit = 2
        viewpager.adapter = adapter
        viewpager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabs))

    }


    private fun setupTabs() {
        tabs.setupWithViewPager(viewpager)
        tabs.addOnTabSelectedListener(this)
    }

    override fun onTabReselected(tab: TabLayout.Tab?) {

    }

    override fun onTabUnselected(tab: TabLayout.Tab?) {

    }

    override fun onTabSelected(tab: TabLayout.Tab?) {
        viewpager.setCurrentItem(tab!!.position, true)
    }

    fun refreshPages(){
        adapter.notifyDataSetChanged()
        Timber.d("Refresh works :)")
    }

    //endregion
}