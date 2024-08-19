package com.job.whatsappstories.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.github.chrisbanes.photoview.PhotoView
import com.job.whatsappstories.R
import com.job.whatsappstories.commoners.K
import com.job.whatsappstories.models.Story
import com.job.whatsappstories.utils.loadUrl

class ImageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image)


        val story = intent.getSerializableExtra(K.STORY) as Story
        findViewById<PhotoView>(R.id.image).loadUrl(story.path!!)
    }
}
