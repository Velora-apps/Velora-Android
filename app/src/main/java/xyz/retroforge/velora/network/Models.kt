package xyz.retroforge.velora.network

import com.google.gson.annotations.SerializedName

// Generic envelope every Velora API endpoint responds with:
// { "success": true, ...fields }  or  { "success": false, "error": "..." }

data class ApiUser(
    val id: Int,
    val username: String,
    val email: String? = null,
    @SerializedName("avatar_url") val avatarUrl: String? = null,
    val status: String? = null,
    val bio: String? = null,
    val pronouns: String? = null,
    @SerializedName("accent_color") val accentColor: String? = null,
    @SerializedName("is_admin") val isAdmin: Int? = null,
    @SerializedName("is_bot") val isBot: Int? = null,
    @SerializedName("site_badge") val siteBadge: String? = null,
    @SerializedName("nitro_until") val nitroUntil: String? = null,
)

data class ApiServer(
    val id: Int,
    val name: String,
    @SerializedName("owner_id") val ownerId: Int,
    @SerializedName("icon_url") val iconUrl: String? = null,
    @SerializedName("invite_code") val inviteCode: String? = null,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("my_permissions") val myPermissions: Long? = null,
)

data class ApiChannel(
    val id: Int,
    @SerializedName("server_id") val serverId: Int,
    val name: String,
    val type: String, // "text" | "voice"
    val topic: String? = null,
    val position: Int? = null,
    @SerializedName("category_id") val categoryId: Int? = null,
)

data class ApiMessage(
    val id: Int,
    @SerializedName("channel_id") val channelId: Int,
    @SerializedName("user_id") val userId: Int,
    val content: String,
    @SerializedName("edited_at") val editedAt: String? = null,
    @SerializedName("created_at") val createdAt: String,
    val username: String,
    @SerializedName("avatar_url") val avatarUrl: String? = null,
    @SerializedName("site_badge") val siteBadge: String? = null,
    @SerializedName("is_bot") val isBot: Int? = null,
)

data class ApiFriendRequest(
    val id: Int,
    @SerializedName("user_id") val userId: Int? = null,
    @SerializedName("friend_id") val friendId: Int? = null,
    val username: String? = null,
    @SerializedName("avatar_url") val avatarUrl: String? = null,
)

data class ApiFriend(
    val id: Int,
    val username: String,
    @SerializedName("avatar_url") val avatarUrl: String? = null,
    val status: String? = null,
    @SerializedName("group_id") val groupId: Int? = null,
)

data class ApiDmConversation(
    val id: Int,
    @SerializedName("other_user") val otherUser: ApiUser,
    @SerializedName("last_message") val lastMessage: ApiDmLastMessage? = null,
)

data class ApiDmLastMessage(val content: String? = null, @SerializedName("created_at") val createdAt: String? = null)

data class ApiDmMessage(
    val id: Int,
    @SerializedName("conversation_id") val conversationId: Int,
    @SerializedName("user_id") val userId: Int,
    val content: String,
    @SerializedName("created_at") val createdAt: String,
    val username: String? = null,
    @SerializedName("avatar_url") val avatarUrl: String? = null,
)

data class ApiTypingUser(@SerializedName("user_id") val userId: Int, val username: String)

data class AuthResponse(val success: Boolean, val error: String? = null, val user: ApiUser? = null)
data class FriendsResponse(
    val success: Boolean,
    val error: String? = null,
    val friends: List<ApiFriend>? = null,
    val incoming: List<ApiFriendRequest>? = null,
    val outgoing: List<ApiFriendRequest>? = null,
)
data class SendFriendRequestResponse(val success: Boolean, val error: String? = null, val accepted: Boolean? = null)
data class DmConversationsResponse(val success: Boolean, val error: String? = null, val conversations: List<ApiDmConversation>? = null)
data class DmStartResponse(val success: Boolean, val error: String? = null, val conversation: ApiDmConversation? = null, @SerializedName("other_user") val otherUser: ApiUser? = null)
data class DmMessagesResponse(val success: Boolean, val error: String? = null, val messages: List<ApiDmMessage>? = null)
data class DmMessageResponse(val success: Boolean, val error: String? = null, val message: ApiDmMessage? = null)
data class UserSearchResponse(val success: Boolean, val error: String? = null, val users: List<ApiUser>? = null)
data class TypingListResponse(val success: Boolean, val error: String? = null, val typing: List<ApiTypingUser>? = null)
data class UnreadSummaryResponse(
    val success: Boolean,
    val error: String? = null,
    val channels: Map<String, Int>? = null,
    val conversations: Map<String, Int>? = null,
    val servers: Map<String, Boolean>? = null,
)
data class ServersResponse(val success: Boolean, val error: String? = null, val servers: List<ApiServer>? = null)
data class ServerResponse(val success: Boolean, val error: String? = null, val server: ApiServer? = null)
data class ChannelsResponse(val success: Boolean, val error: String? = null, val channels: List<ApiChannel>? = null)
data class MessagesResponse(val success: Boolean, val error: String? = null, val messages: List<ApiMessage>? = null)
data class MessageResponse(val success: Boolean, val error: String? = null, val message: ApiMessage? = null)
data class SimpleResponse(val success: Boolean, val error: String? = null)
