package xyz.retroforge.velora.ui.dm

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.Divider
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import xyz.retroforge.velora.network.ApiDmConversation
import xyz.retroforge.velora.ui.theme.VeloraBrand

@Composable
fun DmListScreen(
    onBack: () -> Unit,
    onOpenConversation: (conversationId: Int, username: String) -> Unit,
    viewModel: DmListViewModel = viewModel(),
) {
    val conversations by viewModel.conversations.collectAsState()
    val unreadByConversation by viewModel.unreadByConversation.collectAsState()
    var showNewDmDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.refresh() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Direct Messages") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showNewDmDialog = true }) {
                Icon(Icons.Filled.PersonSearch, contentDescription = "New message")
            }
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (conversations.isEmpty()) {
                Column(modifier = Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center) {
                    Text("No conversations yet. Tap the search icon to message someone.")
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(conversations, key = { it.id }) { convo ->
                        ConversationRow(
                            convo,
                            unreadCount = unreadByConversation[convo.id] ?: 0,
                            onClick = { onOpenConversation(convo.id, convo.otherUser.username) },
                        )
                        Divider()
                    }
                }
            }
        }

        if (showNewDmDialog) {
            NewDmDialog(
                viewModel = viewModel,
                onDismiss = { showNewDmDialog = false },
                onPicked = { userId ->
                    viewModel.startConversation(userId) { conversationId, username ->
                        showNewDmDialog = false
                        onOpenConversation(conversationId, username)
                    }
                },
            )
        }
    }
}

@Composable
private fun ConversationRow(convo: ApiDmConversation, unreadCount: Int, onClick: () -> Unit) {
    ListItem(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        leadingContent = {
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape).background(VeloraBrand),
                contentAlignment = Alignment.Center,
            ) {
                Text(convo.otherUser.username.take(1).uppercase(), color = androidx.compose.ui.graphics.Color.White)
            }
        },
        headlineContent = {
            Text(convo.otherUser.username, fontWeight = if (unreadCount > 0) androidx.compose.ui.text.font.FontWeight.Bold else null)
        },
        supportingContent = {
            Text(
                convo.lastMessage?.content ?: "No messages yet",
                maxLines = 1,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        trailingContent = {
            if (unreadCount > 0) {
                Badge(containerColor = VeloraBrand) { Text(unreadCount.coerceAtMost(99).toString()) }
            }
        },
    )
}

@Composable
private fun NewDmDialog(viewModel: DmListViewModel, onDismiss: () -> Unit, onPicked: (Int) -> Unit) {
    var query by remember { mutableStateOf("") }
    val results by viewModel.searchResults.collectAsState()

    LaunchedEffect(query) { viewModel.search(query) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New message") },
        text = {
            Column {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("Search username") },
                    singleLine = true,
                )
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    results.forEach { user ->
                        ListItem(
                            headlineContent = { Text(user.username) },
                            leadingContent = { Icon(Icons.Filled.Chat, contentDescription = null) },
                            modifier = Modifier.clickable { onPicked(user.id) },
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
