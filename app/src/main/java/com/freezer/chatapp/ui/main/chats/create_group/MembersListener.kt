package com.freezer.chatapp.ui.main.chats.create_group

import com.freezer.chatapp.data.model.Profile

interface MembersListener {
    fun onCheckBoxClick(uid: String, isChecked: Boolean)
}