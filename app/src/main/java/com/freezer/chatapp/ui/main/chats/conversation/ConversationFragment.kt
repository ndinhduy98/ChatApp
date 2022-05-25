package com.freezer.chatapp.ui.main.chats.conversation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.RecyclerView
import com.freezer.chatapp.data.viewmodel.ConversationViewModel
import com.freezer.chatapp.databinding.FragmentConversationBinding
import com.freezer.chatapp.ui.BaseFragment
import com.freezer.chatapp.ui.main.afterTextChanged
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
    private val conversationViewModel: ConversationViewModel by activityViewModels {
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
            conversationViewModel.onSendClick()
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

        binding.imageButtonConversationMoreAction.setOnClickListener {
            val moreBottomSheet = MoreBottomSheet()
            moreBottomSheet.show(parentFragmentManager, "MoreBottomSheet")
        }

        binding.editTextConversationContent.afterTextChanged {
            conversationViewModel.conversationContent.postValue(it)
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