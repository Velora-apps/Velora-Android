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
}
