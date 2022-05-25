package com.freezer.chatapp.ui.main.chats

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.NavHostFragment
import com.freezer.chatapp.R
import com.freezer.chatapp.data.model.ChatGroup
import com.freezer.chatapp.data.viewmodel.ChatGroupsViewModel
import com.freezer.chatapp.data.viewmodel.ContactsViewModel
import com.freezer.chatapp.databinding.FragmentChatsBinding
import com.freezer.chatapp.ui.BaseFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class ChatsFragment : BaseFragment() {

    private var _binding: FragmentChatsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val chatGroupsViewModel: ChatGroupsViewModel by activityViewModels()
    private val contactsViewModel: ContactsViewModel by activityViewModels()

    private lateinit var user: FirebaseUser

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatsBinding.inflate(inflater, container, false)


        binding.imageButtonNewConversation.setOnClickListener {
            NavHostFragment.findNavController(this)
                .navigate(R.id.action_navigation_chats_to_navigation_create_group)
        }
        user = FirebaseAuth.getInstance().currentUser!!

        initializeChatsRecyclerView()

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initializeChatsRecyclerView() {
        binding.chatGroupsViewModel = chatGroupsViewModel

        binding.recyclerViewChatGroups.adapter = context?.let {
            ChatGroupAdapter(it, contactsViewModel.contactProfiles, user.uid,
                object : ChatGroupItemListener {
                    override fun onClick(chatGroup: ChatGroup, chatGroupName: String) {
                        val bundle = bundleOf("chat_group" to chatGroup, "chat_group_name" to chatGroupName)
                        NavHostFragment.findNavController(this@ChatsFragment)
                            .navigate(R.id.navigation_conversation, bundle)
                    }
                })
        }
    }
}