package com.freezer.chatapp.ui.main.chats

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.freezer.chatapp.R
import com.freezer.chatapp.data.model.ChatGroup
import com.freezer.chatapp.data.model.ChatGroupType
import com.freezer.chatapp.data.model.Profile
import com.freezer.chatapp.utils.BindableAdapter
import com.freezer.chatapp.utils.GlideApp
import com.google.firebase.storage.FirebaseStorage


class ChatGroupAdapter(val context: Context, val contacts: ArrayList<Profile>, val listener: ChatGroupItemListener) : RecyclerView.Adapter<ChatGroupAdapter.ChatGroupHolder>(),
    BindableAdapter<List<ChatGroup>> {
    var chatGroups = emptyList<ChatGroup>()
    override fun setData(items: List<ChatGroup>) {
        chatGroups = items
        notifyDataSetChanged()
    }

    class ChatGroupHolder(val context: Context, val contacts: ArrayList<Profile>, private val listener: ChatGroupItemListener, itemView: View): RecyclerView.ViewHolder(itemView) {
        fun bind(chatGroup: ChatGroup) {
            val tvName = itemView.findViewById<TextView>(R.id.textViewItemChatGroupName)
            val ivAvatar = itemView.findViewById<ImageView>(R.id.imageViewChatItemContactAvatar)

            if(chatGroup.type == ChatGroupType.PRIVATE_CHAT) {
                val targetProfile = contacts.find { profile ->
                    profile.uid != chatGroup.createdBy
                }

                val avatarRef = targetProfile?.let {
                    FirebaseStorage.getInstance("gs://chatapp-68a8d.appspot.com")
                        .reference.child(it.avatarUrl)
                }
                GlideApp.with(context)
                    .load(avatarRef)
                    .circleCrop()
                    .into(ivAvatar)
                chatGroup.name = "${targetProfile?.firstName} ${targetProfile?.lastName}"
                tvName.text = chatGroup.name
            } else {
                tvName.text = chatGroup.name
            }

            itemView.setOnClickListener {
                listener.onClick(chatGroup, chatGroup.name!!)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatGroupHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ChatGroupHolder(context, contacts, listener, inflater.inflate(R.layout.item_chat_group, parent, false))
    }

    override fun onBindViewHolder(holder: ChatGroupHolder, position: Int) {
        holder.bind(chatGroups[position])
    }

    override fun getItemCount() = chatGroups.size
}