package com.job.whatsappstories.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * Created by Job on Thursday : 1/10/2019.
 */
class WhatsModel: ViewModel() {
    private var currentFile: MutableLiveData<String> = MutableLiveData()
    private var refresh: MutableLiveData<Boolean> = MutableLiveData()

    init {
        //currentFile.value = K.WHATSAPP_STORIES

    }

    fun getCurrentFile() = currentFile
    fun getRefresh() = refresh
    fun setRefresh(ref: Boolean) {refresh.value = ref}
    fun setCurrentFile(cFile: String) {currentFile.value = cFile}

}