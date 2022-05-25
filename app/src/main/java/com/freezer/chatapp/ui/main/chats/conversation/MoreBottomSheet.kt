package com.freezer.chatapp.ui.main.chats.conversation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.freezer.chatapp.databinding.FragmentBottomSheetConversationBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class MoreBottomSheet: BottomSheetDialogFragment() {
    private var _binding: FragmentBottomSheetConversationBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBottomSheetConversationBinding.inflate(inflater, container, false)

        return binding.root
    }
}