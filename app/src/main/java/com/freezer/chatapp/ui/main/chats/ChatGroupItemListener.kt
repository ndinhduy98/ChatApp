package com.freezer.chatapp.ui.main.chats

import com.freezer.chatapp.data.model.ChatGroup
import com.freezer.chatapp.data.model.Profile

interface ChatGroupItemListener {
    fun onClick(chatGroup: ChatGroup, chatGroupName: String)
}