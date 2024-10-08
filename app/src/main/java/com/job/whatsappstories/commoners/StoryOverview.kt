package com.job.whatsappstories.commoners

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
import com.job.whatsappstories.activities.VideoActivity
import com.job.whatsappstories.databinding.OverviewStoryBinding
import com.job.whatsappstories.models.Story
import com.job.whatsappstories.utils.loadUrl
import com.job.whatsappstories.utils.setDrawable
import com.job.whatsappstories.utils.showView
import com.job.whatsappstories.viewmodel.WhatsModel
import com.mikepenz.ionicons_typeface_library.Ionicons

class StoryOverview : Dialog, View.OnClickListener {
    private var story: Story
    private var c: Context
    private var model: WhatsModel
    private var isFromSaved: String

    private lateinit var binding: OverviewStoryBinding

    constructor(context: Context, story: Story, model: WhatsModel, isFromSaved: String = "False") : super(context) {
        this.c = context
        this.story = story
        this.model = model
        this.isFromSaved = isFromSaved
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        binding = OverviewStoryBinding.inflate(layoutInflater)

        binding.view.setDrawable(AppUtils.setDrawable(c, Ionicons.Icon.ion_eye, R.color.secondaryText, 15))
        binding.share.setDrawable(AppUtils.setDrawable(c, Ionicons.Icon.ion_share, R.color.secondaryText, 15))

        if (isFromSaved.equals("TRUE")) {
            binding.save.setDrawable(AppUtils.setDrawable(c, Ionicons.Icon.ion_android_delete, R.color.secondaryText, 15))
            binding.save.text = "Delete"

        } else binding.save.setDrawable(AppUtils.setDrawable(c, Ionicons.Icon.ion_android_download, R.color.secondaryText, 15))

        binding.view.setOnClickListener(this)
        binding.share.setOnClickListener(this)
        binding.save.setOnClickListener(this)

        when (story.type) {
            K.TYPE_IMAGE -> loadImageStory()

            K.TYPE_VIDEO -> loadVideoStory()
        }

    }


    private fun loadImageStory() {
        binding.image.showView()
        binding.image.loadUrl(story.path!!)
    }

    private fun loadVideoStory() {
        binding.video.showView()
        binding.video.setUp(story.path, JZVideoPlayerStandard.SCREEN_WINDOW_NORMAL, "")
        binding.video.thumbImageView?.loadUrl(story.path!!)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.view -> {
                when (story.type) {
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
                when (story.type) {
                    K.TYPE_IMAGE -> {
                        val image = BitmapFactory.decodeFile(story.path, BitmapFactory.Options())
                        AppUtils.shareImage(c, image)
                    }

                    K.TYPE_VIDEO -> {
                        AppUtils.shareVideo(c, story.path!!)
                    }
                }
            }

            R.id.save -> {
                when (story.type) {
                    K.TYPE_IMAGE -> {
                        val image = BitmapFactory.decodeFile(story.path, BitmapFactory.Options())

                        if (isFromSaved.equals("TRUE")) {

                            AppUtils.deleteImageFile(c, story.path!!)
                            dismiss()
                        } else AppUtils.saveImage(c, image)

                        model.setRefresh(true)
                    }

                    K.TYPE_VIDEO -> {

                        if (isFromSaved.equals("TRUE")) {

                            AppUtils.deleteVideoFile(c, story.path!!)
                            dismiss()
                        } else AppUtils.saveVideoFile(c, story.path!!)
                        model.setRefresh(true)
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