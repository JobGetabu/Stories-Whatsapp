package com.job.whatsappstories.adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.job.whatsappstories.R
import com.job.whatsappstories.callbacks.StoryCallback
import com.job.whatsappstories.commoners.AppUtils.setDrawable
import com.job.whatsappstories.databinding.ItemStoryBinding
import com.job.whatsappstories.models.Story
import com.job.whatsappstories.utils.inflate
import com.mikepenz.ionicons_typeface_library.Ionicons

class StoriesAdapter(private val callback: StoryCallback, private val context: Context) : RecyclerView.Adapter<StoriesAdapter.StoriesHolder>() {

    //defining supported view types
    //need to support list-view style


    val STATUS_ITEM_VIEW_TYPE = 0
    val NATIVE_AD_VIEW_TYPE = 1


    private val stories = mutableListOf<Story>()

    fun addStory(story: Story) {
        stories.add(story)
        notifyItemInserted(stories.size)
    }

    fun addStories(stories: List<Story>) {
        this.stories.addAll(stories)
        notifyDataSetChanged()
    }

    fun clearStories() {
        stories.clear()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoriesHolder {
        return StoriesHolder(parent.inflate(R.layout.item_story), callback, context)
    }

    override fun getItemCount(): Int = stories.size

    override fun onBindViewHolder(holder: StoriesHolder, position: Int) {
        holder.bind(stories[position])
    }

    class StoriesHolder(private val binding: ItemStoryBinding, private val callback: StoryCallback, private val context: Context) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.videoIcon.setImageDrawable(setDrawable(context, Ionicons.Icon.ion_play, R.color.white, 27))
        }

        fun bind(story: Story) {
            binding.story = story
            binding.callback = callback
        }

    }

}