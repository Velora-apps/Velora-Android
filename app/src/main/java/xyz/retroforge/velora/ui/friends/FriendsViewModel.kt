package xyz.retroforge.velora.ui.friends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import xyz.retroforge.velora.network.ApiClient
import xyz.retroforge.velora.network.ApiFriend
import xyz.retroforge.velora.network.ApiFriendRequest
import xyz.retroforge.velora.network.FriendRequestIdBody
import xyz.retroforge.velora.network.RemoveFriendBody
import xyz.retroforge.velora.network.SendFriendRequestBody

class FriendsViewModel : ViewModel() {

    private val _friends = MutableStateFlow<List<ApiFriend>>(emptyList())
    val friends: StateFlow<List<ApiFriend>> = _friends

    private val _incoming = MutableStateFlow<List<ApiFriendRequest>>(emptyList())
    val incoming: StateFlow<List<ApiFriendRequest>> = _incoming

    private val _outgoing = MutableStateFlow<List<ApiFriendRequest>>(emptyList())
    val outgoing: StateFlow<List<ApiFriendRequest>> = _outgoing

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _status = MutableStateFlow<String?>(null)
    val status: StateFlow<String?> = _status

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            runCatching { ApiClient.service.listFriends() }
                .onSuccess { resp ->
                    if (resp.success) {
                        _friends.value = resp.friends ?: emptyList()
                        _incoming.value = resp.incoming ?: emptyList()
                        _outgoing.value = resp.outgoing ?: emptyList()
                    } else {
                        _error.value = resp.error
                    }
                }
                .onFailure { _error.value = it.message }
        }
    }

    fun sendRequest(username: String) {
        if (username.isBlank()) return
        viewModelScope.launch {
            runCatching { ApiClient.service.sendFriendRequest(SendFriendRequestBody(username.trim())) }
                .onSuccess { resp ->
                    if (resp.success) {
                        _status.value = if (resp.accepted == true) "You are now friends with $username" else "Friend request sent"
                        refresh()
                    } else {
                        _error.value = resp.error
                    }
                }
                .onFailure { _error.value = it.message }
        }
    }

    fun acceptRequest(requestId: Int) {
        viewModelScope.launch {
            runCatching { ApiClient.service.acceptFriendRequest(FriendRequestIdBody(requestId)) }
                .onSuccess { if (it.success) refresh() else _error.value = it.error }
        }
    }

    fun removeRequest(requestId: Int) {
        viewModelScope.launch {
            runCatching { ApiClient.service.removeFriendRequest(FriendRequestIdBody(requestId)) }
                .onSuccess { if (it.success) refresh() else _error.value = it.error }
        }
    }

    fun removeFriend(userId: Int) {
        viewModelScope.launch {
            runCatching { ApiClient.service.removeFriend(RemoveFriendBody(userId)) }
                .onSuccess { if (it.success) refresh() else _error.value = it.error }
        }
    }

    fun clearStatus() {
        _status.value = null
        _error.value = null
    }
}
