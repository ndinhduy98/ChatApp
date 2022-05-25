package com.freezer.chatapp.ui.main.contacts.add_contact

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.viewModels
import com.freezer.chatapp.data.model.Profile
import com.freezer.chatapp.data.viewmodel.AddContactViewModel
import com.freezer.chatapp.databinding.FragmentAddContactBinding
import com.freezer.chatapp.ui.BaseFragment
import com.freezer.chatapp.ui.main.contacts.ContactAdapter
import com.freezer.chatapp.ui.main.contacts.ContactItemListener
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddContactFragment : BaseFragment() {
    private var _binding: FragmentAddContactBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val addContactViewModel: AddContactViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddContactBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.viewModel = addContactViewModel

        binding.recyclerViewAddContact.adapter = context?.let { ContactAdapter(it, object :
                ContactItemListener {
            override fun onClick(profile: Profile) {
                addContactViewModel.addPendingContact(profile)
            }

            override fun onLongClick(profile: Profile) {
                TODO("Not yet implemented")
            }
        }) }

        binding.searchViewAddContactPhoneNumber.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    addContactViewModel.findPhoneNumber(query)
                }
                return true
            }

            override fun onQueryTextChange(query: String?): Boolean {
                query?.let {
                    addContactViewModel.findPhoneNumber(query)
                }
                return true
            }
        })

        addContactViewModel.addContactStatus.observe(viewLifecycleOwner) { status ->
            if(status) {
                Toast.makeText(context, "Send request successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Send request failed", Toast.LENGTH_SHORT).show()
            }
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}