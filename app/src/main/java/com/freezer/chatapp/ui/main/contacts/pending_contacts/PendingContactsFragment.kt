package com.freezer.chatapp.ui.main.contacts.pending_contacts

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.freezer.chatapp.data.model.PendingContact
import com.freezer.chatapp.data.model.PendingContactRequest
import com.freezer.chatapp.data.viewmodel.PendingContactsViewModel
import com.freezer.chatapp.databinding.FragmentsPendingContactsBinding
import com.freezer.chatapp.ui.BaseFragment

@RequiresApi(Build.VERSION_CODES.N)
class PendingContactsFragment : BaseFragment() {
    private var _binding: FragmentsPendingContactsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val pendingContactsViewModel: PendingContactsViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentsPendingContactsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.viewModel = pendingContactsViewModel

        binding.recyclerViewPendingContacts.adapter = context?.let {
            PendingContactAdapter(it, object : PendingContactItemListener {
                override fun onApprove(pendingContact: PendingContact) {
                    pendingContactsViewModel.setPendingContactRequest(
                        pendingContact,
                        PendingContactRequest.Status.APPROVED
                    )
                }

                override fun onReject(pendingContact: PendingContact) {
                    pendingContactsViewModel.setPendingContactRequest(
                        pendingContact,
                        PendingContactRequest.Status.REJECTED
                    )
                }

            })
        }
        _binding!!.recyclerViewPendingContacts.layoutManager = LinearLayoutManager(activity)

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}