package com.bytedance.firstapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bytedance.firstapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val chatMessages = mutableListOf<ChatMessage>()
    private lateinit var chatAdapter: ChatAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        loadInitialMessages()

        binding.buttonSend.setOnClickListener {
            val messageText = binding.editTextMessage.text.toString()
            if (messageText.isNotBlank()) {
                sendMessage(messageText)
            }
        }
    }

    private fun setupRecyclerView() {
        chatAdapter = ChatAdapter(chatMessages)
        binding.recyclerViewChat.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = chatAdapter
        }
    }

    private fun loadInitialMessages() {
        // Simulate a few initial messages
        chatMessages.add(ChatMessage("你好！今天过得怎么样？", isSentByUser = false))
        chatMessages.add(ChatMessage("我很好，谢谢！你呢？", isSentByUser = true))
        chatMessages.add(ChatMessage("我也很好。", isSentByUser = false))
        chatAdapter.notifyDataSetChanged()
    }

    private fun sendMessage(text: String) {
        // Add user's message to the list
        chatMessages.add(ChatMessage(text, isSentByUser = true))
        chatAdapter.notifyItemInserted(chatMessages.size - 1)
        binding.recyclerViewChat.scrollToPosition(chatMessages.size - 1)
        binding.editTextMessage.text.clear()

        // Simulate a reply after a short delay
        binding.root.postDelayed({
            val replyText = "这是自动回复。"
            chatMessages.add(ChatMessage(replyText, isSentByUser = false))
            chatAdapter.notifyItemInserted(chatMessages.size - 1)
            binding.recyclerViewChat.scrollToPosition(chatMessages.size - 1)
        }, 1000)
    }
}