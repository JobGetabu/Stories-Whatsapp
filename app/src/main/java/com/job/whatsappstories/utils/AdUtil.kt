package com.job.whatsappstories.utils

import android.app.Activity
import android.content.SharedPreferences
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.job.whatsappstories.BuildConfig
import com.job.whatsappstories.R
import com.job.whatsappstories.models.Story
import timber.log.Timber

/**
 * Created by Job on Friday : 1/4/2019.
 */


fun multipleOfTwo(num: Int) = num % 2 == 0

fun multipleOfFive(num: Int) = num % 5 == 0

fun initLoadAdUnit(mInterstitialAd: InterstitialAd, activity: Activity) {
    with(mInterstitialAd) {
        adUnitId = if (BuildConfig.DEBUG) {
            activity.getString(R.string.test_unit_ad)

        } else {

            activity.getString(R.string.production_unit_ad)
        }

        loadAd(AdRequest.Builder().build())
    }
}

fun displayAd(mInterstitialAd: InterstitialAd) {
    if (mInterstitialAd.isLoaded) {
        mInterstitialAd.show()
    } else {
        Timber.tag("AdUtil").d("The interstitial wasn't loaded yet.")
    }
}

fun adBizLogic(mInterstitialAd: InterstitialAd, story: Story,
               sharedPrefsEditor: SharedPreferences.Editor, sharedPrefs: SharedPreferences) {
    val imgClickCount: Int = sharedPrefs.getInt(Constants.IMAGE_SAVE_CLICKS, 0)
    val vidClickCount: Int = sharedPrefs.getInt(Constants.VIDEO_SAVE_CLICKS, 0)

    Timber.tag("AdUtil").d("imgClickCount = ${imgClickCount} story.type = ${story.type}")
    //Timber.tag("AdUtil").d("vidClickCount = $vidClickCount   story.type = ${story.type}")

    if (story.type == 0) {
        val resultImg: Int = imgClickCount + 1
        sharedPrefsEditor.putInt(Constants.IMAGE_SAVE_CLICKS, resultImg)

        if (multipleOfFive(resultImg)) displayAd(mInterstitialAd)

    } else {
        val resultVid: Int = vidClickCount + 1
        sharedPrefsEditor.putInt(Constants.VIDEO_SAVE_CLICKS, resultVid)

        if (multipleOfFive(resultVid)) displayAd(mInterstitialAd)
    }
    sharedPrefsEditor.apply()
}

fun adBizListner(mInterstitialAd: InterstitialAd){
    mInterstitialAd.adListener = object: AdListener() {
        override fun onAdLoaded() {
            // Code to be executed when an ad finishes loading.
        }

        override fun onAdFailedToLoad(errorCode: Int) {
            // Code to be executed when an ad request fails.
            // keep retrying
            mInterstitialAd.loadAd(AdRequest.Builder().build())
        }

        override fun onAdOpened() {
            // Code to be executed when the ad is displayed.
        }

        override fun onAdLeftApplication() {
            // Code to be executed when the user has left the app.
        }

        override fun onAdClosed() {
            // Code to be executed when when the interstitial ad is closed.
            mInterstitialAd.loadAd(AdRequest.Builder().build())
        }
    }
}