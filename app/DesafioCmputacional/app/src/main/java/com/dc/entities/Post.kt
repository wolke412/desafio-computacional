package com.dc.entities

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.Serializable

@Serializable
data class Post(
    val id_post: Int,
    val title: String,

    val body: String = "",

    val latitude: Double,
    val longitude: Double,

    val upvote_count : Int? = 0,
    val downvote_count : Int? = 0,

    val post_type : PostType,
    val post_tags : List<PostTag> = emptyList(),

    val post_images : List<String> = emptyList(),


    val user_interaction : PostInteractionType = PostInteractionType.NONE,
)
