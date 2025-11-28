package com.bytedance.firstapp.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bytedance.firstapp.databinding.ActivityRegisterBinding
import com.bytedance.firstapp.ui.chat.MainActivity

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val viewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
        observeViewModel()
    }

    private fun setupListeners() {
        binding.buttonRegister.setOnClickListener {
            val username = binding.editTextUsername.text.toString()
            val password = binding.editTextPassword.text.toString()
            val phone = binding.editTextPhone.text.toString().takeIf { it.isNotBlank() }
            val email = binding.editTextEmail.text.toString().takeIf { it.isNotBlank() }

            if (validateInput(username, password)) {
                viewModel.register(username, password, phone, email)
            }
        }

        binding.buttonBackToLogin.setOnClickListener {
            finish() // Go back to LoginActivity
        }
    }

    private fun validateInput(username: String, password: String): Boolean {
        if (username.isBlank()) {
            binding.editTextUsername.error = "请输入用户名"
            return false
        }
        if (password.isBlank()) {
            binding.editTextPassword.error = "请输入密码"
            return false
        }
        return true
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.buttonRegister.isEnabled = !isLoading
            binding.buttonBackToLogin.isEnabled = !isLoading
        }

        viewModel.loginResult.observe(this) { result ->
            result.onSuccess {
                // On successful registration (and auto-login), go to Main Activity
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }.onFailure { e ->
                Toast.makeText(this, "注册失败: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
