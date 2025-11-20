package com.bytedance.firstapp.data.model

import com.google.gson.annotations.SerializedName

// --- Request Models ---

data class ChatCompletionsRequest(
    val prompt: String,
    val history: List<HistoryMessage>? = null
)

data class HistoryMessage(
    val role: String,
    val content: String
)

// --- Response Models ---

data class ChatCompletionsResponse(
    val status: String,
    val data: ChatData
)

data class ChatData(
    @SerializedName("reply") // Use SerializedName if the JSON key is different from the variable name
    val reply: String
)
