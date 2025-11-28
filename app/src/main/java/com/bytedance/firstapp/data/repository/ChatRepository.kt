package com.bytedance.firstapp.data.repository

import com.bytedance.firstapp.data.local.MessageDao
import com.bytedance.firstapp.data.local.MessageEntity
import com.bytedance.firstapp.data.local.SessionDao
import com.bytedance.firstapp.data.local.SessionEntity
import com.bytedance.firstapp.data.model.ChatCompletionsRequest
import com.bytedance.firstapp.data.model.HistoryMessage
import com.bytedance.firstapp.data.remote.RetrofitInstance
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

class ChatRepository(
    private val sessionDao: SessionDao,
    private val messageDao: MessageDao
) {

    fun getSessions(userId: String): Flow<List<SessionEntity>> = sessionDao.getSessionsForUser(userId)

    fun getMessages(sessionId: String): Flow<List<MessageEntity>> = messageDao.getMessagesForSession(sessionId)

    suspend fun getSessionById(sessionId: String): SessionEntity? = sessionDao.getSessionById(sessionId)

    suspend fun createSession(title: String, userId: String): String {
        val session = SessionEntity(
            id = java.util.UUID.randomUUID().toString(),
            title = title,
            lastMessagePreview = "",
            userId = userId
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


    fun streamMessage(sessionId: String, text: String): Flow<String> = kotlinx.coroutines.flow.callbackFlow {
        // 1. Save user message
        val userMessage = MessageEntity(
            sessionId = sessionId,
            text = text,
            isSentByUser = true
        )
        try {
            messageDao.insertMessage(userMessage)
            updateSessionPreview(sessionId, text)
        } catch (e: Exception) {
            close(e)
            return@callbackFlow
        }

        // 2. Prepare API request
        val historyMessages = messageDao.getMessagesForSession(sessionId).first().takeLast(10).map {
            HistoryMessage(
                role = if (it.isSentByUser) "user" else "assistant",
                content = it.text
            )
        }

        val requestPayload = ChatCompletionsRequest(
            prompt = text,
            history = if (historyMessages.isNotEmpty()) historyMessages else null
        )

        val request = okhttp3.Request.Builder()
            .url("http://10.0.2.2:8000/api/v1/chat/completions/stream")
            .post(
                com.google.gson.Gson().toJson(requestPayload).toRequestBody("application/json".toMediaTypeOrNull())
            )
            .build()

        val factory = okhttp3.sse.EventSources.createFactory(RetrofitInstance.sseClient)
        val fullResponseBuilder = StringBuilder()

        val eventSourceListener = object : okhttp3.sse.EventSourceListener() {
            override fun onOpen(eventSource: okhttp3.sse.EventSource, response: okhttp3.Response) {
                android.util.Log.d("ChatRepository", "SSE Connection opened")
            }

            override fun onEvent(eventSource: okhttp3.sse.EventSource, id: String?, type: String?, data: String) {
                android.util.Log.d("ChatRepository", "SSE Event received: $data")
                if (data == "[DONE]") {
                    // Stream finished, save full message
                    val fullResponse = fullResponseBuilder.toString()
                    val aiMessage = MessageEntity(
                        sessionId = sessionId,
                        text = fullResponse,
                        isSentByUser = false
                    )
                    kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                        messageDao.insertMessage(aiMessage)
                        updateSessionPreview(sessionId, fullResponse)
                    }
                    close()
                } else if (data.startsWith("Error:")) {
                    close(Exception(data))
                } else {
                    fullResponseBuilder.append(data)
                    val result = trySend(fullResponseBuilder.toString())
                    if (result.isFailure) {
                        android.util.Log.e("ChatRepository", "Failed to send streaming update", result.exceptionOrNull())
                    }
                }
            }

            override fun onClosed(eventSource: okhttp3.sse.EventSource) {
                android.util.Log.d("ChatRepository", "SSE Connection closed")
                close()
            }

            override fun onFailure(eventSource: okhttp3.sse.EventSource, t: Throwable?, response: okhttp3.Response?) {
                android.util.Log.e("ChatRepository", "SSE Failure", t)
                close(t)
            }
        }

        val eventSource = factory.newEventSource(request, eventSourceListener)

        awaitClose {
            eventSource.cancel()
        }
    }.flowOn(kotlinx.coroutines.Dispatchers.IO)

    private suspend fun updateSessionPreview(sessionId: String, preview: String) {
        val session = sessionDao.getSessionById(sessionId)
        session?.let {
            sessionDao.updateSession(it.copy(lastMessagePreview = preview))
        }
    }

    suspend fun deleteSession(sessionId: String) {
        val session = sessionDao.getSessionById(sessionId)
        session?.let {
            sessionDao.deleteSession(it)
        }
    }

    suspend fun renameSession(sessionId: String, newTitle: String) {
        val session = sessionDao.getSessionById(sessionId)
        session?.let {
            sessionDao.updateSession(it.copy(title = newTitle))
        }
    }
}
