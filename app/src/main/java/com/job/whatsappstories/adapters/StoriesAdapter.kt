package com.job.whatsappstories.adapters

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.ads.nativetemplates.NativeTemplateStyle
import com.google.android.ads.nativetemplates.TemplateView
import com.google.android.gms.ads.formats.UnifiedNativeAd
import com.job.whatsappstories.R
import com.job.whatsappstories.callbacks.StoryCallback
import com.job.whatsappstories.commoners.AppUtils.setDrawable
import com.job.whatsappstories.databinding.ItemStoryBinding
import com.job.whatsappstories.models.Story
import com.job.whatsappstories.utils.inflate
import com.mikepenz.ionicons_typeface_library.Ionicons


class StoriesAdapter(private val callback: StoryCallback, private val context: Context) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    //defining supported view types
    //need to support list-view style


    val STATUS_ITEM_VIEW_TYPE = 0
    val NATIVE_AD_VIEW_TYPE = 1


    private val stories = mutableListOf<Any>()

    fun addStory(story: Story) {
        stories.add(story)
        notifyItemInserted(stories.size)
    }

    fun addAds(ad: UnifiedNativeAd) {
        stories.add(ad)
        notifyItemInserted(stories.size)
    }

    fun addAds(ad: UnifiedNativeAd, position: Int) {
        stories.add(position, ad)
        notifyItemInserted(position)
    }

    fun addStories(stories: List<Story>) {
        this.stories.addAll(stories)
        notifyDataSetChanged()
    }

    fun clearStories() {
        stories.clear()
    }

    override fun getItemViewType(position: Int): Int {
        val recyclerViewItem: Any = stories[position]

        return if (recyclerViewItem is UnifiedNativeAd) NATIVE_AD_VIEW_TYPE
        else STATUS_ITEM_VIEW_TYPE
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            NATIVE_AD_VIEW_TYPE -> {
                val unifiedNativeLayoutView: View = LayoutInflater.from(
                        parent.context).inflate(R.layout.item_story_ad,
                        parent, false)
                UnifiedNativeAdViewHolder(unifiedNativeLayoutView)
            }

            STATUS_ITEM_VIEW_TYPE -> {
                StoriesHolder(parent.inflate(R.layout.item_story), callback, context)
            }
            else -> {
                StoriesHolder(parent.inflate(R.layout.item_story), callback, context)
            }
        }
    }

    override fun getItemCount(): Int = stories.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        when (getItemViewType(position)) {
            NATIVE_AD_VIEW_TYPE -> {
                val nativeAd = stories[position] as UnifiedNativeAd
                populateNativeAdView(nativeAd, (holder as UnifiedNativeAdViewHolder).template)

            }

            STATUS_ITEM_VIEW_TYPE -> {
                (holder as StoriesHolder).bind(stories[position] as Story)
            }

            else -> {
                (holder as StoriesHolder).bind(stories[position]as Story)
            }
        }

    }

    class StoriesHolder(private val binding: ItemStoryBinding, private val callback: StoryCallback, private val context: Context) :
            RecyclerView.ViewHolder(binding.root) {

        init {
            binding.videoIcon.setImageDrawable(setDrawable(context, Ionicons.Icon.ion_play, R.color.white, 27))
        }

        fun bind(story: Story) {
            binding.story = story
            binding.callback = callback
        }
    }
}