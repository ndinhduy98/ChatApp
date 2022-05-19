package com.freezer.chatapp.ui.main.contacts.pending_contacts

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.freezer.chatapp.data.model.PendingContact
import com.freezer.chatapp.data.model.PendingContactRequestStatus
import com.freezer.chatapp.data.viewmodel.PendingContactsViewModel
import com.freezer.chatapp.databinding.FragmentsPendingContactsBinding
import com.freezer.chatapp.ui.BaseFragment
import com.freezer.chatapp.utils.FirestoreUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class PendingContactsFragment : BaseFragment() {
    private var _binding: FragmentsPendingContactsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentsPendingContactsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val pendingContactsViewModel = ViewModelProvider(requireActivity())[PendingContactsViewModel::class.java]
        _binding!!.viewModel = pendingContactsViewModel

        _binding!!.recyclerViewPendingContacts.adapter = context?.let { PendingContactAdapter(it, object : PendingContactItemListener {
            override fun onApprove(pendingContact: PendingContact) {
                CoroutineScope(Dispatchers.IO).launch {
                    setPendingContactRequest(pendingContact, PendingContactRequestStatus.APPROVED)
                }
                // Save to contacts in Firestore
//                addToContacts(pendingContact)
            }

            override fun onReject(pendingContact: PendingContact) {
                CoroutineScope(Dispatchers.IO).launch {
                    setPendingContactRequest(pendingContact, PendingContactRequestStatus.REJECTED)
                }
            }

        }) }
        _binding!!.recyclerViewPendingContacts.layoutManager = LinearLayoutManager(activity)

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private suspend fun setPendingContactRequest(pendingContact: PendingContact, status: String) {
        val user = FirebaseAuth.getInstance().currentUser
        val database = Firebase.firestore
        if (user != null) {
            val documentSnapshot = database.collection("pending_requests").document(user.uid)
                .get().await()
            val requests = documentSnapshot.get("requests") as ArrayList<HashMap<String, String>>

            val parsedRequests = FirestoreUtils().parsePendingContactsWithoutProfiles(requests)

            parsedRequests.forEach {
                if(it.from == pendingContact.request.from) {
                    it.status = status
                }
            }

            database.collection("pending_requests").document(user.uid)
                .update("requests", parsedRequests)
                .addOnSuccessListener {
                    Toast.makeText(context, "Successful", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show()
                    Log.e("PENDING_CONTACT", "${it.printStackTrace()}")
                }
        }
    }

    private fun addToContacts(pendingContact: PendingContact) {
        val user = FirebaseAuth.getInstance().currentUser
        val database = Firebase.firestore
        if(user != null) {
            database.collection("contacts")
                .document(user.uid).update("list",
                    FieldValue.arrayUnion(database.collection("profiles").document(pendingContact.profile!!.uid)))
        }
    }
}