package xyz.retroforge.velora.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import xyz.retroforge.velora.network.ApiClient
import xyz.retroforge.velora.network.ApiMessage
import xyz.retroforge.velora.network.MarkReadBody
import xyz.retroforge.velora.network.SendMessageBody
import xyz.retroforge.velora.network.TypingPingBody

/**
 * The backend has no WebSocket/SignalR channel (see README: "real-time
 * updates use polling, every 3 seconds"), so this mirrors that exactly:
 * poll ?after_id=<last seen id> on the same cadence as the web client.
 */
class ChatViewModel : ViewModel() {

    private val _messages = MutableStateFlow<List<ApiMessage>>(emptyList())
    val messages: StateFlow<List<ApiMessage>> = _messages

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _typingUsername = MutableStateFlow<String?>(null)
    val typingUsername: StateFlow<String?> = _typingUsername

    private var pollingJob: Job? = null
    private var typingJob: Job? = null
    private var channelId: Int = -1
    private var lastTypingPingAt = 0L

    fun start(channelId: Int) {
        if (this.channelId == channelId && pollingJob != null) return
        this.channelId = channelId
        _messages.value = emptyList()
        pollingJob?.cancel()
        typingJob?.cancel()
        pollingJob = viewModelScope.launch {
            loadInitial(channelId)
            markRead(channelId)
            while (true) {
                delay(3000)
                pollNew(channelId)
            }
        }
        typingJob = viewModelScope.launch {
            while (true) {
                delay(2000)
                runCatching { ApiClient.service.typingList("channel", channelId) }
                    .onSuccess { resp -> _typingUsername.value = resp.typing?.firstOrNull()?.username }
            }
        }
    }

    private suspend fun markRead(channelId: Int) {
        runCatching { ApiClient.service.markChannelRead(MarkReadBody(channelId)) }
    }

    /** Throttled: server-side typing TTL is 6s, so once every ~2s is plenty. */
    fun notifyTyping() {
        if (channelId == -1) return
        val now = System.currentTimeMillis()
        if (now - lastTypingPingAt < 2000) return
        lastTypingPingAt = now
        viewModelScope.launch {
            runCatching { ApiClient.service.typingPing(TypingPingBody("channel", channelId)) }
        }
    }

    private suspend fun loadInitial(channelId: Int) {
        runCatching { ApiClient.service.listMessages(channelId) }
            .onSuccess { resp ->
                if (resp.success) _messages.value = resp.messages ?: emptyList()
                else _error.value = resp.error
            }
            .onFailure { _error.value = it.message }
    }

    private suspend fun pollNew(channelId: Int) {
        val lastId = _messages.value.lastOrNull()?.id
        runCatching { ApiClient.service.listMessages(channelId, afterId = lastId) }
            .onSuccess { resp ->
                if (resp.success && !resp.messages.isNullOrEmpty()) {
                    _messages.value = _messages.value + resp.messages
                    markRead(channelId)
                }
            }
        // Silently ignore transient poll failures - don't spam the error banner.
    }

    fun send(text: String) {
        if (text.isBlank() || channelId == -1) return
        viewModelScope.launch {
            runCatching { ApiClient.service.sendMessage(SendMessageBody(channelId, text)) }
                .onSuccess { resp ->
                    if (resp.success && resp.message != null) {
                        _messages.value = _messages.value + resp.message
                    } else {
                        _error.value = resp.error
                    }
                }
                .onFailure { _error.value = it.message }
        }
    }

    override fun onCleared() {
        pollingJob?.cancel()
        typingJob?.cancel()
    }
}
