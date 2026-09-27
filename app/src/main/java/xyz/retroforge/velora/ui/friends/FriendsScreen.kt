package xyz.retroforge.velora.ui.friends

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import xyz.retroforge.velora.network.ApiFriend
import xyz.retroforge.velora.network.ApiFriendRequest
import xyz.retroforge.velora.ui.theme.VeloraGreen
import xyz.retroforge.velora.ui.theme.VeloraRed

@Composable
fun FriendsScreen(
    onBack: () -> Unit,
    onOpenDm: (userId: Int, username: String) -> Unit,
    viewModel: FriendsViewModel = viewModel(),
) {
    val friends by viewModel.friends.collectAsState()
    val incoming by viewModel.incoming.collectAsState()
    val outgoing by viewModel.outgoing.collectAsState()
    val status by viewModel.status.collectAsState()
    val error by viewModel.error.collectAsState()

    var tab by remember { mutableStateOf(0) }
    var showAddDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(status, error) {
        val message = status ?: error
        if (message != null) {
            scope.launch { snackbarHostState.showSnackbar(message) }
            viewModel.clearStatus()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Friends") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Filled.PersonAdd, contentDescription = "Add friend")
            }
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            TabRow(selectedTabIndex = tab) {
                Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("All (${friends.size})") })
                Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("Pending (${incoming.size + outgoing.size})") })
            }

            when (tab) {
                0 -> {
                    if (friends.isEmpty()) {
                        EmptyState("No friends yet. Tap + to add one by username.")
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxWidth()) {
                            items(friends, key = { it.id }) { friend ->
                                FriendRow(friend, onOpenDm = { onOpenDm(friend.id, friend.username) }, onRemove = { viewModel.removeFriend(friend.id) })
                                Divider()
                            }
                        }
                    }
                }
                1 -> {
                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        if (incoming.isNotEmpty()) {
                            item { SectionLabel("Incoming") }
                            items(incoming, key = { "in${it.id}" }) { req ->
                                RequestRow(
                                    req,
                                    onAccept = { viewModel.acceptRequest(req.id) },
                                    onDecline = { viewModel.removeRequest(req.id) },
                                )
                                Divider()
                            }
                        }
                        if (outgoing.isNotEmpty()) {
                            item { SectionLabel("Outgoing") }
                            items(outgoing, key = { "out${it.id}" }) { req ->
                                RequestRow(req, onAccept = null, onDecline = { viewModel.removeRequest(req.id) })
                                Divider()
                            }
                        }
                        if (incoming.isEmpty() && outgoing.isEmpty()) {
                            item { EmptyState("No pending friend requests.") }
                        }
                    }
                }
            }
        }

        if (showAddDialog) {
            AddFriendDialog(
                onDismiss = { showAddDialog = false },
                onSend = { username ->
                    viewModel.sendRequest(username)
                    showAddDialog = false
                },
            )
        }
    }
}

@Composable
private fun FriendRow(friend: ApiFriend, onOpenDm: () -> Unit, onRemove: () -> Unit) {
    ListItem(
        headlineContent = { Text(friend.username) },
        supportingContent = { Text(friend.status ?: "offline", color = if (friend.status == "online") VeloraGreen else MaterialTheme.colorScheme.onSurfaceVariant) },
        trailingContent = {
            Row {
                TextButton(onClick = onOpenDm) { Text("Message") }
                IconButton(onClick = onRemove) {
                    Icon(Icons.Filled.Close, contentDescription = "Remove friend", tint = VeloraRed)
                }
            }
        },
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun RequestRow(request: ApiFriendRequest, onAccept: (() -> Unit)?, onDecline: () -> Unit) {
    ListItem(
        headlineContent = { Text(request.username ?: "Unknown user") },
        trailingContent = {
            Row {
                if (onAccept != null) {
                    IconButton(onClick = onAccept) {
                        Icon(Icons.Filled.Check, contentDescription = "Accept", tint = VeloraGreen)
                    }
                }
                IconButton(onClick = onDecline) {
                    Icon(Icons.Filled.Close, contentDescription = "Decline / cancel", tint = VeloraRed)
                }
            }
        },
    )
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 4.dp),
    )
}

@Composable
private fun EmptyState(text: String) {
    Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Text(text, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun AddFriendDialog(onDismiss: () -> Unit, onSend: (String) -> Unit) {
    var username by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add friend") },
        text = {
            Column {
                Text("You can add a friend by their exact username.", style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") },
                    singleLine = true,
                )
            }
        },
        confirmButton = { TextButton(onClick = { onSend(username) }) { Text("Send request") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
