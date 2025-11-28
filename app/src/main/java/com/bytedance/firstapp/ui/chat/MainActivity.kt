package com.bytedance.firstapp.ui.chat

import android.content.Intent
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
    private lateinit var tokenManager: com.bytedance.firstapp.util.TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tokenManager = com.bytedance.firstapp.util.TokenManager(this)

        if (!tokenManager.isLoggedIn()) {
            startActivity(Intent(this, com.bytedance.firstapp.ui.login.LoginActivity::class.java))
            finish()
            return
        }

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
        updateWelcomeMessage()
    }

    private fun setupToolbar() {
        // Custom toolbar setup
        val btnSessionList = binding.toolbar.findViewById<android.widget.ImageButton>(com.bytedance.firstapp.R.id.btn_session_list)
        btnSessionList.setOnClickListener {
            SessionFragment().show(supportFragmentManager, "SessionFragment")
        }

        val tvTitle = binding.toolbar.findViewById<android.widget.TextView>(com.bytedance.firstapp.R.id.tv_title)
        viewModel.sessionTitle.observe(this) { title ->
            tvTitle.text = title
        }
        
        val tvAvatar = binding.toolbar.findViewById<android.widget.TextView>(com.bytedance.firstapp.R.id.tv_avatar)
        val username = tokenManager.getUsername() ?: "U"
        tvAvatar.text = username.firstOrNull()?.uppercase() ?: "U"
        
        tvAvatar.setOnClickListener {
             // Optional: Logout or Profile
             showLogoutDialog()
        }
    }

    private fun showLogoutDialog() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout") { _, _ ->
                // Call logout API if needed, for now just clear token
                // Ideally call viewModel.logout() which calls API
                tokenManager.clearToken()
                startActivity(Intent(this, com.bytedance.firstapp.ui.login.LoginActivity::class.java))
                finish()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun setupRecyclerView() {
        binding.recyclerViewChat.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = chatAdapter
        }
    }

    private fun observeViewModel() {
        viewModel.messages.observe(this) { messages ->
            android.util.Log.d("MainActivity", "Submitting list to adapter: ${messages.size} items")
            chatAdapter.submitList(messages)
            binding.recyclerViewChat.post {
                if (messages.isNotEmpty()) {
                    binding.recyclerViewChat.scrollToPosition(messages.size - 1)
                }
            }
            updateWelcomeMessage()
        }
    }
    
    private fun updateWelcomeMessage() {
        val messages = viewModel.messages.value
        if (messages.isNullOrEmpty()) {
            binding.tvWelcome.visibility = android.view.View.VISIBLE
            val username = tokenManager.getUsername() ?: "User"
            binding.tvWelcome.text = "Welcome $username"
            binding.recyclerViewChat.visibility = android.view.View.GONE
        } else {
            binding.tvWelcome.visibility = android.view.View.GONE
            binding.recyclerViewChat.visibility = android.view.View.VISIBLE
        }
    }
}
