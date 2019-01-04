package com.job.whatsappstories.commoners

import android.support.multidex.MultiDexApplication
import com.crashlytics.android.Crashlytics
import com.google.android.gms.ads.MobileAds
import com.job.whatsappstories.BuildConfig
import com.job.whatsappstories.R
import io.fabric.sdk.android.Fabric
import timber.log.Timber

class Application : MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()

        Timber.plant(Timber.DebugTree())

        //initializing AdMob account
        MobileAds.initialize(this, getString(R.string.admob_app_id))

        if(!BuildConfig.DEBUG){
            Fabric.with(this,Crashlytics.getInstance())
        }
    }
}