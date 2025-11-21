package com.bytedance.firstapp.data.repository

import com.bytedance.firstapp.data.local.MessageDao
import com.bytedance.firstapp.data.local.MessageEntity
import com.bytedance.firstapp.data.local.SessionDao
import com.bytedance.firstapp.data.local.SessionEntity
import com.bytedance.firstapp.data.model.ChatCompletionsRequest
import com.bytedance.firstapp.data.model.HistoryMessage
import com.bytedance.firstapp.data.remote.RetrofitInstance
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class ChatRepository(
    private val sessionDao: SessionDao,
    private val messageDao: MessageDao
) {

    fun getSessions(): Flow<List<SessionEntity>> = sessionDao.getAllSessions()

    fun getMessages(sessionId: String): Flow<List<MessageEntity>> = messageDao.getMessagesForSession(sessionId)

    suspend fun createSession(title: String): String {
        val session = SessionEntity(
            id = java.util.UUID.randomUUID().toString(),
            title = title,
            lastMessagePreview = ""
        )
        sessionDao.insertSession(session)
        return session.id
    }

    suspend fun sendMessage(sessionId: String, text: String) {
        android.util.Log.d("ChatRepository", "sendMessage called for sessionId: $sessionId, text: $text")
        // 1. Save user message
        val userMessage = MessageEntity(
            sessionId = sessionId,
            text = text,
            isSentByUser = true
        )
        try {
            messageDao.insertMessage(userMessage)
            android.util.Log.d("ChatRepository", "User message inserted into DB")
        } catch (e: Exception) {
            android.util.Log.e("ChatRepository", "Failed to insert user message", e)
            throw e
        }

        // Update session preview
        updateSessionPreview(sessionId, text)

        // 2. Prepare API request
        // We need to get previous messages for history context
        val historyMessages = messageDao.getMessagesForSession(sessionId).first().takeLast(10).map {
            HistoryMessage(
                role = if (it.isSentByUser) "user" else "assistant",
                content = it.text
            )
        }

        val request = ChatCompletionsRequest(
            prompt = text,
            history = if (historyMessages.isNotEmpty()) historyMessages else null
        )

        // 3. Call API
        try {
            android.util.Log.d("ChatRepository", "Calling API...")
            val response = RetrofitInstance.api.getChatCompletions(request)
            val aiReplyText = response.data.reply
            android.util.Log.d("ChatRepository", "API response received: $aiReplyText")

            // 4. Save AI response
            val aiMessage = MessageEntity(
                sessionId = sessionId,
                text = aiReplyText,
                isSentByUser = false
            )
            messageDao.insertMessage(aiMessage)
            android.util.Log.d("ChatRepository", "AI message inserted into DB")
            updateSessionPreview(sessionId, aiReplyText)

        } catch (e: Exception) {
            android.util.Log.e("ChatRepository", "API call or AI message insert failed", e)
            // Handle error (maybe save an error message or throw)
            // For now, we'll just rethrow or log
            throw e
        }
    }

    private suspend fun updateSessionPreview(sessionId: String, preview: String) {
        val session = sessionDao.getSessionById(sessionId)
        session?.let {
            sessionDao.updateSession(it.copy(lastMessagePreview = preview))
        }
    }
}
