package com.freezer.chatapp.data.viewmodel

import androidx.lifecycle.ViewModel
import com.freezer.chatapp.data.model.Profile

class MyProfileViewModel : ViewModel() {
    var myProfile: Profile = Profile()
}