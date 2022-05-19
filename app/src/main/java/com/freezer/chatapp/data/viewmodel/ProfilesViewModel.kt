package com.freezer.chatapp.data.viewmodel

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.freezer.chatapp.BR
import com.freezer.chatapp.data.model.Profile

class ProfilesViewModel : BaseObservable() {
    @get:Bindable
    var profiles: List<Profile> = emptyList()
        set(value) {
            field = value
            notifyPropertyChanged(BR.profiles)
        }
}