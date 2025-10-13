package com.dc.entities

import kotlinx.serialization.Serializable

@Serializable
data class Post(
    val id_post: Int,
    val title: String,

    val body: String = "",

//    val points : List<LatLon>,

    val latitude: Double,
    val longitude: Double,


    val post_type : PostType,
    val post_tags : List<PostTag> = emptyList(),

    val post_images : List<String> = emptyList(),

//    val post_tags : List<PostTag>
)
