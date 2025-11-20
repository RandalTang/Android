package com.bytedance.firstapp.data.remote

import com.bytedance.firstapp.data.model.ChatCompletionsRequest
import com.bytedance.firstapp.data.model.ChatCompletionsResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {

    @POST("api/v1/chat/completions")
    suspend fun getChatCompletions(
        @Body request: ChatCompletionsRequest
    ): ChatCompletionsResponse
}
