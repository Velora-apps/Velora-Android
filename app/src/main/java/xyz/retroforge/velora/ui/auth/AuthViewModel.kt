package xyz.retroforge.velora.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import xyz.retroforge.velora.network.ApiClient
import xyz.retroforge.velora.network.ApiUser
import xyz.retroforge.velora.network.LoginBody
import xyz.retroforge.velora.network.RegisterBody

sealed interface AuthState {
    data object CheckingSession : AuthState
    data object LoggedOut : AuthState
    data class LoggedIn(val user: ApiUser) : AuthState
}

class AuthViewModel : ViewModel() {

    private val _state = MutableStateFlow<AuthState>(AuthState.CheckingSession)
    val state: StateFlow<AuthState> = _state

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    init {
        checkSession()
    }

    fun checkSession() {
        viewModelScope.launch {
            runCatching { ApiClient.service.me() }
                .onSuccess { resp ->
                    _state.value = if (resp.success && resp.user != null) {
                        AuthState.LoggedIn(resp.user)
                    } else {
                        AuthState.LoggedOut
                    }
                }
                .onFailure { _state.value = AuthState.LoggedOut }
        }
    }

    fun login(identifier: String, password: String) {
        if (identifier.isBlank() || password.isBlank()) {
            _error.value = "Enter your username/email and password."
            return
        }
        _loading.value = true
        _error.value = null
        viewModelScope.launch {
            runCatching { ApiClient.service.login(LoginBody(identifier, password)) }
                .onSuccess { resp ->
                    if (resp.success && resp.user != null) {
                        _state.value = AuthState.LoggedIn(resp.user)
                    } else {
                        _error.value = resp.error ?: "Login failed."
                    }
                }
                .onFailure { _error.value = "Couldn't reach Velora: ${it.message}" }
            _loading.value = false
        }
    }

    fun register(username: String, email: String, password: String) {
        if (username.isBlank() || email.isBlank() || password.isBlank()) {
            _error.value = "Fill in every field."
            return
        }
        _loading.value = true
        _error.value = null
        viewModelScope.launch {
            runCatching { ApiClient.service.register(RegisterBody(username, email, password)) }
                .onSuccess { resp ->
                    if (resp.success && resp.user != null) {
                        _state.value = AuthState.LoggedIn(resp.user)
                    } else {
                        _error.value = resp.error ?: "Registration failed."
                    }
                }
                .onFailure { _error.value = "Couldn't reach Velora: ${it.message}" }
            _loading.value = false
        }
    }

    fun logout() {
        viewModelScope.launch {
            runCatching { ApiClient.service.logout() }
            ApiClient.cookieJar.clear()
            _state.value = AuthState.LoggedOut
        }
    }
}
