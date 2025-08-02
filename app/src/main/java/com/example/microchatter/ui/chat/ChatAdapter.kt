package com.example.microchatter.ui.chat

import android.net.Uri
import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.microchatter.databinding.ItemMessageBinding
import com.example.microchatter.model.Message
import com.example.microchatter.model.MessageType
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.common.MediaItem

import java.io.File

class ChatAdapter : ListAdapter<Message, ChatAdapter.MsgHolder>(Diff) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MsgHolder {
        val binding = ItemMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MsgHolder(binding)
    }

    override fun onBindViewHolder(holder: MsgHolder, position: Int) {
        val message = getItem(position)
        val ctx = holder.itemView.context
        val binding = holder.binding

        binding.messageText.text = if (message.type == MessageType.TEXT) {
            message.content
        } else "🎧 Play Audio"

        binding.messageText.setOnClickListener {
            if (message.type == MessageType.AUDIO) {
                val decoded = Base64.decode(message.content, Base64.DEFAULT)
                val tempFile = File.createTempFile("play_", ".aac", ctx.cacheDir)
                tempFile.writeBytes(decoded)

                val player = ExoPlayer.Builder(ctx).build()
                val mediaItem = MediaItem.fromUri(Uri.fromFile(tempFile))
                player.setMediaItem(mediaItem)
                player.prepare()
                player.play()
            }
        }

        binding.timestamp.text = android.text.format.DateFormat.format("hh:mm a", message.timestamp)
        binding.messageContainer.gravity =
            if (message.isSentByMe) android.view.Gravity.END else android.view.Gravity.START
    }

    class MsgHolder(val binding: ItemMessageBinding) : RecyclerView.ViewHolder(binding.root)

    companion object Diff : DiffUtil.ItemCallback<Message>() {
        override fun areItemsTheSame(old: Message, new: Message) = old.id == new.id
        override fun areContentsTheSame(old: Message, new: Message) = old == new
    }
}




