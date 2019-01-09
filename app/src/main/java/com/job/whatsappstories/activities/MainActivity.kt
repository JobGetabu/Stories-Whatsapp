package com.job.whatsappstories.activities

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.support.annotation.ColorInt
import android.support.annotation.ColorRes
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import cn.jzvd.JZVideoPlayer
import com.google.android.gms.ads.InterstitialAd
import com.job.whatsappstories.R
import com.job.whatsappstories.commoners.AppUtils
import com.job.whatsappstories.commoners.BaseActivity
import com.job.whatsappstories.fragments.WhatsFragment
import com.job.whatsappstories.menu.DrawerAdapter
import com.job.whatsappstories.menu.DrawerItem
import com.job.whatsappstories.menu.SimpleItem
import com.job.whatsappstories.menu.SpaceItem
import com.job.whatsappstories.utils.*
import com.yarolegovich.slidingrootnav.SlidingRootNav
import com.yarolegovich.slidingrootnav.SlidingRootNavBuilder
import kotlinx.android.synthetic.main.home_main.*
import kotlinx.android.synthetic.main.menu_left_drawer.*
import org.jetbrains.anko.toast
import timber.log.Timber
import java.util.*

class MainActivity : BaseActivity(), DrawerAdapter.OnItemSelectedListener {
    private var doubleBackToExit = false
    private lateinit var adapter: PagerAdapter
    private lateinit var mInterstitialAd: InterstitialAd
    private lateinit var slidingRootNav: SlidingRootNav
    private lateinit var screenTitles: Array<String>
    private lateinit var screenIcons: Array<Drawable?>


    companion object {
        private const val STATUS = 0
        private const val BUSINESS_STATUS = 1
        private const val RATE = 3
        private const val REMOVE_ADS = 4
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home_main)

        setSupportActionBar(toolbar)
        supportActionBar?.title = getString(R.string.app_name)
        mInterstitialAd = InterstitialAd(this)
        initLoadAdUnit(mInterstitialAd, this)
        adBizListner(mInterstitialAd)

        setupSliderDrawer(savedInstanceState)

    }

    private fun setupSliderDrawer(savedInstanceState: Bundle?) {
        setSupportActionBar(toolbar)
        supportActionBar?.title = getString(R.string.app_name)


        slidingRootNav = SlidingRootNavBuilder(this)
                .withToolbarMenuToggle(toolbar)
                .withMenuOpened(false)
                .withContentClickableWhenMenuOpened(false)
                .withSavedState(savedInstanceState)
                .withMenuLayout(R.layout.menu_left_drawer)
                .inject()


        screenIcons = loadScreenIcons()
        screenTitles = loadScreenTitles()

        val adapter = DrawerAdapter(Arrays.asList(
                createItemFor(STATUS).setChecked(true),
                createItemFor(BUSINESS_STATUS),
                SpaceItem(24),
                createItemFor(RATE),
                createItemFor(REMOVE_ADS)))

        adapter.setListener(this)


        drawerList.isNestedScrollingEnabled = false
        drawerList.layoutManager = LinearLayoutManager(this)
        drawerList.adapter = adapter

        adapter.setSelected(STATUS)


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
        }

        return true
    }

    override fun onBackPressed() {
        if (JZVideoPlayer.backPress()) {
            return
        }

        if (doubleBackToExit) {
            super.onBackPressed()
            displayImgAd(mInterstitialAd)

        } else {
            toast("Please tap back again to exit")

            doubleBackToExit = true

            Handler().postDelayed({ doubleBackToExit = false }, 1500)
        }
    }

    fun refreshPages() {
        adapter.notifyDataSetChanged()
        Timber.d("Refresh works :)")
    }

    override fun onItemSelected(position: Int) {

        slidingRootNav.closeMenu()

        when (position) {
            STATUS -> showFragment(WhatsFragment.createFor(screenTitles[position]))
            BUSINESS_STATUS -> toast("WA Business selected")
            REMOVE_ADS -> toast("Perform purchase")
            RATE -> {
                toast("Love this app give us a 5 star rating", Toast.LENGTH_LONG)
                AppUtils.rateApp(this)
            }
        }
    }

    private fun showFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
                .replace(R.id.container, fragment)
                .commit()
    }

    private fun createItemFor(position: Int): DrawerItem<*> {
        return SimpleItem(screenIcons[position], screenTitles[position])
                .withIconTint(color(R.color.tabsText))
                .withTextTint(color(R.color.tabsText))
                .withSelectedIconTint(color(R.color.colorAccent))
                .withSelectedTextTint(color(R.color.colorAccent))
    }

    private fun loadScreenTitles(): Array<String> {
        return resources.getStringArray(R.array.ld_activityScreenTitles)
    }

    private fun loadScreenIcons(): Array<Drawable?> {
        val ta = resources.obtainTypedArray(R.array.ld_activityScreenIcons)
        val icons = arrayOfNulls<Drawable>(ta.length())
        for (i in 0 until ta.length()) {
            val id = ta.getResourceId(i, 0)
            if (id != 0) {
                icons[i] = ContextCompat.getDrawable(this, id)
            }
        }
        ta.recycle()
        return icons
    }

    @ColorInt
    private fun color(@ColorRes res: Int): Int {
        return ContextCompat.getColor(this, res)
    }
}
