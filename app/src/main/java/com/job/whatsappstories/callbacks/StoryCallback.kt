package com.job.whatsappstories.callbacks

import android.view.View
import com.job.whatsappstories.models.Story

interface StoryCallback {

    fun onStoryClicked(v: View, story: Story)

}