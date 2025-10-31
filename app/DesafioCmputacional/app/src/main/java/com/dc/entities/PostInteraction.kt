package com.dc.entities

import kotlinx.serialization.Serializable

@Serializable
data class PostInteraction(
    val id_post: Int,
    val id_user: Int,
    val interaction : String?,
    val created_at: String? = "",
)
{
    fun getInteractionType(): PostInteractionType {
        return when (interaction) {
            "UP" -> PostInteractionType.UPVOTE
            "DOWN" -> PostInteractionType.DOWNVOTE
            else -> PostInteractionType.NONE
        }
    }
}

