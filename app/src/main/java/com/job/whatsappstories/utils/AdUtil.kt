package com.job.whatsappstories.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.ads.reward.RewardedVideoAd
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.dynamiclinks.DynamicLink
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.firebase.firestore.FirebaseFirestore
import com.job.whatsappstories.BuildConfig
import com.job.whatsappstories.R
import com.job.whatsappstories.models.Story
import com.job.whatsappstories.models.User
import com.job.whatsappstories.utils.Constants.USER_COL
import org.jetbrains.anko.toast
import timber.log.Timber


/**
 * Created by Job on Friday : 1/4/2019.
 */


fun multipleOfTwo(num: Int) = num % 2 == 0

fun multipleOfThree(num: Int) = num % 3 == 0

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

        Timber.tag("AdUtil").d("imgClickCount = $imgClickCount story.type = ${story.type}")
        if (multipleOfTwo(resultImg)) displayImgAd(mInterstitialAd)

    } else {
        val resultVid: Int = vidClickCount + 1
        sharedPrefsEditor.putInt(Constants.VIDEO_SAVE_CLICKS, resultVid)

        Timber.tag("AdUtil").d("vidClickCount = $vidClickCount   story.type = ${story.type}")
        if (multipleOfTwo(resultVid)) displayImgAd(mInterstitialAd)
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
        if (multipleOfThree(resultVid)) displayVideoAd(mRewardedVideoAd)

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
    return try {
        packageManager.getApplicationInfo(packageName, 0).enabled
    } catch (e: PackageManager.NameNotFoundException) {
        false
    }
}

fun createDynamicLink(context: Context) {

    val user = FirebaseAuth.getInstance().currentUser
    if (user == null) {
        context.toast("Network connection needed")
        return
    }

    val uid = user.uid
    val link = "https://whatsappstories.page.link/get/?invitedby=$uid"
    FirebaseDynamicLinks.getInstance().createDynamicLink()
            .setLink(Uri.parse(link))
            .setDomainUriPrefix("https://whatsappstories.page.link")
            .setAndroidParameters(
                    DynamicLink.AndroidParameters.Builder("com.job.whatsappstories")
                            .setMinimumVersion(170200000)
                            .build())

            .buildShortDynamicLink()
            .addOnSuccessListener { shortDynamicLink ->
                var mInvitationUrl = shortDynamicLink.shortLink
                //var workingLink = "https://whatsappstories.page.link/?invitedby=" + uid

                val intent = Intent(Intent.ACTION_SEND)
                intent.type = "text/plain"
                intent.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.referrer_txt))
                intent.putExtra(Intent.EXTRA_TEXT, context.getString(R.string.referrer_link_txt, mInvitationUrl))
                context.startActivity(Intent.createChooser(intent, "Refer using..."))

            }
}

fun handleInvite(activity: Activity, intent: Intent){
    FirebaseDynamicLinks.getInstance()
            .getDynamicLink(intent)
            .addOnSuccessListener(activity) { pendingDynamicLinkData ->
                // Get deep link from result (may be null if no link is found)
                var deepLink: Uri? = null
                if (pendingDynamicLinkData != null) {
                    deepLink = pendingDynamicLinkData.link
                }
                //
                // If the user isn't signed in and the pending Dynamic Link is
                // an invitation, sign in the user anonymously, and record the
                // referrer's UID.
                //

                val user = FirebaseAuth.getInstance().currentUser
                if (user == null &&
                        deepLink != null &&
                        deepLink.getBooleanQueryParameter("invitedby", false)) {

                    val referrerUid = deepLink.getQueryParameter("invitedby")
                    createAnonymousAccountWithReferrerInfo(referrerUid)

                    activity.toast("invited by $referrerUid")
                    Timber.d("invited by $referrerUid")
                }

                //testing referral
                val referrerUid = deepLink?.getQueryParameter("invitedby")
                //createAnonymousAccountWithReferrerInfo(referrerUid)

                activity.toast("invited by $referrerUid")
                Timber.d("invited by $referrerUid")

            }
}

private fun createAnonymousAccountWithReferrerInfo(referrerUid: String?) {
    FirebaseAuth.getInstance()
            .signInAnonymously()
            .addOnSuccessListener {
                // Keep track of the referrer in the RTDB. Database calls
                // will depend on the structure of your app's RTDB.
                val user = FirebaseAuth.getInstance().currentUser
                //use firestore
                val myUser = User(user!!.uid,referrerUid!!,0)

                FirebaseFirestore.getInstance().collection(USER_COL)
                        .document(referrerUid)
                        .set(myUser)
                        .addOnCompleteListener { task ->
                            if (task.isComplete)  Timber.d("Account created for $referrerUid")
                            else  Timber.e(task.exception, "Account creation failure for $referrerUid")
                        }
            }
}