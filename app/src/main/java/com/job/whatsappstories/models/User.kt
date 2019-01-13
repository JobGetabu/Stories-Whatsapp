package com.job.whatsappstories.models

/**
 * Created by Job on Thursday : 1/10/2019.
 */
data class User(val userid: String, val referredby: String, val refercount: Int, val token: String, val ispro: Boolean)