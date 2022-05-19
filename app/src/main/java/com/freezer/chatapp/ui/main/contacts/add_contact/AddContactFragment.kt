package com.freezer.chatapp.ui.main.contacts.add_contact

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.freezer.chatapp.data.model.PendingContactRequest
import com.freezer.chatapp.data.model.PendingContactRequestStatus
import com.freezer.chatapp.data.model.Profile
import com.freezer.chatapp.data.viewmodel.ProfilesViewModel
import com.freezer.chatapp.databinding.FragmentAddContactBinding
import com.freezer.chatapp.ui.BaseFragment
import com.freezer.chatapp.ui.main.contacts.ContactAdapter
import com.freezer.chatapp.ui.main.contacts.ContactItemListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class AddContactFragment : BaseFragment() {
    private var _binding: FragmentAddContactBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val viewModel = ProfilesViewModel()
    private var profileResults = listOf<Profile>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddContactBinding.inflate(inflater, container, false)
        val root: View = binding.root

        _binding!!.viewModel = viewModel

        _binding!!.recyclerViewAddContact.adapter = context?.let { ContactAdapter(it, object :
                ContactItemListener {
            override fun onClick(profile: Profile) {
                addPendingContact(profile)
            }

            override fun onLongClick(profile: Profile) {
                TODO("Not yet implemented")
            }
        }) }
        _binding!!.recyclerViewAddContact.layoutManager = LinearLayoutManager(activity)


        _binding!!.searchViewAddContactPhoneNumber.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    findPhoneNumber(query)
                }
                return true
            }

            override fun onQueryTextChange(query: String?): Boolean {
                query?.let {
                    findPhoneNumber(query)
                }
                return true
            }
        })

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    private fun findPhoneNumber(phoneNumber: String) {
        val database = Firebase.firestore
        val userRef = database.collection("profiles")

        val query = userRef.whereEqualTo("phoneNumber", "+$phoneNumber")
        query.get()
            .addOnSuccessListener { documents ->
                viewModel.profiles = (documents.toObjects(Profile::class.java))
//                var results = listOf<Profile>()
//                documents.forEach { document ->
//                    results.plus(document.toObject(Profile::class.java))
//                }
//                profileResults = results
//                viewModel.setNewList(profileResults)
                Log.d("Query", profileResults.map { it.phoneNumber }.toString())
            }
            .addOnFailureListener {
                Log.e("Query", it.printStackTrace().toString())
            }
    }

    private fun addPendingContact(profile: Profile) {
        val user = FirebaseAuth.getInstance().currentUser

        val database = Firebase.firestore

        user?.let {
            val pendingRequest = PendingContactRequest(
                from = user.uid,
                to = profile.uid,
                status = PendingContactRequestStatus.PENDING
            )
            database.collection("pending_requests").document(profile.uid).update("requests", FieldValue.arrayUnion(pendingRequest))
                .addOnSuccessListener {
                    Toast.makeText(context, "Send request successfully", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Send request failed", Toast.LENGTH_SHORT).show()
                    Log.e("PENDING_REQUEST", "${it.printStackTrace()}")
                }
        }
    }
}