package com.bytedance.firstapp.data.model

import java.util.UUID

data class Session(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val lastMessagePreview: String
)
