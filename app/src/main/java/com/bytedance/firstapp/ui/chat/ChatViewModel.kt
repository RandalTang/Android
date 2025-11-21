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
import kotlinx.coroutines.launch

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ChatRepository
    private val _currentSessionId = MutableLiveData<String>()
    val currentSessionId: LiveData<String> = _currentSessionId

    // Expose messages as LiveData, transformed from Repository Flow
    val messages: LiveData<List<ChatMessage>> = _currentSessionId.switchMap { sessionId ->
        Log.d("ChatViewModel", "Switching to session: $sessionId")
        repository.getMessages(sessionId).asLiveData().map { entities ->
            Log.d("ChatViewModel", "Received ${entities.size} messages from DB for session $sessionId")
            entities.map { entity ->
                ChatMessage(
                    text = entity.text,
                    isSentByUser = entity.isSentByUser
                )
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
        } else {
            createNewSession()
        }
    }

    private fun createNewSession() {
        viewModelScope.launch {
            val sessionId = repository.createSession("New Chat ${System.currentTimeMillis()}")
            _currentSessionId.value = sessionId
        }
    }

    fun sendMessage(text: String) {
        val sessionId = _currentSessionId.value ?: return

        viewModelScope.launch {
            try {
                repository.sendMessage(sessionId, text)
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error sending message", e)
                // Optionally handle error state in UI
            }
        }
    }
}
