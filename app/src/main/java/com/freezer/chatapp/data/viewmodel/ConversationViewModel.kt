package com.freezer.chatapp.data.viewmodel

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.freezer.chatapp.data.model.*
import com.freezer.chatapp.ui.call.AudioCallingActivity
import com.freezer.chatapp.ui.call.VideoCallingActivity
import com.freezer.chatapp.ui.main.chats.conversation.ImageReceiveMessageAdapter
import com.freezer.chatapp.ui.main.chats.conversation.ImageSendMessageAdapter
import com.freezer.chatapp.ui.main.chats.conversation.TextReceiveMessageAdapter
import com.freezer.chatapp.ui.main.chats.conversation.TextSendMessageAdapter
import com.freezer.chatapp.webrtc.CallMode
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.xwray.groupie.GroupieAdapter
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Named

class ConversationViewModel @AssistedInject constructor(
    @Named("user") private val user: FirebaseUser?,
    @Named("database") private val database: FirebaseFirestore,
    @Assisted private val arguments: Bundle,
    @Assisted private val groupieAdapter: GroupieAdapter
) : ViewModel() {
    val conversationName = MutableLiveData<String>()
    var groupId = ""
    val conversationContent = MutableLiveData<String>()

    private var profile: Profile? = null
    private var chatGroup: ChatGroup? = null

    private var chatGroupMembers: ArrayList<String>? = null
    private var chatGroupName: String? = null

    init {
        profile = arguments.getParcelable("profile")
        chatGroup = arguments.getParcelable("chat_group")
        chatGroupMembers = arguments.getStringArrayList("chat_group_members")
        chatGroupName = arguments.getString("chat_group_name")

        CoroutineScope(Dispatchers.IO).launch {
            if (profile != null && user != null) {
                val chatGroups = database.collection("groups")
                    .whereArrayContains("members", user.uid).get().await()
                    .toObjects(ChatGroup::class.java)

                val index = chatGroups.indexOfFirst {
                    it.members!!.contains(profile!!.uid) && it.type == ChatGroupType.PRIVATE_CHAT
                }

                if (index < 0) {
                    // Create group
                    val newChatGroupRef = database.collection("groups").document()
                    val newChatGroup = ChatGroup(
                        id = newChatGroupRef.id,
                        createdBy = user.uid,
                        members = arrayListOf(user.uid, profile!!.uid),
                        profileRef = database.collection("profiles").document(profile!!.uid),
                        type = ChatGroupType.PRIVATE_CHAT
                    )
                    newChatGroupRef.set(newChatGroup)
                    groupId = newChatGroupRef.id
                } else {
                    groupId = chatGroups[index].id
                }
                chatGroupName = "${profile!!.firstName} ${profile!!.lastName}"
            }
            if (chatGroupMembers != null && user != null) {
                CoroutineScope(Dispatchers.IO).launch {
                    // Add current user to group
                    chatGroupMembers!!.add(user.uid)

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
                    groupId = newChatGroupRef.id
                }
            }

            if (chatGroup != null) {
                groupId = chatGroup!!.id
            }
            conversationName.postValue(chatGroupName!!)
            initializeConversationRecyclerView()
        }
    }

    private fun initializeConversationRecyclerView() {
        var isFirstInit = true
        if (user != null)
            database.collection("message")
                .document(groupId)
                .collection("messages")
                .addSnapshotListener { value, _ ->
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
//                        recyclerViewConversation.smoothScrollToPosition(groupieAdapter.itemCount)
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
//                        recyclerViewConversation.smoothScrollToPosition(groupieAdapter.itemCount)
                    }
                }
    }


    fun onVideoCallClick(context: Context) {
        val callingBundle = Bundle()
        profile?.let { callingBundle.putParcelable("profile", it) }
        callingBundle.putString("calling_mode", CallMode.CALL_MODE_OFFER)
        val intent = Intent(context, VideoCallingActivity::class.java)
        intent.putExtras(callingBundle)
        intent.putExtra("group_id", groupId)
        context.startActivity(intent)
    }

    fun onAudioCallClick(context: Context) {
        val callingBundle = Bundle()
        profile?.let { callingBundle.putParcelable("profile", it) }
        callingBundle.putString("calling_mode", CallMode.CALL_MODE_OFFER)
        val intent = Intent(context, AudioCallingActivity::class.java)
        intent.putExtras(callingBundle)
        intent.putExtra("group_id", groupId)
        context.startActivity(intent)
    }

    fun onSendClick() {
        if (TextUtils.isEmpty(conversationContent.value))
            return
        if(user != null) {
            database.collection("message")
                .document(groupId)
                .collection("messages")
                .document()
                .set(
                    TextMessage(
                        text = conversationContent.value!!,
                        sendBy = user.uid,
                        deliveryStatus = DeliveryStatus.SENT
                    )
                )
        }
    }

    @dagger.assisted.AssistedFactory
    interface AssistedFactory{
        fun create(@Named("arguments") arguments: Bundle, @Named("groupie_adapter") groupieAdapter: GroupieAdapter): ConversationViewModel
    }

    companion object {
        fun providesFactory(assistedFactory: AssistedFactory, arguments: Bundle, groupieAdapter: GroupieAdapter): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return assistedFactory.create(arguments, groupieAdapter) as T
            }

        }
    }
}