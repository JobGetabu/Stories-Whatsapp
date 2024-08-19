package com.job.whatsappstories.activities

import android.os.Bundle
import cn.jzvd.JZVideoPlayer
import cn.jzvd.JZVideoPlayerStandard
import com.job.whatsappstories.R
import com.job.whatsappstories.commoners.BaseActivity
import com.job.whatsappstories.commoners.K
import com.job.whatsappstories.models.Story
import com.job.whatsappstories.utils.loadUrl

class VideoActivity : BaseActivity() {

    private val video = findViewById<JZVideoPlayerStandard>(R.id.video)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video)


        val story = intent.getSerializableExtra(K.STORY) as Story

        video.setUp(story.path, JZVideoPlayerStandard.SCREEN_WINDOW_NORMAL, "")
        video.thumbImageView.loadUrl(story.path!!)
    }

    override fun onPause() {
        super.onPause()
        JZVideoPlayer.releaseAllVideos()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        JZVideoPlayer.backPress()
    }
}
