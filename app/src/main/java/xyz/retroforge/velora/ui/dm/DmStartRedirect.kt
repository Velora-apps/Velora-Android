package xyz.retroforge.velora.ui.dm

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * The Friends screen only has a user id to go on (friends.php doesn't return
 * a conversation id). This calls dm.php?action=start, which finds-or-creates
 * the conversation, then hands off to the real chat route — same pattern the
 * web client uses when you click "Message" from a friend's profile.
 */
@Composable
fun DmStartRedirect(
    userId: Int,
    fallbackUsername: String,
    onResolved: (conversationId: Int, username: String) -> Unit,
    viewModel: DmListViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
) {
    LaunchedEffect(userId) {
        viewModel.startConversation(userId) { conversationId, username ->
            onResolved(conversationId, username.ifBlank { fallbackUsername })
        }
    }
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}
