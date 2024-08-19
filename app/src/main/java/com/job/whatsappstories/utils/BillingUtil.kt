package com.job.whatsappstories.utils

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.job.whatsappstories.R
import com.job.whatsappstories.activities.MainActivity
import com.job.whatsappstories.commoners.Application
import com.job.whatsappstories.models.User
import com.job.whatsappstories.utils.Constants.*
import timber.log.Timber


fun removeAds() {
    Timber.tag("pay").d("Remove ads called")
    upgradeUser()
}


fun upgradeUser() {
    val userPrefsEditor = Application.instance.getPrefs().edit()
    val userPrefs = Application.instance.getPrefs()

    val userId = userPrefs.getString(USER_UID, "")
    if (userId == "") signInOnUpgrade()
    else {
        FirebaseFirestore.getInstance()
                .collection(USER_COL).document(userId!!)
                .update("ispro", true)
                .addOnSuccessListener {
                    userPrefsEditor.putBoolean(IS_PRO_USER, true)
                    userPrefsEditor.apply()
                }
    }
}

fun checkUpgrade(activity: Activity) {
    val userPrefsEditor = Application.instance.getPrefs().edit()
    val userPrefs = Application.instance.getPrefs()
    val userId = userPrefs.getString(USER_UID, "")
    if (userId != "") {
        FirebaseFirestore.getInstance()
                .collection(USER_COL).document(userId!!)
                .get()
                .addOnCompleteListener {
                    if (it.isSuccessful) run {
                        val user: User = it.result!!.toObject(User::class.java)!!

                        if (user.ispro) {
                            userPrefsEditor.putBoolean(IS_PRO_USER, true)
                            userPrefsEditor.apply()
                            val intent = Intent(activity, MainActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            activity.startActivity(intent)
                        }
                    }
                }
    }

}

private fun signInOnUpgrade() {
    FirebaseAuth.getInstance().signInAnonymously()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Timber.d("signInAnonymously:success")
                    val user = FirebaseAuth.getInstance().currentUser
                    val userPrefsEditor = Application.instance.getPrefs().edit()
                    userPrefsEditor.putString(Constants.USER_UID, user?.uid)
                    userPrefsEditor.apply()

                    FirebaseFirestore.getInstance()
                            .collection(USER_COL).document(user!!.uid)
                            .update("ispro", true)
                            .addOnSuccessListener {
                                userPrefsEditor.putBoolean(IS_PRO_USER, true)
                                userPrefsEditor.apply()
                            }

                } else {
                    // If sign in fails, display a message to the user.
                    Timber.e(task.exception, "signInAnonymously:failure")
                    Timber.d("Authentication failed.")
                }
            }
}

fun loadScreenIconsPro(activity: Activity): Array<Drawable?> {
    val ta = activity.resources.obtainTypedArray(R.array.ld_activityScreenIcons_pro)
    val icons = arrayOfNulls<Drawable>(ta.length())
    for (i in 0 until ta.length()) {
        val id = ta.getResourceId(i, 0)
        if (id != 0) {
            icons[i] = ContextCompat.getDrawable(activity, id)
        }
    }
    ta.recycle()
    return icons
}

fun loadScreenTitlesPro(activity: Activity): Array<String> {
    return activity.resources.getStringArray(R.array.ld_activityScreenTitles_pro)
}

