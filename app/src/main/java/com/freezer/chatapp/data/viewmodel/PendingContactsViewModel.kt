package com.freezer.chatapp.data.viewmodel

import android.view.View
import androidx.databinding.Bindable
import androidx.databinding.Observable
import androidx.databinding.PropertyChangeRegistry
import androidx.lifecycle.ViewModel
import com.freezer.chatapp.BR
import com.freezer.chatapp.data.model.PendingContact

class PendingContactsViewModel : Observable, ViewModel() {
    @get:Bindable
    var pendingIndicator = View.INVISIBLE

    private val callbacks: PropertyChangeRegistry by lazy { PropertyChangeRegistry() }
    @get:Bindable
    var pendingContacts = ArrayList<PendingContact>()
        set(value) {
            field = value
            pendingIndicator = if (pendingContacts.isNotEmpty()) View.VISIBLE else View.GONE
            callbacks.notifyChange(this, BR.pendingContacts)
        }

    fun remove(pendingContact: PendingContact) {
        pendingContacts.remove(pendingContact)
        pendingIndicator = if (pendingContacts.isNotEmpty()) View.VISIBLE else View.GONE
        callbacks.notifyChange(this, BR.pendingContacts)
        callbacks.notifyChange(this, BR.pendingIndicator)
    }

    fun add(pendingContact: PendingContact) {
        pendingContacts.add(pendingContact)
        pendingIndicator = if (pendingContacts.isNotEmpty()) View.VISIBLE else View.GONE
        callbacks.notifyChange(this, BR.pendingContacts)
        callbacks.notifyChange(this, BR.pendingIndicator)
    }


    override fun addOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback) {
        callbacks.add(callback)
    }

    override fun removeOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback) {
        callbacks.remove(callback)
    }
}