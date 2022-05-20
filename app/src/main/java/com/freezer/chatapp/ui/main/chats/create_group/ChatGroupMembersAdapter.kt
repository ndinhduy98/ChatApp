package com.freezer.chatapp.ui.main.chats.create_group

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.freezer.chatapp.R
import com.freezer.chatapp.data.model.Profile
import com.freezer.chatapp.utils.BindableAdapter
import com.freezer.chatapp.utils.GlideApp
import com.google.firebase.storage.FirebaseStorage

class ChatGroupMembersAdapter(val context: Context, val listener: MembersListener) : RecyclerView.Adapter<ChatGroupMembersAdapter.ResultContactHolder>(), BindableAdapter<List<Profile>> {
    var profiles = emptyList<Profile>()
    override fun setData(items: List<Profile>) {
        profiles = items
        notifyDataSetChanged()
    }

    class ResultContactHolder(val context: Context, private val listener: MembersListener, itemView: View): RecyclerView.ViewHolder(itemView) {
        fun bind(profile: Profile) {
            val ivAvatar = itemView.findViewById<ImageView>(R.id.imageViewItemContactAvatar)
            val tvName = itemView.findViewById<TextView>(R.id.textViewItemContactName)
            val checkBox = itemView.findViewById<CheckBox>(R.id.checkboxItemContact)

            if (profile.avatarUrl.isNotEmpty()) {
                val avatarRef = FirebaseStorage.getInstance("gs://chatapp-68a8d.appspot.com")
                    .reference.child(profile.avatarUrl)

                GlideApp.with(context)
                    .load(avatarRef)
                    .circleCrop()
                    .into(ivAvatar)

                tvName.text = "${profile.firstName} ${profile.lastName}"
            }
            checkBox.setOnCheckedChangeListener { _, isChecked ->
                listener.onCheckBoxClick(profile.uid, isChecked)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultContactHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ResultContactHolder(context, listener, inflater.inflate(R.layout.item_contact_create_group, parent, false))
    }

    override fun onBindViewHolder(holder: ResultContactHolder, position: Int) {
        holder.bind(profiles[position])
    }

    override fun getItemCount() = profiles.size
}