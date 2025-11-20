package com.bytedance.firstapp.ui.session

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bytedance.firstapp.databinding.ActivitySessionBinding
import com.bytedance.firstapp.ui.chat.MainActivity

class SessionActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySessionBinding
    private val viewModel: SessionViewModel by viewModels()
    private lateinit var sessionAdapter: SessionAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySessionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        observeViewModel()

        binding.buttonNewChat.setOnClickListener {
            // Start a new chat session
            val intent = Intent(this, MainActivity::class.java)
            // You might want to add flags or an ID to indicate it's a new session
            startActivity(intent)
        }
    }

    private fun setupRecyclerView() {
        sessionAdapter = SessionAdapter { session ->
            // Handle click on a session item
            val intent = Intent(this, MainActivity::class.java)
            // Pass the session ID to the chat activity
            intent.putExtra("SESSION_ID", session.id)
            startActivity(intent)
            Toast.makeText(this, "Opening session: ${session.title}", Toast.LENGTH_SHORT).show()
        }
        binding.recyclerViewSessions.apply {
            layoutManager = LinearLayoutManager(this@SessionActivity)
            adapter = sessionAdapter
        }
    }

    private fun observeViewModel() {
        viewModel.sessions.observe(this) { sessions ->
            sessionAdapter.submitList(sessions)
        }
    }
}
