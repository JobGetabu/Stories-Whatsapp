package com.job.whatsappstories.utils

import android.app.Activity
import android.content.SharedPreferences
import android.content.pm.PackageManager
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.ads.reward.RewardedVideoAd
import com.job.whatsappstories.BuildConfig
import com.job.whatsappstories.R
import com.job.whatsappstories.models.Story
import timber.log.Timber



/**
 * Created by Job on Friday : 1/4/2019.
 */


fun multipleOfTwo(num: Int) = num % 2 == 0

fun multipleOfFive(num: Int) = num % 5 == 0

fun multipleOfSeven(num: Int) = num % 7 == 0

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

fun initLoadVideoAdUnit(mRewardedVideoAd: RewardedVideoAd, activity: Activity) {
    with(mRewardedVideoAd) {
        if (BuildConfig.DEBUG) {
            loadAd(activity.getString(R.string.test_video_rewarded_ad),
                    AdRequest.Builder().build())

        } else {
            loadAd(activity.getString(R.string.production_video_rewarded_ad),
                    AdRequest.Builder().build())

        }
    }
}

fun displayImgAd(mInterstitialAd: InterstitialAd) {
    if (mInterstitialAd.isLoaded) {
        mInterstitialAd.show()
    } else {
        Timber.tag("AdUtil").d("The interstitial wasn't loaded yet.")
    }
}

fun displayVideoAd(mRewardedVideoAd: RewardedVideoAd) {
    if (mRewardedVideoAd.isLoaded) {
        mRewardedVideoAd.show()
    }
}



fun adBizLogicImg(mInterstitialAd: InterstitialAd, story: Story,
               sharedPrefsEditor: SharedPreferences.Editor, sharedPrefs: SharedPreferences) {
    val imgClickCount: Int = sharedPrefs.getInt(Constants.IMAGE_SAVE_CLICKS, 0)
    val vidClickCount: Int = sharedPrefs.getInt(Constants.VIDEO_SAVE_CLICKS, 0)


    if (story.type == 0) {
        val resultImg: Int = imgClickCount + 1
        sharedPrefsEditor.putInt(Constants.IMAGE_SAVE_CLICKS, resultImg)

        Timber.tag("AdUtil").d("imgClickCount = ${imgClickCount} story.type = ${story.type}")
        if (multipleOfFive(resultImg)) displayImgAd(mInterstitialAd)

    } else {
        val resultVid: Int = vidClickCount + 1
        sharedPrefsEditor.putInt(Constants.VIDEO_SAVE_CLICKS, resultVid)

        Timber.tag("AdUtil").d("vidClickCount = $vidClickCount   story.type = ${story.type}")
        if (multipleOfFive(resultVid)) displayImgAd(mInterstitialAd)
    }
    sharedPrefsEditor.apply()
}

fun adBizLogicVideo(mRewardedVideoAd: RewardedVideoAd, story: Story,
                  sharedPrefsEditor: SharedPreferences.Editor, sharedPrefs: SharedPreferences) {

    val vidClickCount: Int = sharedPrefs.getInt(Constants.VIDEO_SAVE_CLICKS, 0)

    if (story.type == 1) {
        val resultVid: Int = vidClickCount + 1
        sharedPrefsEditor.putInt(Constants.VIDEO_SAVE_CLICKS, resultVid)

        Timber.tag("AdUtil").d("vidClickCount = $vidClickCount   story.type = ${story.type}")
        if (multipleOfSeven(resultVid)) displayVideoAd(mRewardedVideoAd)

    }
    sharedPrefsEditor.apply()
}

fun adBizListner(mInterstitialAd: InterstitialAd) {
    mInterstitialAd.adListener = object : AdListener() {
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

fun isPackageInstalled(packageName: String, packageManager: PackageManager): Boolean {
    try {
        return packageManager.getApplicationInfo(packageName, 0).enabled
    } catch (e: PackageManager.NameNotFoundException) {
        return false
    }
}