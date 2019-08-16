package com.job.whatsappstories.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.job.whatsappstories.R
import com.job.whatsappstories.commoners.BaseFragment
import com.job.whatsappstories.utils.AppExecutors
import com.job.whatsappstories.utils.PagerAdapter
import com.job.whatsappstories.viewmodel.WhatsModel
import timber.log.Timber

class WhatsFragment : BaseFragment(), TabLayout.OnTabSelectedListener {
    private var doubleBackToExit = false
    private lateinit var adapter: PagerAdapter
    private lateinit var childFragManager: FragmentManager
    private lateinit var vp: ViewPager
    private lateinit var tabs: TabLayout

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
        childFragManager = childFragmentManager
        val v = inflater.inflate(R.layout.activity_main, container, false)
        vp = v.findViewById(R.id.viewpager)
        tabs = v.findViewById(R.id.tabs)
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()

        val model = activity?.run {
            ViewModelProviders.of(this).get(WhatsModel::class.java)
        } ?: throw Exception("Invalid Activity")
        fragObserver(model)
    }

    private fun fragObserver(model: WhatsModel) {

        model.getRefresh().observe(this, Observer {

            if(it!! && ::adapter.isInitialized) adapter.notifyDataSetChanged()
        })
    }

    private fun initViews() {
        AppExecutors().mainThread().execute {
            setupViewPager()
            setupTabs()
        }
    }

    //region SETUP TABS

    private fun setupViewPager() {
        adapter = PagerAdapter(childFragManager, context)
        val images = ImagesFragment()
        val videos = VideosFragment()
        val saved = SavedFragment()

        adapter.addAllFrags(images, videos, saved)
        adapter.addAllTitles(IMAGES, VIDEOS, SAVED)
        vp.offscreenPageLimit = 2
        vp.adapter = adapter
        vp.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabs))

    }


    private fun setupTabs() {
        tabs.setupWithViewPager(vp)
        tabs.addOnTabSelectedListener(this)
    }

    override fun onTabReselected(tab: TabLayout.Tab?) {

    }

    override fun onTabUnselected(tab: TabLayout.Tab?) {

    }

    override fun onTabSelected(tab: TabLayout.Tab?) {
        vp.setCurrentItem(tab!!.position, true)
    }

    fun refreshPages(){
        adapter.notifyDataSetChanged()
        Timber.d("Refresh works :)")
    }

    //endregion
}
