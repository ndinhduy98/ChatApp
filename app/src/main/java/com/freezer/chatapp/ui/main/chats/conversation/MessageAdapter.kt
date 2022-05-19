package com.freezer.chatapp.ui.main.chats.conversation

import com.freezer.chatapp.R
import com.freezer.chatapp.data.model.Message
import com.freezer.chatapp.databinding.ItemConversationReceiveImageBinding
import com.freezer.chatapp.databinding.ItemConversationReceiveTextBinding
import com.freezer.chatapp.databinding.ItemConversationSendImageBinding
import com.freezer.chatapp.databinding.ItemConversationSendTextBinding
import com.xwray.groupie.databinding.BindableItem

class TextSendMessageAdapter(private val message: Message)
    : BindableItem<ItemConversationSendTextBinding>() {
    override fun bind(viewBinding: ItemConversationSendTextBinding, position: Int) {
        viewBinding.message = message
    }

    override fun getLayout(): Int {
        return R.layout.item_conversation_send_text
    }
}

class TextReceiveMessageAdapter(private val message: Message)
    : BindableItem<ItemConversationReceiveTextBinding>() {
    override fun bind(viewBinding: ItemConversationReceiveTextBinding, position: Int) {
        viewBinding.message = message
    }

    override fun getLayout(): Int {
        return R.layout.item_conversation_receive_text
    }
}

class ImageSendMessageAdapter(private val message: Message)
    : BindableItem<ItemConversationSendImageBinding>() {
    override fun bind(viewBinding: ItemConversationSendImageBinding, position: Int) {
        viewBinding.message = message
    }

    override fun getLayout(): Int {
        return R.layout.item_conversation_send_image
    }
}

class ImageReceiveMessageAdapter(private val message: Message)
    : BindableItem<ItemConversationReceiveImageBinding>() {
    override fun bind(viewBinding: ItemConversationReceiveImageBinding, position: Int) {
        viewBinding.message = message
    }

    override fun getLayout(): Int {
        return R.layout.item_conversation_receive_image
    }
}

