package xyz.retroforge.velora.ui.servers

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import xyz.retroforge.velora.network.ApiServer

@Composable
fun ServerListScreen(
    onOpenServer: (ApiServer) -> Unit,
    onLogout: () -> Unit,
    viewModel: ServerListViewModel = viewModel(),
) {
    val servers by viewModel.servers.collectAsState()
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
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(server.name, style = MaterialTheme.typography.titleMedium)
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
