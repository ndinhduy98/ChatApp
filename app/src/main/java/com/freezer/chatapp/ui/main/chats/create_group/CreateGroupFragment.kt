package com.freezer.chatapp.ui.main.chats.create_group

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.NavHostFragment
import com.freezer.chatapp.R
import com.freezer.chatapp.data.viewmodel.ContactsViewModel
import com.freezer.chatapp.data.viewmodel.CreateGroupViewModel
import com.freezer.chatapp.databinding.FragmentCreateGroupBinding
import com.freezer.chatapp.ui.BaseFragment
import com.freezer.chatapp.ui.main.afterTextChanged

class CreateGroupFragment: BaseFragment() {
    private var _binding: FragmentCreateGroupBinding? = null

    override var bottomNavigationViewVisibility = View.GONE

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val contactsViewModel: ContactsViewModel by activityViewModels()

    private val createGroupViewModel: CreateGroupViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateGroupBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.contactsViewModel = contactsViewModel

        binding.recyclerViewCreateGroup.adapter = context?.let {
            ChatGroupMembersAdapter(it, object :
                MembersListener {
                override fun onCheckBoxClick(uid: String, isChecked: Boolean) {
                    if(isChecked) {
                        createGroupViewModel.addMember(uid)
                    } else {
                        createGroupViewModel.removeMember(uid)
                    }
                }
            })
        }

        binding.editTextCreateGroupName.afterTextChanged {
            createGroupViewModel.chatGroupName.postValue(it)
        }

        binding.buttonCreateGroupConfirm.setOnClickListener {
            val bundle = bundleOf("chat_group_members" to createGroupViewModel.chatGroupMembers,
                "chat_group_name" to createGroupViewModel.chatGroupName.value)
            NavHostFragment.findNavController(this@CreateGroupFragment)
                .navigate(R.id.navigation_conversation, bundle)
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}