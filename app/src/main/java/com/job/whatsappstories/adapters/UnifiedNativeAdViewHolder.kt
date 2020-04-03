package com.job.whatsappstories.adapters

import android.graphics.drawable.ColorDrawable
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.ads.nativetemplates.NativeTemplateStyle
import com.google.android.ads.nativetemplates.TemplateView
import com.google.android.gms.ads.formats.MediaView
import com.google.android.gms.ads.formats.UnifiedNativeAd
import com.google.android.gms.ads.formats.UnifiedNativeAdView
import com.job.whatsappstories.R


class UnifiedNativeAdViewHolder internal constructor(view: View) : RecyclerView.ViewHolder(view) {
    val background = ColorDrawable(ContextCompat.getColor(view.context, R.color.white))
    val callToActionColor = ColorDrawable(ContextCompat.getColor(view.context, R.color.colorPrimary))
    var styles = NativeTemplateStyle.Builder()
            .withMainBackgroundColor(background)
            .withCallToActionBackgroundColor(callToActionColor)
            .build()
    val template = view.findViewById<TemplateView>(R.id.my_template)

    init {
        template.setStyles(styles)
    }
}

fun populateNativeAdView(nativeAd: UnifiedNativeAd,
                         template: TemplateView ) {

    template.setNativeAd(nativeAd)
}