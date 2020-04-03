package com.job.whatsappstories.commoners


import android.Manifest
import android.content.pm.PackageManager
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.ads.nativetemplates.NativeTemplateStyle
import com.google.android.ads.nativetemplates.TemplateView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.formats.UnifiedNativeAd
import com.job.whatsappstories.BuildConfig
import com.job.whatsappstories.R
import com.job.whatsappstories.adapters.StoriesAdapter
import com.job.whatsappstories.utils.NUMBER_OF_ADS
import com.job.whatsappstories.utils.multipleOfSeven
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import kotlinx.android.synthetic.main.native_bottom_ad.*
import org.jetbrains.anko.toast
import timber.log.Timber
import kotlin.math.ceil

open class BaseFragment : Fragment() {

    lateinit var adLoader: AdLoader
    lateinit var adLoader2: AdLoader
    val mNativeAds: MutableList<UnifiedNativeAd> = ArrayList()
    var adCounter: Int = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    // User hasn't requested storage permission; request them to allow
    fun requestStoragePermission() {
        Dexter.withActivity(activity)
                .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(object : PermissionListener {
                    override fun onPermissionGranted(response: PermissionGrantedResponse) {
                    }

                    override fun onPermissionDenied(response: PermissionDeniedResponse) {
                        activity?.toast("Storage permission is required!")
                    }

                    override fun onPermissionRationaleShouldBeShown(permission: PermissionRequest, token: PermissionToken) {
                        token.continuePermissionRequest()
                    }
                }).check()
    }

    // Check if user has granted storage permission
    fun storagePermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(activity!!, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }


    fun loadNativeAds(adapter: StoriesAdapter, insertAdsInStoryItems: (StoriesAdapter) -> Unit) {

        val builder: AdLoader.Builder =
                if (BuildConfig.DEBUG) AdLoader.Builder(requireContext(), getString(R.string.production_native_ad_test))
        else AdLoader.Builder(requireContext(), getString(R.string.production_native_ad))

        adLoader = builder.forUnifiedNativeAd { unifiedNativeAd -> // A native ad loaded successfully, check if the ad loader has finished loading
            // and if so, insert the ads into the list.
            mNativeAds.add(unifiedNativeAd)
            if (!adLoader.isLoading) {
                insertAdsInStoryItems(adapter)
            }
        }.withAdListener(
                object : AdListener() {
                    override fun onAdFailedToLoad(errorCode: Int) {
                        // A native ad failed to load, check if the ad loader has finished loading
                        // and if so, insert the ads into the list.
                        Timber.e("#Ads The previous native ad failed to load. Attempting to load another")
                        if (!adLoader.isLoading) {
                            insertAdsInStoryItems(adapter)
                        }
                    }

                    override fun onAdLoaded() {
                        Timber.d("#Ads native ad loaded")
                        super.onAdLoaded()
                    }
                }
        ).build()

        if (adapter.itemCount != 0){
            NUMBER_OF_ADS = (ceil((adapter.itemCount / 6).toDouble()) + 3).toInt()
        }

        // Load the Native Express ad.
        adLoader.loadAds(AdRequest.Builder().build(), NUMBER_OF_ADS)
    }

    fun insertAdsInStoryItems(adapter: StoriesAdapter) {
        if (mNativeAds.size <= 0) {
            return
        }

        if (adapter.itemCount <= 0) {
            return
        }

        //each 6th item
        for (c in 0 until adapter.itemCount) {

            var adPosition = multipleOfSeven(c)
            if (adPosition != null) {
                if (adPosition == 7) {
                    adapter.addAds(mNativeAds[adCounter], 6)
                } else adapter.addAds(mNativeAds[adCounter], adPosition)
                adCounter++
            }
        }
        Timber.d("Ads position = $adCounter")
        setBottomAd(adCounter)
    }

    open fun setBottomAd(adCounter: Int){
        val background = ColorDrawable(ContextCompat.getColor(requireContext(), R.color.white))
        val callToActionColor = ColorDrawable(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
        val styles = NativeTemplateStyle.Builder()
                .withMainBackgroundColor(background)
                .withCallToActionBackgroundColor(callToActionColor)
                .build()
        //val template = requireActivity().findViewById<TemplateView>(R.id.my_template_bottom)
        my_template_bottom.setStyles(styles)
        my_template_bottom.setNativeAd(mNativeAds.last())
    }

}
