package com.job.whatsappstories.activities

import android.content.Intent
import android.content.IntentSender
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import cn.jzvd.JZVideoPlayer
import com.google.android.gms.ads.InterstitialAd
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallState
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType.IMMEDIATE
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.firebase.auth.FirebaseAuth
import com.job.whatsappstories.R
import com.job.whatsappstories.commoners.*
import com.job.whatsappstories.fragments.WhatsFragment
import com.job.whatsappstories.menu.DrawerAdapter
import com.job.whatsappstories.menu.DrawerItem
import com.job.whatsappstories.menu.SimpleItem
import com.job.whatsappstories.menu.SpaceItem
import com.job.whatsappstories.utils.*
import com.job.whatsappstories.utils.Constants.IS_PRO_USER
import com.job.whatsappstories.utils.Constants.USER_UID
import com.job.whatsappstories.viewmodel.WhatsModel
import com.yarolegovich.slidingrootnav.SlidingRootNav
import com.yarolegovich.slidingrootnav.SlidingRootNavBuilder
import kotlinx.android.synthetic.main.home_main.*
import kotlinx.android.synthetic.main.menu_left_drawer.*
import org.jetbrains.anko.toast
import timber.log.Timber
import java.util.*

class MainActivity : BaseActivity(), DrawerAdapter.OnItemSelectedListener, InstallStateUpdatedListener {

    private var doubleBackToExit = false
    private lateinit var adapter: PagerAdapter
    private lateinit var mInterstitialAd: InterstitialAd
    private lateinit var slidingRootNav: SlidingRootNav
    private lateinit var screenTitles: Array<String>
    private lateinit var screenIcons: Array<Drawable?>
    private lateinit var model: WhatsModel
    private lateinit var auth: FirebaseAuth
    private lateinit var appUpdateManager: AppUpdateManager


    companion object {
        private const val UPDATE_REQUEST_CODE = 108
        private const val STATUS = 0
        private const val BUSINESS_STATUS = 1
        private const val RATE = 3
        private const val REMOVE_ADS = 40
        private const val REFERRAL = 4
        private const val SKU_REMOVE_ADS = "remove_ad"
    }

    override fun onStart() {
        super.onStart()
        if (auth.currentUser == null) signIn()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home_main)
        setSupportActionBar(toolbar)

        model = ViewModelProviders.of(this).get(WhatsModel::class.java)

        supportActionBar?.title = getString(R.string.app_name)
        mInterstitialAd = InterstitialAd(this)
        initLoadAdUnit(mInterstitialAd, this)
        adBizListner(mInterstitialAd)


        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        isUserPro(savedInstanceState)

