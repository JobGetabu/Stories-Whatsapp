package com.job.whatsappstories.activities

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.job.whatsappstories.R
import com.job.whatsappstories.commoners.K
import com.job.whatsappstories.models.Story
import com.job.whatsappstories.utils.loadUrl
import kotlinx.android.synthetic.main.activity_image.*

class ImageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image)

        val story = intent.getSerializableExtra(K.STORY) as Story
        image.loadUrl(story.path!!)
    }
}
