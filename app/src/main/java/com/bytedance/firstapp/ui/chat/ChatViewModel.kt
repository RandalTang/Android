package com.bytedance.firstapp.ui.chat

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.bytedance.firstapp.data.local.AppDatabase
import com.bytedance.firstapp.data.model.ChatMessage
import com.bytedance.firstapp.data.repository.ChatRepository
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.launch

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ChatRepository
    private val _currentSessionId = MutableLiveData<String>()
    val currentSessionId: LiveData<String> = _currentSessionId

    private val _sessionTitle = MutableLiveData<String>()
    val sessionTitle: LiveData<String> = _sessionTitle

    private val _streamingText = MutableLiveData<String?>()

    // Expose messages as LiveData, transformed from Repository Flow
    val messages: LiveData<List<ChatMessage>> = _currentSessionId.switchMap { sessionId ->
        Log.d("ChatViewModel", "Switching to session: $sessionId")
        val dbMessages = repository.getMessages(sessionId).asLiveData()
        
        // Combine DB messages with streaming text
        androidx.lifecycle.MediatorLiveData<List<ChatMessage>>().apply {
            var currentDbMessages: List<ChatMessage> = emptyList()
            var currentStreamingText: String? = null

            fun update() {
                val list = currentDbMessages.toMutableList()
                currentStreamingText?.let { text ->
                    if (text.isNotEmpty()) {
                        // Use a specific ID for the streaming message so DiffUtil knows it's the same item
                        list.add(ChatMessage(id = -1L, text = text, isSentByUser = false))
                    }
                }
                value = list
            }

            addSource(dbMessages) { entities ->
                Log.d("ChatViewModel", "Received ${entities.size} messages from DB for session $sessionId")
                currentDbMessages = entities.map { entity ->
                    ChatMessage(
                        id = entity.id,
                        text = entity.text,
                        isSentByUser = entity.isSentByUser
                    )
                }
                update()
            }

            addSource(_streamingText) { text ->
                currentStreamingText = text
                update()
            }
        }
    }

    init {
        val database = AppDatabase.getDatabase(application)
        repository = ChatRepository(database.sessionDao(), database.messageDao())
        
        // Don't create new session here automatically. 
        // MainActivity will call loadSession or createNewSession.
    }

    fun loadSession(sessionId: String?) {
        if (sessionId != null) {
            _currentSessionId.value = sessionId
            viewModelScope.launch {
                val session = repository.getSessionById(sessionId)
                _sessionTitle.value = session?.title ?: "Chat"
            }
        } else {
            createNewSession()
        }
    }

    private fun createNewSession() {
        viewModelScope.launch {
            val tokenManager = com.bytedance.firstapp.util.TokenManager(getApplication())
            val userId = tokenManager.getUserId()
            if (userId != null) {
                val title = "New Chat ${System.currentTimeMillis()}"
                val sessionId = repository.createSession(title, userId)
                _currentSessionId.value = sessionId
                _sessionTitle.value = title
            } else {
                // Handle case where userId is null (should not happen if logged in)
                Log.e("ChatViewModel", "User ID is null, cannot create session")
            }
        }
    }

    fun sendMessage(text: String) {
        val sessionId = _currentSessionId.value ?: return

        viewModelScope.launch {
            try {
                _streamingText.value = "" // Start streaming
                repository.streamMessage(sessionId, text)
                    .conflate()
                    .collect { partialText ->
                        Log.d("ChatViewModel", "Streaming update: ${partialText.length} chars")
                        _streamingText.value = partialText
                        kotlinx.coroutines.delay(50) // Throttle updates to avoid flooding ListAdapter
                    }
                _streamingText.value = null // Stop streaming (DB will have full message)
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error sending message", e)
                _streamingText.value = null // Stop streaming on error
                // Optionally handle error state in UI
            }
        }
    }
}
