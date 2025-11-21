package com.bytedance.firstapp.ui.chat

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bytedance.firstapp.databinding.ActivityMainBinding
import com.bytedance.firstapp.ui.session.SessionFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val chatAdapter = ChatAdapter()
    private val viewModel: ChatViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        observeViewModel()

        val sessionId = intent.getStringExtra("SESSION_ID")
        viewModel.loadSession(sessionId)

        binding.buttonSend.setOnClickListener {
            val messageText = binding.editTextMessage.text.toString()
            if (messageText.isNotBlank()) {
                viewModel.sendMessage(messageText)
                binding.editTextMessage.text.clear()
            }
        }
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            // When the navigation icon (session list) is clicked, show the DialogFragment
            SessionFragment().show(supportFragmentManager, "SessionFragment")
        }
    }

    private fun setupRecyclerView() {
        binding.recyclerViewChat.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = chatAdapter
        }
    }

    private fun observeViewModel() {
        viewModel.messages.observe(this) { messages ->
            chatAdapter.submitList(messages)
            binding.recyclerViewChat.post { // Use post to scroll after the adapter has updated
                if (messages.isNotEmpty()) {
                    binding.recyclerViewChat.scrollToPosition(messages.size - 1)
                }
            }
        }
    }
}
