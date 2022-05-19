package com.freezer.chatapp.data.viewmodel

import androidx.databinding.Bindable
import androidx.databinding.Observable
import androidx.databinding.PropertyChangeRegistry
import androidx.lifecycle.ViewModel
import com.freezer.chatapp.BR
import com.freezer.chatapp.data.model.Profile

class ContactsViewModel: Observable, ViewModel() {
    private val callbacks: PropertyChangeRegistry by lazy { PropertyChangeRegistry() }

    @get:Bindable
    var contactProfiles = ArrayList<Profile>()
        set(value) {
            field = value
            callbacks.notifyChange(this, BR.contactProfiles)
        }

    fun remove(profile: Profile) {
        contactProfiles.remove(profile)
        callbacks.notifyChange(this, BR.contactProfiles)
    }

    fun add(profile: Profile) {
        contactProfiles.add(profile)
        callbacks.notifyChange(this, BR.contactProfiles)
    }

    override fun addOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback) {
        callbacks.add(callback)
    }

    override fun removeOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback) {
        callbacks.remove(callback)
    }
}