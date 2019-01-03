package com.job.whatsappstories.commoners

import android.support.multidex.MultiDexApplication
import com.google.android.gms.ads.MobileAds
import com.job.whatsappstories.R
import timber.log.Timber

class Application : MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()

        Timber.plant(Timber.DebugTree())

        //initializing AdMob account
        MobileAds.initialize(this, getString(R.string.admob_app_id))
    }
}