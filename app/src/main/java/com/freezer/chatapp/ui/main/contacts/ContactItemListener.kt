package com.freezer.chatapp.ui.main.contacts

import com.freezer.chatapp.data.model.Profile

interface ContactItemListener {
    fun onClick(profile: Profile)
    fun onLongClick(profile: Profile)
}