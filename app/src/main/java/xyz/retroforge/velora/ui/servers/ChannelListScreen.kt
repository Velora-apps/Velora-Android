package xyz.retroforge.velora.ui.servers

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import xyz.retroforge.velora.network.ApiChannel
import xyz.retroforge.velora.network.ApiClient

class ChannelListViewModel : ViewModel() {
    private val _channels = MutableStateFlow<List<ApiChannel>>(emptyList())
    val channels: StateFlow<List<ApiChannel>> = _channels

    fun load(serverId: Int) {
        viewModelScope.launch {
            runCatching { ApiClient.service.listChannels(serverId) }
                .onSuccess { resp -> if (resp.success) _channels.value = resp.channels ?: emptyList() }
        }
    }
}

@Composable
fun ChannelListScreen(
    serverId: Int,
    serverName: String,
    onOpenChannel: (ApiChannel) -> Unit,
    onBack: () -> Unit,
    viewModel: ChannelListViewModel = viewModel(),
) {
    val channels by viewModel.channels.collectAsState()

    LaunchedEffect(serverId) { viewModel.load(serverId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(serverName) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
            items(channels.filter { it.type == "text" }) { channel ->
                ListItem(
                    headlineContent = { Text(channel.name) },
                    leadingContent = { Text("#") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenChannel(channel) },
                )
            }
        }
    }
}
