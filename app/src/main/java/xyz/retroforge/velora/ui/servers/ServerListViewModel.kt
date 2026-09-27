package xyz.retroforge.velora.ui.servers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import xyz.retroforge.velora.network.ApiClient
import xyz.retroforge.velora.network.ApiServer
import xyz.retroforge.velora.network.JoinServerBody

class ServerListViewModel : ViewModel() {

    private val _servers = MutableStateFlow<List<ApiServer>>(emptyList())
    val servers: StateFlow<List<ApiServer>> = _servers

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            runCatching { ApiClient.service.listServers() }
                .onSuccess { resp ->
                    if (resp.success) _servers.value = resp.servers ?: emptyList()
                    else _error.value = resp.error
                }
                .onFailure { _error.value = it.message }
        }
    }

    fun joinServer(inviteCode: String, onDone: (Boolean) -> Unit) {
        viewModelScope.launch {
            runCatching { ApiClient.service.joinServer(JoinServerBody(inviteCode)) }
                .onSuccess { resp ->
                    if (resp.success) {
                        refresh()
                        onDone(true)
                    } else {
                        _error.value = resp.error
                        onDone(false)
                    }
                }
                .onFailure {
                    _error.value = it.message
                    onDone(false)
                }
        }
    }
}
