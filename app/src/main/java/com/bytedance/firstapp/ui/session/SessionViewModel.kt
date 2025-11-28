package com.bytedance.firstapp.ui.session

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.bytedance.firstapp.data.local.AppDatabase
import com.bytedance.firstapp.data.model.Session
import com.bytedance.firstapp.data.repository.ChatRepository
import kotlinx.coroutines.launch

class SessionViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ChatRepository

    val sessions: LiveData<List<Session>>

    init {
        val database = AppDatabase.getDatabase(application)
        repository = ChatRepository(database.sessionDao(), database.messageDao())
        val tokenManager = com.bytedance.firstapp.util.TokenManager(application)
        val userId = tokenManager.getUserId()

        sessions = if (userId != null) {
            repository.getSessions(userId).asLiveData().map { entities ->
                entities.map { entity ->
                    Session(
                        id = entity.id,
                        title = entity.title,
                        lastMessagePreview = entity.lastMessagePreview
                    )
                }
            }
        } else {
            androidx.lifecycle.MutableLiveData(emptyList())
        }
    }
    fun deleteSession(session: Session) {
        viewModelScope.launch {
            repository.deleteSession(session.id)
        }
    }

    fun renameSession(session: Session, newTitle: String) {
        viewModelScope.launch {
            repository.renameSession(session.id, newTitle)
        }
    }
}
