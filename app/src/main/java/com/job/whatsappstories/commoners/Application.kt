package com.job.whatsappstories.commoners

import androidx.appcompat.app.AppCompatDelegate
import androidx.multidex.MultiDexApplication
import com.google.android.gms.ads.MobileAds
import com.job.whatsappstories.BuildConfig
import com.job.whatsappstories.R
import com.job.whatsappstories.utils.PreferenceHelper.customPrefs
import timber.log.Timber





class Application : MultiDexApplication() {

    private val base64PubKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAgfguVJq/jGwcejjXTdQEiDW9hJ9TV60+/dNm9Qd9mRXqRyU6GtPkLcP8qW7WAUxmWGpWj88In3tM+NUp1bdX+wKAp2FvegwYucosoBFNzfjfyfpmhJxMYqHBgnIgvDn7JbOjOzAO7YPizo9amqI8uOdjtgMSUfA04LNGVOcauDW6lLYFq44SvDEel7x5zAJJ8kCNS2eoCWPzRjhpz5JnGeUx6XRs0QxJ2w3YtxSRH/5aiU+m0oX8tM1yiBNxtLv6eN5lnHNCmPILhHTqT4ZLwVScg6AypQ0wGfjmukN2W61pND4pHJEug4UV4s7jHgyjYlZgNP2aRyUyvjTaMWMzrQIDAQAB"

    init
    {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
    }

    companion object {
        lateinit var instance: Application
    }

    override fun onCreate() {
        super.onCreate()

        instance = this
        Timber.plant(Timber.DebugTree())

        //initializing AdMob account
        MobileAds.initialize(this, getString(R.string.admob_app_id))

        //vector enabled for > 19
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)


    }

    fun getApp() = instance

    fun getPrefs() = customPrefs(this)
}