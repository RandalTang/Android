package com.bytedance.firstapp.data.remote

import com.bytedance.firstapp.data.model.ChatCompletionsRequest
import com.bytedance.firstapp.data.model.ChatCompletionsResponse
import com.bytedance.firstapp.data.model.LoginRequest
import com.bytedance.firstapp.data.model.LoginResponse
import com.bytedance.firstapp.data.model.LogoutRequest
import com.bytedance.firstapp.data.model.LogoutResponse
import com.bytedance.firstapp.data.model.RegisterRequest
import com.bytedance.firstapp.data.model.SessionRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {

    @POST("api/v1/chat/completions")
    suspend fun getChatCompletions(
        @Body request: ChatCompletionsRequest
    ): ChatCompletionsResponse

    @POST("api/v1/auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("api/v1/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<LoginResponse>

    @POST("api/v1/auth/logout")
    suspend fun logout(@Body request: LogoutRequest): LogoutResponse

    @POST("api/v1/sessions")
    suspend fun createOrUpdateSession(@Body request: SessionRequest): Response<Unit>

    @retrofit2.http.DELETE("api/v1/sessions/{id}")
    suspend fun deleteSession(@retrofit2.http.Path("id") id: String): Response<Unit>
}
