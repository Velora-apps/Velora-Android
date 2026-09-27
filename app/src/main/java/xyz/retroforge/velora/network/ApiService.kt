package xyz.retroforge.velora.network

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

data class LoginBody(val identifier: String, val password: String)
data class RegisterBody(val username: String, val email: String, val password: String)
data class CreateServerBody(val name: String)
data class JoinServerBody(@com.google.gson.annotations.SerializedName("invite_code") val inviteCode: String)
data class SendMessageBody(
    @com.google.gson.annotations.SerializedName("channel_id") val channelId: Int,
    val content: String,
)
data class SendFriendRequestBody(val username: String)
data class FriendRequestIdBody(@com.google.gson.annotations.SerializedName("request_id") val requestId: Int)
data class RemoveFriendBody(@com.google.gson.annotations.SerializedName("user_id") val userId: Int)
data class DmStartBody(@com.google.gson.annotations.SerializedName("user_id") val userId: Int)
data class DmSendBody(
    @com.google.gson.annotations.SerializedName("conversation_id") val conversationId: Int,
    val content: String,
)
data class DmMarkReadBody(@com.google.gson.annotations.SerializedName("conversation_id") val conversationId: Int)
data class MarkReadBody(@com.google.gson.annotations.SerializedName("channel_id") val channelId: Int)
data class TypingPingBody(val kind: String, @com.google.gson.annotations.SerializedName("target_id") val targetId: Int)

interface ApiService {

    // --- Auth ---
    @POST("auth.php?action=login")
    suspend fun login(@Body body: LoginBody): AuthResponse

    @POST("auth.php?action=register")
    suspend fun register(@Body body: RegisterBody): AuthResponse

    @POST("auth.php?action=logout")
    suspend fun logout(): SimpleResponse

    @GET("auth.php?action=me")
    suspend fun me(): AuthResponse

    // --- Servers ---
    @GET("servers.php?action=list")
    suspend fun listServers(): ServersResponse

    @POST("servers.php?action=create")
    suspend fun createServer(@Body body: CreateServerBody): ServerResponse

    @POST("servers.php?action=join")
    suspend fun joinServer(@Body body: JoinServerBody): ServerResponse

    // --- Channels ---
    @GET("channels.php?action=list")
    suspend fun listChannels(@Query("server_id") serverId: Int): ChannelsResponse

    // --- Messages ---
    @GET("messages.php?action=list")
    suspend fun listMessages(
        @Query("channel_id") channelId: Int,
        @Query("after_id") afterId: Int? = null,
    ): MessagesResponse

    @POST("messages.php?action=send")
    suspend fun sendMessage(@Body body: SendMessageBody): MessageResponse

    @POST("messages.php?action=mark_read")
    suspend fun markChannelRead(@Body body: MarkReadBody): SimpleResponse

    @GET("messages.php?action=unread_summary")
    suspend fun unreadSummary(): UnreadSummaryResponse

    // --- Friends ---
    @GET("friends.php?action=list")
    suspend fun listFriends(): FriendsResponse

    @POST("friends.php?action=send_request")
    suspend fun sendFriendRequest(@Body body: SendFriendRequestBody): SendFriendRequestResponse

    @POST("friends.php?action=accept_request")
    suspend fun acceptFriendRequest(@Body body: FriendRequestIdBody): SimpleResponse

    @POST("friends.php?action=remove_request")
    suspend fun removeFriendRequest(@Body body: FriendRequestIdBody): SimpleResponse

    @POST("friends.php?action=remove_friend")
    suspend fun removeFriend(@Body body: RemoveFriendBody): SimpleResponse

    // --- Direct Messages ---
    @GET("dm.php?action=conversations")
    suspend fun listDmConversations(): DmConversationsResponse

    @POST("dm.php?action=start")
    suspend fun startDm(@Body body: DmStartBody): DmStartResponse

    @GET("dm.php?action=messages")
    suspend fun listDmMessages(
        @Query("conversation_id") conversationId: Int,
        @Query("after_id") afterId: Int? = null,
    ): DmMessagesResponse

    @POST("dm.php?action=send")
    suspend fun sendDm(@Body body: DmSendBody): DmMessageResponse

    @POST("dm.php?action=mark_read")
    suspend fun markDmRead(@Body body: DmMarkReadBody): SimpleResponse

    @GET("dm.php?action=search_users")
    suspend fun searchUsers(@Query("q") query: String): UserSearchResponse

    // --- Typing indicators ---
    @POST("typing.php?action=ping")
    suspend fun typingPing(@Body body: TypingPingBody): SimpleResponse

    @GET("typing.php?action=list")
    suspend fun typingList(@Query("kind") kind: String, @Query("target_id") targetId: Int): TypingListResponse
}
