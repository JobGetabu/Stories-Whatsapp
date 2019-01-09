package com.job.whatsappstories.activities

import android.os.Bundle
import android.os.Handler
import android.support.design.widget.TabLayout
import android.view.Menu
import android.view.MenuItem
import cn.jzvd.JZVideoPlayer
import com.job.whatsappstories.R
import com.job.whatsappstories.commoners.AppUtils
import com.job.whatsappstories.commoners.BaseActivity
import com.job.whatsappstories.fragments.ImagesFragment
import com.job.whatsappstories.fragments.SavedFragment
import com.job.whatsappstories.fragments.VideosFragment
import com.job.whatsappstories.utils.PagerAdapter
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.toast
import timber.log.Timber

class MainActivity : BaseActivity(), TabLayout.OnTabSelectedListener {
    private var doubleBackToExit = false
    private lateinit var adapter: PagerAdapter

    companion object {
        private const val IMAGES = "IMAGES"
        private const val VIDEOS = "VIDEOS"
        private const val SAVED = "SAVED"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
    }

    private fun initViews() {
        setSupportActionBar(toolbar)
        supportActionBar?.title = getString(R.string.app_name)

        setupViewPager()
        setupTabs()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.home_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        super.onOptionsItemSelected(item)
        val id = item.itemId
        when (id) {

            R.id.share_app -> AppUtils.shareApp(this)
        }//sendToAdvertiseClass();

        return true
    }


    private fun setupViewPager() {
        adapter = PagerAdapter(supportFragmentManager, this)
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

    override fun onBackPressed() {
        if (JZVideoPlayer.backPress()) {
            return
        }

        if (doubleBackToExit) {
            super.onBackPressed()
        } else {
            toast("Please tap back again to exit")

            doubleBackToExit = true

            Handler().postDelayed({doubleBackToExit = false }, 1500)
        }
    }

    fun refreshPages(){
        adapter.notifyDataSetChanged()
        Timber.d("Refresh works :)")
    }
}
