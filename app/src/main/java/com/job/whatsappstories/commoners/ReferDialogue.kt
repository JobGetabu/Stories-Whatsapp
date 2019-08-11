package com.job.whatsappstories.commoners

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.Window
import com.job.whatsappstories.R
import com.job.whatsappstories.utils.createDynamicLink
import com.job.whatsappstories.utils.toast
import kotlinx.android.synthetic.main.dialogue_refer.*

class ReferDialogue : Dialog, View.OnClickListener {
    private var c: Context


    constructor(context: Context) : super(context) {
        this.c = context

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialogue_refer)

        referBtn.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {

            R.id.referBtn -> {
                createDynamicLink(c)
                c.toast("Generating link")
            }

        }
    }
}