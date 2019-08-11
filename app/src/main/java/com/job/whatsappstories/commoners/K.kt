package com.job.whatsappstories.commoners

import android.content.Context
import android.os.Environment

object K {

    const val STORY = "story"

    const val TYPE_IMAGE = 0
    const val TYPE_VIDEO = 1

    var WHATSAPP_STORIES = Environment.getExternalStorageDirectory().absolutePath +"/WhatsApp/Media/.Statuses"
    fun WHATSAPP_STORIES(context: Context) : String = context.getExternalFilesDir(null)?.absolutePath +"/WhatsApp/Media/.Statuses"

    var WHATSAPP_BUSINESS_STORIES = Environment.getExternalStorageDirectory().absolutePath +"/WhatsApp Business/Media/.Statuses"
    fun WHATSAPP_BUSINESS_STORIES(context: Context) = context.getExternalFilesDir(null)?.absolutePath +"/WhatsApp Business/Media/.Statuses"

    var GBWHATSAPP_STORIES = Environment.getExternalStorageDirectory().absolutePath +"/GBWhatsApp/Media/.Statuses"
    var SAVED_STORIES = Environment.getExternalStorageDirectory().absolutePath +"/Stories Saver"
    fun SAVED_STORIES(context: Context) = context.getExternalFilesDir(null)?.absolutePath +"/Stories Saver"


}