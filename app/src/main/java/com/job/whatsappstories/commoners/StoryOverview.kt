package com.job.whatsappstories.commoners

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.Window
import cn.jzvd.JZVideoPlayer
import cn.jzvd.JZVideoPlayerStandard
import com.job.whatsappstories.R
import com.job.whatsappstories.activities.ImageActivity
import com.job.whatsappstories.activities.MainActivity
import com.job.whatsappstories.activities.VideoActivity
import com.job.whatsappstories.models.Story
import com.job.whatsappstories.utils.loadUrl
import com.job.whatsappstories.utils.setDrawable
import com.job.whatsappstories.utils.showView
import com.mikepenz.ionicons_typeface_library.Ionicons
import kotlinx.android.synthetic.main.overview_story.*

class StoryOverview : Dialog, View.OnClickListener {
    private var story: Story
    private var c: Context
    private var mainActivity: Activity

    constructor(context: Context, story: Story, mainActivity: Activity): super(context) {
        this.c = context
        this.story = story
        this.mainActivity = mainActivity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.overview_story)

        view.setDrawable(AppUtils.setDrawable(c, Ionicons.Icon.ion_eye, R.color.secondaryText, 15))
        share.setDrawable(AppUtils.setDrawable(c, Ionicons.Icon.ion_share, R.color.secondaryText, 15))
        save.setDrawable(AppUtils.setDrawable(c, Ionicons.Icon.ion_android_download, R.color.secondaryText, 15))

        view.setOnClickListener(this)
        share.setOnClickListener(this)
        save.setOnClickListener(this)

        when(story.type) {
            K.TYPE_IMAGE -> loadImageStory()

            K.TYPE_VIDEO -> loadVideoStory()
        }

    }



    private fun loadImageStory() {
        image?.showView()
        image?.loadUrl(story.path!!)
    }

    private fun loadVideoStory() {
        video?.showView()
        video?.setUp(story.path, JZVideoPlayerStandard.SCREEN_WINDOW_NORMAL, "")
        video?.thumbImageView?.loadUrl(story.path!!)
    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.view -> {
                when(story.type) {
                    K.TYPE_IMAGE -> {
                        val i = Intent(c, ImageActivity::class.java)
                        i.putExtra(K.STORY, story)
                        c.startActivity(i)
                    }

                    K.TYPE_VIDEO -> {
                        val i = Intent(c, VideoActivity::class.java)
                        i.putExtra(K.STORY, story)
                        c.startActivity(i)
                    }
                }
            }

            R.id.share -> {
                when(story.type) {
                    K.TYPE_IMAGE -> {
                        val image = BitmapFactory.decodeFile(story.path,BitmapFactory.Options())
                        AppUtils.shareImage(c,image)
                    }

                    K.TYPE_VIDEO -> {
                        AppUtils.shareVideo(c, story.path!!)
                    }
                }
            }

            R.id.save -> {
                when(story.type) {
                    K.TYPE_IMAGE -> {
                        val image = BitmapFactory.decodeFile(story.path,BitmapFactory.Options())
                        AppUtils.saveImage(c,image)
                        (mainActivity as MainActivity).refreshPages()
                    }

                    K.TYPE_VIDEO -> {
                        AppUtils.saveVideoFile(c, story.path!!)
                        (mainActivity as MainActivity).refreshPages()
                    }
                }
            }
        }
    }

    override fun onBackPressed() {

        JZVideoPlayer.backPress()
        JZVideoPlayer.releaseAllVideos()
        super.onBackPressed()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {

        JZVideoPlayer.releaseAllVideos()
        return super.onTouchEvent(event)
    }
}