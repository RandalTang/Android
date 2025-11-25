package com.bytedance.firstapp.data.model

data class ChatMessage(
    val id: Long = 0,
    val text: String,
    val isSentByUser: Boolean
)
