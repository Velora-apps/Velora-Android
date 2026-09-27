package xyz.retroforge.velora.ui.dm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import xyz.retroforge.velora.network.ApiClient
import xyz.retroforge.velora.network.ApiDmConversation
import xyz.retroforge.velora.network.ApiUser
import xyz.retroforge.velora.network.DmStartBody

class DmListViewModel : ViewModel() {

    private val _conversations = MutableStateFlow<List<ApiDmConversation>>(emptyList())
    val conversations: StateFlow<List<ApiDmConversation>> = _conversations

    private val _searchResults = MutableStateFlow<List<ApiUser>>(emptyList())
    val searchResults: StateFlow<List<ApiUser>> = _searchResults

    private val _unreadByConversation = MutableStateFlow<Map<Int, Int>>(emptyMap())
    val unreadByConversation: StateFlow<Map<Int, Int>> = _unreadByConversation

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        refresh()
        viewModelScope.launch {
            while (true) {
                pollUnread()
                delay(6000)
            }
        }
    }

    private suspend fun pollUnread() {
        runCatching { ApiClient.service.unreadSummary() }
            .onSuccess { resp ->
                if (resp.success) {
                    _unreadByConversation.value = resp.conversations
                        ?.mapNotNull { (id, count) -> id.toIntOrNull()?.let { it to count } }
                        ?.toMap() ?: emptyMap()
                }
            }
    }

    fun refresh() {
        viewModelScope.launch {
            runCatching { ApiClient.service.listDmConversations() }
                .onSuccess { resp -> if (resp.success) _conversations.value = resp.conversations ?: emptyList() }
                .onFailure { _error.value = it.message }
        }
    }

    fun search(query: String) {
        if (query.length < 2) {
            _searchResults.value = emptyList()
            return
        }
        viewModelScope.launch {
            runCatching { ApiClient.service.searchUsers(query) }
                .onSuccess { resp -> if (resp.success) _searchResults.value = resp.users ?: emptyList() }
        }
    }

    fun startConversation(userId: Int, onStarted: (conversationId: Int, username: String) -> Unit) {
        viewModelScope.launch {
            runCatching { ApiClient.service.startDm(DmStartBody(userId)) }
                .onSuccess { resp ->
                    val convo = resp.conversation
                    val other = resp.otherUser
                    if (resp.success && convo != null && other != null) {
                        refresh()
                        onStarted(convo.id, other.username)
                    } else {
                        _error.value = resp.error
                    }
                }
                .onFailure { _error.value = it.message }
        }
    }
}
