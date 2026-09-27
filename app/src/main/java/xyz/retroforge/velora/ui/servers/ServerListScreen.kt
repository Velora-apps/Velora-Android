package xyz.retroforge.velora.ui.servers

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import xyz.retroforge.velora.network.ApiServer
import xyz.retroforge.velora.ui.theme.VeloraBrand

@Composable
fun ServerListScreen(
    onOpenServer: (ApiServer) -> Unit,
    onOpenFriends: () -> Unit,
    onOpenDms: () -> Unit,
    onLogout: () -> Unit,
    viewModel: ServerListViewModel = viewModel(),
) {
    val servers by viewModel.servers.collectAsState()
    val unreadServerIds by viewModel.unreadServerIds.collectAsState()
    val unreadDmCount by viewModel.unreadDmCount.collectAsState()
    var showJoinDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Velora") },
                actions = {
                    TextButton(onClick = onLogout) { Text("Log out") }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showJoinDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Join server")
            }
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            Column(modifier = Modifier.fillMaxSize()) {
                ListItem(
                    headlineContent = { Text("Friends") },
                    leadingContent = { Icon(Icons.Filled.People, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth().clickable(onClick = onOpenFriends),
                )
                ListItem(
                    headlineContent = { Text("Direct Messages") },
                    leadingContent = { Icon(Icons.Filled.Chat, contentDescription = null) },
                    trailingContent = {
                        if (unreadDmCount > 0) Badge(containerColor = VeloraBrand) { Text(unreadDmCount.toString()) }
                    },
                    modifier = Modifier.fillMaxWidth().clickable(onClick = onOpenDms),
                )
                Divider()

                if (servers.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text("No servers yet — join one with an invite code.")
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                        items(servers) { server ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                onClick = { onOpenServer(server) },
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    if (server.id in unreadServerIds) {
                                        Box(
                                            modifier = Modifier.size(8.dp).background(VeloraBrand, CircleShape),
                                        )
                                        Box(modifier = Modifier.padding(end = 8.dp))
                                    }
                                    Text(server.name, style = MaterialTheme.typography.titleMedium)
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showJoinDialog) {
            JoinServerDialog(
                onDismiss = { showJoinDialog = false },
                onJoin = { code ->
                    viewModel.joinServer(code) { showJoinDialog = false }
                },
            )
        }
    }
}

@Composable
private fun JoinServerDialog(onDismiss: () -> Unit, onJoin: (String) -> Unit) {
    var code by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Join a server") },
        text = {
            OutlinedTextField(
                value = code,
                onValueChange = { code = it },
                label = { Text("Invite code") },
                singleLine = true,
            )
        },
        confirmButton = {
            TextButton(onClick = { onJoin(code) }) { Text("Join") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}
