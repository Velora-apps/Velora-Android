package xyz.retroforge.velora

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import xyz.retroforge.velora.network.ApiClient
import xyz.retroforge.velora.ui.auth.AuthState
import xyz.retroforge.velora.ui.auth.AuthViewModel
import xyz.retroforge.velora.ui.auth.LoginScreen
import xyz.retroforge.velora.ui.chat.ChatScreen
import xyz.retroforge.velora.ui.dm.DmChatScreen
import xyz.retroforge.velora.ui.dm.DmListScreen
import xyz.retroforge.velora.ui.friends.FriendsScreen
import xyz.retroforge.velora.ui.servers.ChannelListScreen
import xyz.retroforge.velora.ui.servers.ServerListScreen
import xyz.retroforge.velora.ui.theme.VeloraTheme
import java.net.URLDecoder
import java.net.URLEncoder

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ApiClient.init(applicationContext)

        setContent {
            VeloraTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    VeloraApp()
                }
            }
        }
    }
}

@androidx.compose.runtime.Composable
fun VeloraApp() {
    val authViewModel: AuthViewModel = viewModel()
    val authState by authViewModel.state.collectAsState()

    when (val state = authState) {
        is AuthState.CheckingSession -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is AuthState.LoggedOut -> {
            LoginScreen(viewModel = authViewModel)
        }
        is AuthState.LoggedIn -> {
            AppNavHost(onLogout = { authViewModel.logout() })
        }
    }
}

@androidx.compose.runtime.Composable
private fun AppNavHost(onLogout: () -> Unit) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "servers") {
        composable("servers") {
            ServerListScreen(
                onOpenServer = { server ->
                    val encodedName = URLEncoder.encode(server.name, "UTF-8")
                    navController.navigate("channels/${server.id}/$encodedName")
                },
                onOpenFriends = { navController.navigate("friends") },
                onOpenDms = { navController.navigate("dms") },
                onLogout = onLogout,
            )
        }
        composable("friends") {
            FriendsScreen(
                onBack = { navController.popBackStack() },
                onOpenDm = { userId, username ->
                    val encodedName = URLEncoder.encode(username, "UTF-8")
                    // Friends screen only knows the user id; dm.php?action=start resolves
                    // (or creates) the conversation, so hand off through a start route.
                    navController.navigate("dm_start/$userId/$encodedName")
                },
            )
        }
        composable("dms") {
            DmListScreen(
                onBack = { navController.popBackStack() },
                onOpenConversation = { conversationId, username ->
                    val encodedName = URLEncoder.encode(username, "UTF-8")
                    navController.navigate("dm_chat/$conversationId/$encodedName")
                },
            )
        }
        composable(
            "dm_start/{userId}/{username}",
            arguments = listOf(
                navArgument("userId") { type = NavType.IntType },
                navArgument("username") { type = NavType.StringType },
            ),
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getInt("userId") ?: 0
            val username = URLDecoder.decode(backStackEntry.arguments?.getString("username") ?: "", "UTF-8")
            xyz.retroforge.velora.ui.dm.DmStartRedirect(
                userId = userId,
                fallbackUsername = username,
                onResolved = { conversationId, resolvedUsername ->
                    val encodedName = URLEncoder.encode(resolvedUsername, "UTF-8")
                    navController.navigate("dm_chat/$conversationId/$encodedName") {
                        popUpTo("friends")
                    }
                },
            )
        }
        composable(
            "dm_chat/{conversationId}/{username}",
            arguments = listOf(
                navArgument("conversationId") { type = NavType.IntType },
                navArgument("username") { type = NavType.StringType },
            ),
        ) { backStackEntry ->
            val conversationId = backStackEntry.arguments?.getInt("conversationId") ?: 0
            val username = URLDecoder.decode(backStackEntry.arguments?.getString("username") ?: "", "UTF-8")
            DmChatScreen(
                conversationId = conversationId,
                otherUsername = username,
                onBack = { navController.popBackStack() },
            )
        }
        composable(
            "channels/{serverId}/{serverName}",
            arguments = listOf(
                navArgument("serverId") { type = NavType.IntType },
                navArgument("serverName") { type = NavType.StringType },
            ),
        ) { backStackEntry ->
            val serverId = backStackEntry.arguments?.getInt("serverId") ?: 0
            val serverName = URLDecoder.decode(
                backStackEntry.arguments?.getString("serverName") ?: "",
                "UTF-8",
            )
            ChannelListScreen(
                serverId = serverId,
                serverName = serverName,
                onOpenChannel = { channel ->
                    val encodedChannelName = URLEncoder.encode(channel.name, "UTF-8")
                    navController.navigate("chat/${channel.id}/$encodedChannelName")
                },
                onBack = { navController.popBackStack() },
            )
        }
        composable(
            "chat/{channelId}/{channelName}",
            arguments = listOf(
                navArgument("channelId") { type = NavType.IntType },
                navArgument("channelName") { type = NavType.StringType },
            ),
        ) { backStackEntry ->
            val channelId = backStackEntry.arguments?.getInt("channelId") ?: 0
            val channelName = URLDecoder.decode(
                backStackEntry.arguments?.getString("channelName") ?: "",
                "UTF-8",
            )
            ChatScreen(
                channelId = channelId,
                channelName = channelName,
                onBack = { navController.popBackStack() },
            )
        }
    }
}
