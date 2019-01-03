package com.job.whatsappstories.commoners

import android.support.multidex.MultiDexApplication
import timber.log.Timber

class Application : MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()

        Timber.plant(Timber.DebugTree())
    }
}