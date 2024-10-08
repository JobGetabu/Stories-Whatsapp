package com.job.whatsappstories.commoners

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.StrictMode
import androidx.core.content.ContextCompat
import com.job.whatsappstories.R
import com.job.whatsappstories.utils.toast
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.IIcon
import timber.log.Timber
import java.io.*


object AppUtils {

    fun setDrawable(context: Context, icon: IIcon, color: Int, size: Int): Drawable {
        return IconicsDrawable(context).icon(icon).color(ContextCompat.getColor(context, color)).sizeDp(size)
    }

    fun isImage(file: File): Boolean {
        val fileName = file.name.toLowerCase()
        return fileName.endsWith("jpg") || fileName.endsWith("jpeg") || fileName.endsWith("png")
    }

    fun isVideo(file: File): Boolean {
        val fileName = file.name.toLowerCase()
        return fileName.endsWith("mp4") || fileName.endsWith("avi") || fileName.endsWith("gif") || fileName.endsWith("mkv")
    }

    fun saveImage(context: Context, bitmap: Bitmap) {

        val file = File(K.SAVED_STORIES)
        if (!file.exists()) file.mkdirs()

        val fileName = "Story-" + System.currentTimeMillis() + ".jpg"

        val newImage = File(file, fileName)
        if (newImage.exists()) file.delete()
        try {
            val out = FileOutputStream(newImage)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
            out.flush()
            out.close()

            if (Build.VERSION.SDK_INT >= 19) {
                MediaScannerConnection.scanFile(context, arrayOf(newImage.absolutePath), null, null)
            } else {
                context.sendBroadcast(Intent("android.intent.action.MEDIA_MOUNTED", Uri.fromFile(newImage)))
            }
            context.toast("Story saved")

        } catch (e: Exception) {
            Timber.e(e.localizedMessage)
        }

    }

    fun saveVideo(context: Context, filePath: String) {

        val newfile: File

        try {

            val currentFile = File(filePath)
            val fileName = currentFile.name

            newfile = File(K.SAVED_STORIES, fileName)

            if (currentFile.exists()) {

                val instream = FileInputStream(currentFile)
                val out = FileOutputStream(newfile)

                // Copy the bits from instream to outstream
                val buf = ByteArray(1024)
                var len = instream.read(buf)

                while (len > 0) {
                    out.write(buf, 0, len)
                }

                instream.close()
                out.close()

                context.toast("Video saved")
            } else {
                context.toast("Error saving video")
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun saveVideoFile(context: Context, filePath: String) {
        val currentFile = File(filePath)
        val fileName = currentFile.name

        try {
            File(filePath).copyTo(File(K.SAVED_STORIES, fileName), true)
            context.toast("Video saved")
        } catch (e: Exception) {
            Timber.tag("saveVid").e(e)
            context.toast("Error saving video")
        }
    }

    fun deleteVideoFile(context: Context, filePath: String) {
        val currentFile = File(filePath)

        try {
            currentFile.delete()

            context.toast("Video deleted")
        } catch (e: Exception) {
            Timber.tag("delVid").e(e)
            context.toast("Error deleting video")
        }
    }

    fun deleteImageFile(context: Context, filePath: String) {
        val currentFile = File(filePath)

        try {
            currentFile.delete()

            context.toast("Image deleted")
        } catch (e: Exception) {
            Timber.tag("delVid").e(e)
            context.toast("Error deleting image")
        }
    }

    fun shareImage(context: Context, bitmap: Bitmap) {
        val share = Intent(Intent.ACTION_SEND)
        share.type = "image/*"

        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val f = File("${Environment.getExternalStorageDirectory()}/${File.separator}/temporary_file.jpg")

        try {
            f.createNewFile()
            val fo = FileOutputStream(f)
            fo.write(bytes.toByteArray())
        } catch (e: IOException) {
            e.printStackTrace()
        }

        if (Build.VERSION.SDK_INT >= 24) {
            try {
                val m = StrictMode::class.java.getMethod("disableDeathOnFileUriExposure")
                m.invoke(null)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        share.putExtra(Intent.EXTRA_STREAM, Uri.parse("file:///sdcard/temporary_file.jpg"))
        context.startActivity(Intent.createChooser(share, "Share via..."))
    }

    fun shareVideo(context: Context, path: String) {
        val uri = Uri.fromFile(File(path))
        val intent = Intent(Intent.ACTION_SEND, uri)
        intent.setDataAndType(Uri.parse(path), "video/*")
        //intent.setPackage(WHATAPP_PACKAGE_NAME)
        intent.putExtra(Intent.EXTRA_STREAM, uri)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        context.startActivity(Intent.createChooser(intent, "Share via..."))
    }

    fun shareApp(context: Context) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.setType("text/plain")
        intent.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.share_txt))
        intent.putExtra(Intent.EXTRA_TEXT, context.getString(R.string.share_link_txt))
        context.startActivity(Intent.createChooser(intent, "Share via..."))
    }

    fun rateApp(context: Context){

        try {
            val playstoreuri1: Uri = Uri.parse("market://details?id=" + "com.job.whatsappstories")

            val playstoreIntent1 = Intent(Intent.ACTION_VIEW, playstoreuri1)
            context.startActivity(playstoreIntent1)
            //it genrate exception when devices do not have playstore
        }catch (exp:Exception){
            val playstoreuri2: Uri = Uri.parse("http://play.google.com/store/apps/details?id=" + "com.job.whatsappstories")
            val playstoreIntent2 = Intent(Intent.ACTION_VIEW, playstoreuri2)
            context.startActivity(playstoreIntent2)
        }
    }

}