package com.dc.entities

import android.graphics.Bitmap
import kotlinx.serialization.Serializable

data class E_CreatePost(
    val userId : Int = 1,
    val title: String,
    val body: String,
    val latitude: Double,
    val longitude: Double,
    val image: Bitmap?,
    val type: PostType
)

@Serializable
data class E_CreatePostBody(
    val id_user : Int = 1,
    val title: String,
    val body: String,
    val latitude: Double,
    val longitude: Double,
    val post_type: PostType
)

fun E_CreatePost.toCreatePostBody(): E_CreatePostBody {
    return E_CreatePostBody(
        id_user = this.userId,
        title = this.title,
        body = this.body,
        latitude = this.latitude,
        longitude = this.longitude,
        post_type = this.type
    )
}
