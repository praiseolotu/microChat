package com.example.microchatter.ui.chat

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.viewModels

import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.microchatter.databinding.FragmentChatBinding
import com.example.microchatter.model.MessageType
import com.example.microchatter.network.socket.SocketManager
import com.example.microchatter.network.wifip2p.WifiP2PManagerHelper
import com.example.microchatter.utils.AESUtil
import com.example.microchatter.utils.AudioRecorder
import java.io.File

class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ChatViewModel by viewModels()
    private lateinit var wifiHelper: WifiP2PManagerHelper
    private lateinit var socketManager: SocketManager
    private lateinit var audioRecorder: AudioRecorder
    private lateinit var adapter: ChatAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        wifiHelper = WifiP2PManagerHelper(requireContext(), lifecycle)
        socketManager = SocketManager()

        adapter = ChatAdapter()
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        viewModel.messages.observe(viewLifecycleOwner) {
            adapter.submitList(it)
            binding.recyclerView.scrollToPosition(it.size - 1)
        }

        viewModel.connected.observe(viewLifecycleOwner) {
            binding.connectionBanner.setConnected(it)
        }

        binding.sendButton.setOnClickListener {
            val text = binding.messageInput.text.toString().trim()
            if (text.isNotEmpty()) {
                val encrypted = AESUtil.encrypt(text)
                socketManager.send(encrypted)
                viewModel.addMessage(text, MessageType.TEXT, true)
                binding.messageInput.setText("")
            }
        }

        binding.messageInput.doAfterTextChanged {
            binding.sendButton.isEnabled = it?.isNotBlank() == true
        }

        binding.btnConnect.setOnClickListener {
            val firstPeer = viewModel.peers.value?.firstOrNull()
            if (firstPeer != null) {
                wifiHelper.connectToPeer(firstPeer.address) {
                    viewModel.setConnected(true)
                }
            } else {
                Toast.makeText(context, "No peers found", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnDisconnect.setOnClickListener {
            endSession("Disconnected by user.")
        }

        audioRecorder = AudioRecorder(requireContext())
        audioRecorder.onRecordingFinished = { file ->
            val bytes = file.readBytes()
            val encrypted = AESUtil.encrypt(bytes)
            socketManager.send("AUDIO:$encrypted")
            viewModel.addMessage(encrypted, MessageType.AUDIO, true)
        }

        binding.audioButton.setOnLongClickListener {
            audioRecorder.startRecording()
            Toast.makeText(context, "Recording...", Toast.LENGTH_SHORT).show()
            true
        }

        binding.audioButton.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP || event.action == MotionEvent.ACTION_CANCEL) {
                audioRecorder.stopRecording()
            }
            false
        }

        // Start peer discovery and socket server/client
        wifiHelper.discoverPeers { viewModel.updatePeers(it) }
        wifiHelper.onGroupOwnerAvailable { ip, isOwner ->
            if (isOwner) {
                socketManager.startServer { encryptedMsg ->
                    if (encryptedMsg.startsWith("AUDIO:")) {
                        val audioEnc = encryptedMsg.removePrefix("AUDIO:")
                        val decrypted = AESUtil.decryptToBytes(audioEnc)
                        val file = File.createTempFile("recv_", ".aac", requireContext().cacheDir)
                        file.writeBytes(decrypted)
                        val reenc = AESUtil.encrypt(decrypted)
                        viewModel.addMessage(reenc, MessageType.AUDIO, false)
                    } else {
                        val plain = AESUtil.decrypt(encryptedMsg)
                        viewModel.addMessage(plain, MessageType.TEXT, false)
                    }
                }
            } else {
                socketManager.setClientTarget(ip)
            }
        }
    }

    private fun endSession(reason: String) {
        socketManager.stopServer()
        viewModel.clearMessages()
        wifiHelper.disconnect {
            Toast.makeText(context, reason, Toast.LENGTH_SHORT).show()
            viewModel.setConnected(false)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = ChatFragment()
    }
}