        //app update global init
        appUpdateManager = AppUpdateManagerFactory.create(this)
        appUpdateManager.registerListener(this)
        appUpdatePrep()
    }

    private fun refreshStatus() {
        model.setRefresh(true)
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

        val adapter = DrawerAdapter(listOf(
                createItemFor(STATUS).setChecked(true),
                createItemFor(BUSINESS_STATUS),
                SpaceItem(24),
                createItemFor(RATE),
                //createItemFor(REMOVE_ADS),
                createItemFor(REFERRAL)))

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
        when (item.itemId) {

            R.id.share_app -> AppUtils.shareApp(this)

            R.id.menu_refresh -> refreshStatus()
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

    override fun onItemSelected(position: Int) {

        slidingRootNav.closeMenu()

        when (position) {
            STATUS -> {

                if (isPackageInstalled(Constants.WHATAPP_PACKAGE_NAME, packageManager)) {

                    model.setCurrentFile(K.WHATSAPP_STORIES)
                } else {

                    model.setCurrentFile(K.GBWHATSAPP_STORIES)
                }

                showFragment(WhatsFragment.createFor(screenTitles[position]))
            }
            BUSINESS_STATUS -> {
                model.setCurrentFile(K.WHATSAPP_BUSINESS_STORIES)
                if (isPackageInstalled(Constants.WHATAPP_BUSINESS_PACKAGE_NAME, packageManager)) {
                    model.setCurrentFile(K.WHATSAPP_BUSINESS_STORIES)

                } else {
                    toast(getString(R.string.WA_Biz_not_installed), Toast.LENGTH_LONG)
                }
            }

            REMOVE_ADS -> {

                val userPrefs = Application.instance.getPrefs()
                val isPro = userPrefs.getBoolean(IS_PRO_USER, false)

                if (!isPro) {

                } else {
                    //is pro do REFERRAL
                    val referDialogue = ReferDialogue(this)
                    referDialogue.show()

                    toast("Earn with referrals")
                }
            }

            REFERRAL -> {
                val referDialogue = ReferDialogue(this)
                referDialogue.show()

                toast("Earn with referrals")
            }
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

    private fun signIn() {
        auth.signInAnonymously()
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Timber.d("signInAnonymously:success")
                        val user = auth.currentUser
                        val userPrefsEditor = PreferenceHelper.customPrefs(this).edit()
                        userPrefsEditor.putString(USER_UID, user?.uid)
                        userPrefsEditor.apply()

                        createAccount(user!!.uid)

                    } else {
                        // If sign in fails, display a message to the user.
                        Timber.e(task.exception, "signInAnonymously:failure")
                        Timber.d("Authentication failed.")
                    }

                }
    }

    override fun onDestroy() {
        super.onDestroy()
        appUpdateManager.unregisterListener(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == UPDATE_REQUEST_CODE) {
            if (resultCode != RESULT_OK) {
                Timber.d("Update flow failed! Result code: $resultCode");
                // If the update is cancelled or fails,
                // you can request to start the update again.
            }
        }
    }

    private fun isUserPro(savedInstanceState: Bundle?) {
        val userPrefs = Application.instance.getPrefs()
        val isPro = userPrefs.getBoolean(IS_PRO_USER, false)

        if (!isPro) setupSliderDrawer(savedInstanceState)
        else setupSliderDrawerPro(savedInstanceState)
    }

    private fun setupSliderDrawerPro(savedInstanceState: Bundle?) {
        setSupportActionBar(toolbar)
        supportActionBar?.title = getString(R.string.app_name_pro)


        slidingRootNav = SlidingRootNavBuilder(this)
                .withToolbarMenuToggle(toolbar)
                .withMenuOpened(false)
                .withContentClickableWhenMenuOpened(false)
                .withSavedState(savedInstanceState)
                .withMenuLayout(R.layout.menu_left_drawer)
                .inject()


        screenIcons = loadScreenIconsPro(this)
        screenTitles = loadScreenTitlesPro(this)

        val adapter = DrawerAdapter(Arrays.asList(
                createItemFor(STATUS).setChecked(true),
                createItemFor(BUSINESS_STATUS),
                SpaceItem(24),
                createItemFor(RATE),
                createItemFor(4)))

        adapter.setListener(this)


        drawerList.isNestedScrollingEnabled = false
        drawerList.layoutManager = LinearLayoutManager(this)
        drawerList.adapter = adapter

        adapter.setSelected(STATUS)
    }

    //region UPDATE APP CODE
    private fun appUpdatePrep() {

        val appUpdateInfoTask = appUpdateManager.appUpdateInfo

        appUpdateInfoTask.addOnSuccessListener {

        }

        appUpdateInfoTask.addOnFailureListener { e -> Timber.e(e, "UPDATE_NOT_AVAILABLE") }
    }

    private fun startUpdateFlow(appUpdateInfo: AppUpdateInfo) {
        try {
            appUpdateManager.startUpdateFlowForResult(
                    // Pass the intent that is returned by 'getAppUpdateInfo()'.
                    appUpdateInfo,
                    // Or 'AppUpdateType.FLEXIBLE' for flexible updates.
                    IMMEDIATE,
                    // The current activity making the update request.
                    this,
                    // Include a request code to later monitor this update request.
                    UPDATE_REQUEST_CODE
            )
        } catch (e: IntentSender.SendIntentException) {
            Timber.e(e)
        }
    }

    override fun onStateUpdate(state: InstallState) {
        if (state.installStatus() == InstallStatus.DOWNLOADED) {
            // After the update is downloaded, show a notification
            // and request user confirmation to restart the app.

            Snackbar.make(findViewById(android.R.id.content), "An update has just been downloaded.",
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction("RESTART") {
                        appUpdateManager.completeUpdate()
                    }
        }
    }

    private fun checkIfUpdateWasUnderWay() {
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo
        appUpdateInfoTask.addOnSuccessListener {
            if (it.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS
                    && it.isUpdateTypeAllowed(IMMEDIATE)) {
                // If an in-app update is already running, resume the update.
                startUpdateFlow(it)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        checkIfUpdateWasUnderWay()
    }

    //endregion

}
