package com.freezer.chatapp.ui.main.chats.conversation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.RecyclerView
import com.freezer.chatapp.R
import com.freezer.chatapp.data.viewmodel.ConversationViewModel
import com.freezer.chatapp.databinding.FragmentConversationBinding
import com.freezer.chatapp.ui.BaseFragment
import com.freezer.chatapp.ui.main.afterTextChanged
import com.github.file_picker.FileType
import com.github.file_picker.ListDirection
import com.github.file_picker.adapter.FilePickerAdapter
import com.github.file_picker.extension.showFilePicker
import com.github.file_picker.listener.OnItemClickListener
import com.github.file_picker.listener.OnSubmitClickListener
import com.github.file_picker.model.Media
import com.xwray.groupie.GroupieAdapter
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ConversationFragment : BaseFragment() {

    private var _binding: FragmentConversationBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override var bottomNavigationViewVisibility = View.GONE

    @Inject
    lateinit var conversationViewModelAssistedFactory: ConversationViewModel.AssistedFactory
    private val conversationViewModel: ConversationViewModel by viewModels {
        ConversationViewModel.providesFactory(conversationViewModelAssistedFactory, arguments!!, groupieAdapter)
    }

    private val groupieAdapter = GroupieAdapter()

    private lateinit var recyclerViewConversation: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentConversationBinding.inflate(inflater, container, false)

        binding.conversationViewModel = conversationViewModel
        binding.lifecycleOwner = viewLifecycleOwner

        recyclerViewConversation = binding.recyclerViewConversation

        recyclerViewConversation.adapter = groupieAdapter

        binding.imageButtonConversationSend.setOnClickListener {
            binding.editTextConversationContent.text?.clear()
            recyclerViewConversation.smoothScrollToPosition(groupieAdapter.itemCount + 1)
        }

        binding.imageButtonAudioCall.setOnClickListener {
            conversationViewModel.onAudioCallClick(requireContext())
        }

        binding.imageButtonVideoCall.setOnClickListener {
            conversationViewModel.onVideoCallClick(requireContext())
        }

        binding.imageButtonBack.setOnClickListener {
            NavHostFragment.findNavController(this).popBackStack()
        }

//        binding.imageButtonConversationMoreAction.setOnClickListener {
//            val conversationMoreBottomSheet = ConversationMoreBottomSheet()
//            conversationMoreBottomSheet.show(parentFragmentManager, "MoreBottomSheet")
//        }

        binding.editTextConversationContent.afterTextChanged {
            conversationViewModel.conversationContent.postValue(it)
            if(it.isEmpty()) conversationViewModel.isConversationContentEmpty.postValue(true)
            else conversationViewModel.isConversationContentEmpty.postValue(false)
        }

        binding.imageButtonConversationSendImage.setOnClickListener {
            showFilePicker(
                limitItemSelection = 1,
                listDirection = ListDirection.LTR,
                fileType = FileType.IMAGE,
                accentColor = ContextCompat.getColor(requireContext(), R.color.purple_700),
                titleTextColor = ContextCompat.getColor(requireContext(), R.color.purple_700),
                onSubmitClickListener = object : OnSubmitClickListener {
                    override fun onClick(files: List<Media>) {
                        conversationViewModel.sendImageMessage(files)
                    }
                },
                onItemClickListener = object : OnItemClickListener {
                    override fun onClick(media: Media, position: Int, adapter: FilePickerAdapter) {
                        if (!media.file.isDirectory) {
                            adapter.setSelected(position)
                        }
                    }
                }
            )
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        binding.recyclerViewConversation
            .smoothScrollToPosition(groupieAdapter.itemCount)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}