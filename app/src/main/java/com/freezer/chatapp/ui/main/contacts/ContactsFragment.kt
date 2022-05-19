package com.freezer.chatapp.ui.main.contacts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import com.freezer.chatapp.R
import com.freezer.chatapp.data.model.Profile
import com.freezer.chatapp.data.viewmodel.ContactsViewModel
import com.freezer.chatapp.data.viewmodel.PendingContactsViewModel
import com.freezer.chatapp.databinding.FragmentContactsBinding
import com.freezer.chatapp.ui.BaseFragment

class ContactsFragment : BaseFragment() {

    private var _binding: FragmentContactsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentContactsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        _binding?.imageButtonAddContact?.setOnClickListener {
            NavHostFragment.findNavController(this).navigate(R.id.navigation_add_contact)
        }

        _binding?.imageButtonPendingContacts?.setOnClickListener {
            NavHostFragment.findNavController(this).navigate(R.id.action_navigation_contacts_to_navigation_pending_contacts)
        }

        // ContactsViewModel
        val contactsViewModel = ViewModelProvider(requireActivity())[ContactsViewModel::class.java]
        _binding!!.contactsViewModel = contactsViewModel

        _binding!!.recyclerViewContacts.adapter = context?.let { ContactAdapter(it, object : ContactItemListener {
            override fun onClick(profile: Profile) {
                val bundle = Bundle()
                bundle.putParcelable("profile", profile)
                NavHostFragment.findNavController(this@ContactsFragment)
                    .navigate(R.id.navigation_conversation, bundle)
            }

            override fun onLongClick(profile: Profile) {
                TODO("Not yet implemented")
            }
        }) }

        // Pending Contacts View Model
        val pendingContactsViewModel = ViewModelProvider(requireActivity())[PendingContactsViewModel::class.java]
        _binding!!.pendingContactsViewModel = pendingContactsViewModel

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}