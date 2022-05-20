package com.freezer.chatapp.ui.main.chats.conversation

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.RecyclerView
import com.freezer.chatapp.data.model.*
import com.freezer.chatapp.data.viewmodel.ConversationViewModel
import com.freezer.chatapp.databinding.FragmentConversationBinding
import com.freezer.chatapp.ui.BaseFragment
import com.freezer.chatapp.ui.call.AudioCallingActivity
import com.freezer.chatapp.ui.call.VideoCallingActivity
import com.freezer.chatapp.webrtc.CallMode
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.xwray.groupie.GroupieAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ConversationFragment : BaseFragment() {

    private var _binding: FragmentConversationBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override var bottomNavigationViewVisibility = View.GONE

    private lateinit var conversationViewModel: ConversationViewModel

    private lateinit var database: FirebaseFirestore
    private lateinit var user: FirebaseUser

    private lateinit var groupieAdapter: GroupieAdapter

    private lateinit var recyclerViewConversation: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentConversationBinding.inflate(inflater, container, false)

        recyclerViewConversation = binding.recyclerViewConversation

        // Get profile
        val bundle = arguments
        val profile = bundle?.getParcelable<Profile>("profile")
        val chatGroup = bundle?.getParcelable<ChatGroup>("chat_group")

        val chatGroupMembers = bundle?.getStringArrayList("chat_group_members")
        val chatGroupName = bundle?.getString("chat_group_name")

        // Retrieve group
        database = Firebase.firestore
        user = FirebaseAuth.getInstance().currentUser!!

        conversationViewModel = ConversationViewModel()

        binding.conversationViewModel = conversationViewModel
        binding.lifecycleOwner = viewLifecycleOwner

        if(profile != null) {
            CoroutineScope(Dispatchers.IO).launch {
                val chatGroups = database.collection("groups")
                    .whereArrayContains("members", user.uid).get().await()
                    .toObjects(ChatGroup::class.java)

                val index = chatGroups.indexOfFirst {
                    it.members!!.contains(profile.uid) && it.type == ChatGroupType.PRIVATE_CHAT
                }

                if (index < 0) {
                    // Create group
                    val newChatGroupRef = database.collection("groups").document()
                    val newChatGroup = ChatGroup(
                        id = newChatGroupRef.id,
                        createdBy = user.uid,
                        members = arrayListOf(user.uid, profile.uid),
                        type = ChatGroupType.PRIVATE_CHAT
                    )
                    newChatGroupRef.set(newChatGroup)
                    conversationViewModel.groupId = newChatGroupRef.id
                } else {
                    conversationViewModel.groupId = chatGroups[index].id
                }
                conversationViewModel.conversationName.postValue("${profile.firstName} ${profile.lastName}")
                initializeConversationRecyclerView()
            }
        }

        if(chatGroupMembers != null) {
            CoroutineScope(Dispatchers.IO).launch {
                // Add current user to group
                chatGroupMembers.add(user.uid)

                // Create group
                val newChatGroupRef = database.collection("groups").document()
                val newChatGroup = ChatGroup(
                    id = newChatGroupRef.id,
                    createdBy = user.uid,
                    members = chatGroupMembers,
                    type = ChatGroupType.GROUP_CHAT,
                    name = chatGroupName
                )
                newChatGroupRef.set(newChatGroup)
                conversationViewModel.groupId = newChatGroupRef.id
                initializeConversationRecyclerView()
            }
        }

        if (chatGroup != null) {
            conversationViewModel.groupId = chatGroup.id
            if (chatGroup.type == ChatGroupType.PRIVATE_CHAT) {
                val targetProfileUid = chatGroup.members?.find { it != user.uid }
                // Retrieve contacts list
            } else {

            }
        }

        binding.imageButtonConversationSend.setOnClickListener {
            if(TextUtils.isEmpty(binding.editTextConversationContent.text))
                return@setOnClickListener
            database.collection("message")
                .document(conversationViewModel.groupId)
                .collection("messages")
                .document()
                .set(
                    TextMessage(
                        text = binding.editTextConversationContent.text.toString(),
                        sendBy = user.uid,
                        deliveryStatus = DeliveryStatus.SENT
                    )
                )
            binding.editTextConversationContent.text?.clear()
        }

        binding.editTextConversationContent.setOnFocusChangeListener{ _, hasFocus ->
            if(hasFocus) {
                binding.recyclerViewConversation.smoothScrollToPosition(groupieAdapter.itemCount)
            }
        }

        binding.imageButtonAudioCall.setOnClickListener {
            val callingBundle = Bundle()
            profile?.let { callingBundle.putParcelable("profile", it) }
            callingBundle.putString("calling_mode", CallMode.CALL_MODE_OFFER)
            val intent = Intent(requireContext(), AudioCallingActivity::class.java)
            intent.putExtras(callingBundle)
            intent.putExtra("group_id", conversationViewModel.groupId)
            startActivity(intent)
        }

        binding.imageButtonVideoCall.setOnClickListener {
            val callingBundle = Bundle()
            profile?.let { callingBundle.putParcelable("profile", it) }
            callingBundle.putString("calling_mode", CallMode.CALL_MODE_OFFER)
            val intent = Intent(requireContext(), VideoCallingActivity::class.java)
            intent.putExtras(callingBundle)
            intent.putExtra("group_id", conversationViewModel.groupId)
            startActivity(intent)
        }

        binding.imageButtonBack.setOnClickListener {
            NavHostFragment.findNavController(this).popBackStack()
        }

        binding.imageButtonBack.setOnClickListener {
            NavHostFragment.findNavController(this).popBackStack()
        }
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        if(this::groupieAdapter.isInitialized) {
            binding.recyclerViewConversation
                .smoothScrollToPosition(groupieAdapter.itemCount)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private suspend fun initializeConversationRecyclerView() {
        groupieAdapter = GroupieAdapter()

        withContext(Dispatchers.Main) {
            _binding!!.recyclerViewConversation.adapter = groupieAdapter
        }

        var isFirstInit = true

        database.collection("message")
            .document(conversationViewModel.groupId)
            .collection("messages")
            .addSnapshotListener { value, error ->
                if (isFirstInit) {
                    isFirstInit = false
                    val messages = value?.documents
                    messages?.forEach { message ->
                        if (message.get("type") == MessageType.TEXT) {
                            val textMessage = message.toObject(TextMessage::class.java)!!
                            if (textMessage.sendBy == user.uid) {
                                groupieAdapter.add(TextSendMessageAdapter(textMessage))
                            } else {
                                groupieAdapter.add(TextReceiveMessageAdapter(textMessage))
                            }
                        } else {
                            val imageMessage = message.toObject(ImageMessage::class.java)!!
                            if (imageMessage.sendBy == user.uid) {
                                groupieAdapter.add(ImageSendMessageAdapter(imageMessage))
                            } else {
                                groupieAdapter.add(ImageReceiveMessageAdapter(imageMessage))
                            }
                        }
                    }
                    recyclerViewConversation.smoothScrollToPosition(groupieAdapter.itemCount)
                } else {
                    val newMessage = value?.documentChanges?.get(0)?.document
                    newMessage?.let {
                        if (newMessage.get("createdAt") == null) // Wait for server timestamp
                            return@addSnapshotListener
                        if (newMessage.get("type") == MessageType.TEXT) {
                            val textMessage = newMessage.toObject(TextMessage::class.java)
                            if (textMessage.sendBy == user.uid) {
                                groupieAdapter.add(TextSendMessageAdapter(textMessage))
                            } else {
                                groupieAdapter.add(TextReceiveMessageAdapter(textMessage))
                            }
                        } else {
                            val imageMessage = newMessage.toObject(ImageMessage::class.java)
                            if (imageMessage.sendBy == user.uid) {
                                groupieAdapter.add(ImageSendMessageAdapter(imageMessage))
                            } else {
                                groupieAdapter.add(ImageReceiveMessageAdapter(imageMessage))
                            }
                        }
                    }
                    recyclerViewConversation.smoothScrollToPosition(groupieAdapter.itemCount)
                }
            }
    }
}