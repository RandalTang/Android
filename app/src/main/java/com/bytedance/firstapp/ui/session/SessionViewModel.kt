package com.bytedance.firstapp.ui.session

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.map
import com.bytedance.firstapp.data.local.AppDatabase
import com.bytedance.firstapp.data.model.Session
import com.bytedance.firstapp.data.repository.ChatRepository

class SessionViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ChatRepository

    val sessions: LiveData<List<Session>>

    init {
        val database = AppDatabase.getDatabase(application)
        repository = ChatRepository(database.sessionDao(), database.messageDao())

        sessions = repository.getSessions().asLiveData().map { entities ->
            entities.map { entity ->
                Session(
                    id = entity.id,
                    title = entity.title,
                    lastMessagePreview = entity.lastMessagePreview
                )
            }
        }
    }
}
