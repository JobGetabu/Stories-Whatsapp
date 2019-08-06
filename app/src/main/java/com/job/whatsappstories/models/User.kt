package com.job.whatsappstories.models

import androidx.annotation.Keep

/**
 * Created by Job on Thursday : 1/10/2019.
 */
@Keep
data class User(val userid: String = "", val referredby: String= "", val refercount: Int = 0, val token: String= "", val ispro: Boolean = false)