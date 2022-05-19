package com.freezer.chatapp.data.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ConversationViewModel: ViewModel() {
    val conversationName = MutableLiveData<String>()
    var groupId = ""
}