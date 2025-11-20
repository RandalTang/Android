package com.bytedance.firstapp.ui.chat

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bytedance.firstapp.data.model.ChatMessage
import com.bytedance.firstapp.data.model.ChatCompletionsRequest
import com.bytedance.firstapp.data.model.HistoryMessage
import com.bytedance.firstapp.data.remote.RetrofitInstance
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {

    private val _messages = MutableLiveData<List<ChatMessage>>(emptyList())
    val messages: LiveData<List<ChatMessage>> = _messages

    fun sendMessage(text: String) {
        // 1. Add user message to UI immediately
        val userMessage = ChatMessage(text, isSentByUser = true)
        _messages.value = _messages.value.orEmpty() + userMessage

        // 2. Prepare request for the API
        val currentHistory = _messages.value.orEmpty().dropLast(1) // Exclude the user's latest message
            .map {
                val role = if (it.isSentByUser) "user" else "assistant"
                HistoryMessage(role, it.text)
            }

        val request = ChatCompletionsRequest(
            prompt = text,
            history = if (currentHistory.isNotEmpty()) currentHistory else null
        )

        // 3. Launch a coroutine to call the API
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.getChatCompletions(request)
                val aiReply = ChatMessage(response.data.reply, isSentByUser = false)
                _messages.value = _messages.value.orEmpty() + aiReply
            } catch (e: Exception) {
                Log.e("ChatViewModel", "API call failed", e)
                val errorReply = ChatMessage("抱歉，网络出错了: ${e.message}", isSentByUser = false)
                _messages.value = _messages.value.orEmpty() + errorReply
            }
        }
    }
}
