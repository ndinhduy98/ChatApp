package com.freezer.chatapp.ui.main.contacts.pending_contacts

import com.freezer.chatapp.data.model.PendingContact
import com.freezer.chatapp.data.model.PendingContactRequest
import com.freezer.chatapp.data.model.Profile

interface PendingContactItemListener {
    fun onApprove(pendingContact: PendingContact)
    fun onReject(pendingContact: PendingContact)
}