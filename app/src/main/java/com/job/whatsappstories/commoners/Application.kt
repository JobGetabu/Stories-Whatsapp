package com.job.whatsappstories.commoners

import android.support.multidex.MultiDexApplication
import android.support.v7.app.AppCompatDelegate
import com.crashlytics.android.Crashlytics
import com.google.android.gms.ads.MobileAds
import com.job.whatsappstories.BuildConfig
import com.job.whatsappstories.R
import io.fabric.sdk.android.Fabric
import timber.log.Timber



class Application : MultiDexApplication() {

    init
    {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
    }

    override fun onCreate() {
        super.onCreate()

        Timber.plant(Timber.DebugTree())

        //initializing AdMob account
        MobileAds.initialize(this, getString(R.string.admob_app_id))

        //vector enabled for > 19
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)

        if( !BuildConfig.DEBUG)  Fabric.with(this,Crashlytics())

    }
}