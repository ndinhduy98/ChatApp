package com.freezer.chatapp.ui.main.contacts.pending_contacts

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.freezer.chatapp.R
import com.freezer.chatapp.data.model.PendingContact
import com.freezer.chatapp.utils.BindableAdapter
import com.freezer.chatapp.utils.GlideApp
import com.google.firebase.storage.FirebaseStorage

class PendingContactAdapter(val context: Context, val listener: PendingContactItemListener) : RecyclerView.Adapter<PendingContactAdapter.PendingContactHolder>(), BindableAdapter<List<PendingContact>> {
    private var pendingContacts = emptyList<PendingContact>()
    override fun setData(items: List<PendingContact>) {
        pendingContacts = items
        notifyDataSetChanged()
    }

    class PendingContactHolder(val context: Context, private val listener: PendingContactItemListener, itemView: View): RecyclerView.ViewHolder(itemView) {
        fun bind(pendingContact: PendingContact) {
            val ivAvatar = itemView.findViewById<ImageView>(R.id.imageViewItemContactAvatar)
            val tvName = itemView.findViewById<TextView>(R.id.textViewItemContactName)
            val ibApprove = itemView.findViewById<ImageButton>(R.id.imageButtonPendingContactApprove)
            val ibCancel = itemView.findViewById<ImageButton>(R.id.imageButtonPendingContactCancel)

            if (pendingContact.profile!!.avatarUrl.isNotEmpty()) {
                val avatarRef = FirebaseStorage.getInstance("gs://chatapp-68a8d.appspot.com")
                    .reference.child(pendingContact.profile.avatarUrl)

                GlideApp.with(context)
                    .load(avatarRef)
                    .circleCrop()
                    .into(ivAvatar)
                tvName.text = "${pendingContact.profile!!.firstName} ${pendingContact.profile!!.lastName}"
            }

            ibApprove.setOnClickListener {
                listener.onApprove(pendingContact)
            }

            ibCancel.setOnClickListener {
                listener.onReject(pendingContact)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PendingContactHolder {
        val inflater = LayoutInflater.from(parent.context)
        return PendingContactHolder(context, listener, inflater.inflate(R.layout.item_pending_contact, parent, false))
    }

    override fun onBindViewHolder(holder: PendingContactHolder, position: Int) {
        holder.bind(pendingContacts[position])
    }

    override fun getItemCount() = pendingContacts.size
}

