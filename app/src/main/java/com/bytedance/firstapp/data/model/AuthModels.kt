package com.bytedance.firstapp.data.model

data class LoginRequest(
    val username: String,
    val password: String
)

data class User(
    val id: Long,
    val username: String,
    val phone: String?,
    val email: String?
)

data class LoginResponse(
    val token: String,
    val user: User,
    val status: String
)

data class RegisterRequest(
    val username: String,
    val password: String,
    val phone: String? = null,
    val email: String? = null
)

data class LogoutRequest(
    val token: String
)

data class LogoutResponse(
    val status: String,
    val message: String
)
