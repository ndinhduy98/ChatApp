package com.freezer.chatapp.ui.main.chats.conversation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.freezer.chatapp.databinding.FragmentBottomSheetConversationBinding
import com.github.file_picker.ListDirection
import com.github.file_picker.adapter.FilePickerAdapter
import com.github.file_picker.extension.showFilePicker
import com.github.file_picker.listener.OnItemClickListener
import com.github.file_picker.listener.OnSubmitClickListener
import com.github.file_picker.model.Media
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ConversationMoreBottomSheet: BottomSheetDialogFragment() {
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

        binding.imageButtonImageBottomSheet.setOnClickListener {
            showFilePicker(
                limitItemSelection = 1,
                listDirection = ListDirection.LTR,
                onSubmitClickListener = object :OnSubmitClickListener {
                    override fun onClick(files: List<Media>) {

                    }

                },
                onItemClickListener = object : OnItemClickListener {
                    override fun onClick(media: Media, position: Int, adapter: FilePickerAdapter) {
                        if(!media.file.isDirectory)
                            adapter.setSelected(position)
                    }
                }
            )
        }

        return binding.root
    }
}