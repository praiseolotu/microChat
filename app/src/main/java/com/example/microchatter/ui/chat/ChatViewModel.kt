package com.example.microchatter.ui.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.microchatter.model.Message
import com.example.microchatter.model.MessageType
import com.example.microchatter.model.PeerDevice
import java.util.*

class ChatViewModel : ViewModel() {

    private val _messages = MutableLiveData<List<Message>>(emptyList())
    val messages: LiveData<List<Message>> = _messages

    private val _peers = MutableLiveData<List<PeerDevice>>(emptyList())
    val peers: LiveData<List<PeerDevice>> = _peers

    private val _connected = MutableLiveData(false)
    val connected: LiveData<Boolean> = _connected

    fun addMessage(content: String, type: MessageType, sentByMe: Boolean) {
        val newMsg = Message(UUID.randomUUID().toString(), content, type, System.currentTimeMillis(), sentByMe)
        _messages.value = _messages.value?.plus(newMsg)
    }

    fun clearMessages() {
        _messages.value = emptyList()
    }

    fun updatePeers(peers: List<PeerDevice>) {
        _peers.value = peers
    }

    fun setConnected(state: Boolean) {
        _connected.value = state
    }
}