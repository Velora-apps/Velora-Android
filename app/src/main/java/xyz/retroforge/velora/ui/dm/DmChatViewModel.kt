package xyz.retroforge.velora.ui.dm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import xyz.retroforge.velora.network.ApiClient
import xyz.retroforge.velora.network.ApiDmMessage
import xyz.retroforge.velora.network.DmMarkReadBody
import xyz.retroforge.velora.network.DmSendBody
import xyz.retroforge.velora.network.TypingPingBody

/** Mirrors ChatViewModel's 3s polling, plus typing ping/list for DMs. */
class DmChatViewModel : ViewModel() {

    private val _messages = MutableStateFlow<List<ApiDmMessage>>(emptyList())
    val messages: StateFlow<List<ApiDmMessage>> = _messages

    private val _typingUsername = MutableStateFlow<String?>(null)
    val typingUsername: StateFlow<String?> = _typingUsername

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private var pollingJob: Job? = null
    private var typingJob: Job? = null
    private var conversationId: Int = -1

    fun start(conversationId: Int) {
        if (this.conversationId == conversationId && pollingJob != null) return
        this.conversationId = conversationId
        _messages.value = emptyList()
        pollingJob?.cancel()
        typingJob?.cancel()

        pollingJob = viewModelScope.launch {
            loadInitial(conversationId)
            markRead(conversationId)
            while (true) {
                delay(3000)
                pollNew(conversationId)
            }
        }
        typingJob = viewModelScope.launch {
            while (true) {
                delay(2000)
                pollTyping(conversationId)
            }
        }
    }

    private suspend fun loadInitial(conversationId: Int) {
        runCatching { ApiClient.service.listDmMessages(conversationId) }
            .onSuccess { resp ->
                if (resp.success) _messages.value = resp.messages ?: emptyList()
                else _error.value = resp.error
            }
            .onFailure { _error.value = it.message }
    }

    private suspend fun pollNew(conversationId: Int) {
        val lastId = _messages.value.lastOrNull()?.id
        runCatching { ApiClient.service.listDmMessages(conversationId, afterId = lastId) }
            .onSuccess { resp ->
                if (resp.success && !resp.messages.isNullOrEmpty()) {
                    _messages.value = _messages.value + resp.messages
                    markRead(conversationId)
                }
            }
    }

    private suspend fun pollTyping(conversationId: Int) {
        runCatching { ApiClient.service.typingList("dm", conversationId) }
            .onSuccess { resp -> _typingUsername.value = resp.typing?.firstOrNull()?.username }
    }

    private suspend fun markRead(conversationId: Int) {
        runCatching { ApiClient.service.markDmRead(DmMarkReadBody(conversationId)) }
    }

    private var lastTypingPingAt = 0L

    /** Throttled: the ping TTL server-side is 6s, so once every ~2s is plenty. */
    fun notifyTyping() {
        val id = conversationId
        if (id == -1) return
        val now = System.currentTimeMillis()
        if (now - lastTypingPingAt < 2000) return
        lastTypingPingAt = now
        viewModelScope.launch {
            runCatching { ApiClient.service.typingPing(TypingPingBody("dm", id)) }
        }
    }

    fun send(text: String) {
        if (text.isBlank() || conversationId == -1) return
        viewModelScope.launch {
            runCatching { ApiClient.service.sendDm(DmSendBody(conversationId, text)) }
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
