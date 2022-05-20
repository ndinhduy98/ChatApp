package com.freezer.chatapp.ui.main.chats

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import com.freezer.chatapp.R
import com.freezer.chatapp.data.model.ChatGroup
import com.freezer.chatapp.data.viewmodel.ChatGroupsViewModel
import com.freezer.chatapp.databinding.FragmentChatsBinding
import com.freezer.chatapp.ui.BaseFragment
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

class ChatsFragment : BaseFragment() {

    private var _binding: FragmentChatsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatsBinding.inflate(inflater, container, false)

        binding.imageButtonNewConversation.setOnClickListener {
            NavHostFragment.findNavController(this).navigate(R.id.action_navigation_chats_to_navigation_create_group)
        }
        initializeChatsRecyclerView()

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initializeChatsRecyclerView() {
        _binding!!.chatGroupsViewModel = ViewModelProvider(requireActivity())[ChatGroupsViewModel::class.java]

        _binding!!.recyclerViewChatGroups.adapter = context?.let { ChatGroupAdapter(it,
            object : ChatGroupItemListener {
                override fun onClick(chatGroup: ChatGroup) {
                    val bundle = Bundle()
                    bundle.putParcelable("chat_group", chatGroup)
                    NavHostFragment.findNavController(this@ChatsFragment)
                        .navigate(R.id.navigation_conversation, bundle)
                }
            })}
    }
}