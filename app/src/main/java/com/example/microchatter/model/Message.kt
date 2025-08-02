package com.example.microchatter.model

data class Message(
    val id: String,
    val content: String,
    val type: MessageType,
    val timestamp: Long,
    val isSentByMe: Boolean
)

enum class MessageType {
    TEXT, AUDIO
}