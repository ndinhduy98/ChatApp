package com.freezer.chatapp.ui.main.chats

import com.freezer.chatapp.data.model.ChatGroup

interface ChatGroupItemListener {
    fun onClick(chatGroup: ChatGroup)
}