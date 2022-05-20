package com.freezer.chatapp.ui.main.chats.create_group

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import com.freezer.chatapp.R
import com.freezer.chatapp.data.model.Profile
import com.freezer.chatapp.data.viewmodel.ContactsViewModel
import com.freezer.chatapp.databinding.FragmentCreateGroupBinding
import com.freezer.chatapp.ui.BaseFragment
import com.google.firebase.auth.FirebaseAuth

class CreateGroupFragment: BaseFragment() {
    private var _binding: FragmentCreateGroupBinding? = null

    override var bottomNavigationViewVisibility = View.GONE

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val chatGroupMembers = arrayListOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateGroupBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.viewModel = ViewModelProvider(requireActivity())[ContactsViewModel::class.java]

        binding.recyclerViewCreateGroup.adapter = context?.let {
            ChatGroupMembersAdapter(it, object :
                MembersListener {
                override fun onCheckBoxClick(uid: String, isChecked: Boolean) {
                    if(isChecked) {
                        chatGroupMembers.add(uid)
                    } else {
                        chatGroupMembers.remove(uid)
                    }
                }
            })
        }

        binding.buttonCreateGroupConfirm.setOnClickListener {
            val bundle = Bundle()
            bundle.putStringArrayList("chat_group_members", chatGroupMembers)
            bundle.putString("chat_group_name", binding.editTextCreateGroupName.text.toString())
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