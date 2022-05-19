package com.freezer.chatapp.data.viewmodel

import androidx.databinding.Bindable
import androidx.databinding.Observable
import androidx.databinding.PropertyChangeRegistry
import androidx.lifecycle.ViewModel
import com.freezer.chatapp.BR
import com.freezer.chatapp.data.model.ChatGroup

class ChatGroupsViewModel: Observable, ViewModel() {
    private val callbacks: PropertyChangeRegistry by lazy { PropertyChangeRegistry() }

    @get:Bindable
    var chatGroups = ArrayList<ChatGroup>()
        set(value) {
            field = value
            callbacks.notifyChange(this, BR.chatGroups)
        }

    fun remove(chatGroup: ChatGroup) {
        chatGroups.remove(chatGroup)
        callbacks.notifyChange(this, BR.chatGroups)
    }

    fun add(chatGroup: ChatGroup) {
        chatGroups.add(chatGroup)
        callbacks.notifyChange(this, BR.chatGroups)
    }

    override fun addOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback) {
        callbacks.add(callback)
    }

    override fun removeOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback) {
        callbacks.remove(callback)
    }
}