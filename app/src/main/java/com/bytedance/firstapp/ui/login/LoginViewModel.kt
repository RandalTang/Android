package com.bytedance.firstapp.ui.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.bytedance.firstapp.data.model.LoginRequest
import com.bytedance.firstapp.data.model.LoginResponse
import com.bytedance.firstapp.data.model.RegisterRequest
import com.bytedance.firstapp.data.remote.RetrofitInstance
import com.bytedance.firstapp.util.TokenManager
import kotlinx.coroutines.launch
import retrofit2.HttpException

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val apiService = RetrofitInstance.api
    private val tokenManager = TokenManager(application)

    private val _loginResult = MutableLiveData<Result<Boolean>>()
    val loginResult: LiveData<Result<Boolean>> = _loginResult

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun login(username: String, password: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = apiService.login(LoginRequest(username, password))
                if (response.status == "success") {
                    tokenManager.saveToken(response.token, username)
                    _loginResult.value = Result.success(true)
                } else {
                    _loginResult.value = Result.failure(Exception("Login failed: ${response.status}"))
                }
            } catch (e: Exception) {
                _loginResult.value = Result.failure(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun register(username: String, password: String, phone: String? = null, email: String? = null) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = apiService.register(RegisterRequest(username, password, phone, email))
                if (response.isSuccessful && response.body()?.status == "success") {
                     // Auto login after register if token is present, or just notify success
                     val body = response.body()
                     if (body?.token != null) {
                         tokenManager.saveToken(body.token, username)
                         _loginResult.value = Result.success(true)
                     } else {
                         // If no token returned, maybe just success message, user needs to login?
                         // For now assume auto-login if token present, else treat as success but maybe need login.
                         // Let's assume for this task that register returns token as well or we just login immediately.
                         // If body has token, great.
                         _loginResult.value = Result.success(true)
                     }
                } else {
                    _loginResult.value = Result.failure(Exception("Registration failed"))
                }
            } catch (e: Exception) {
                _loginResult.value = Result.failure(e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}
