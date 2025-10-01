package com.dc.entities

import android.graphics.Bitmap
import com.dc.coordinates.LatLon

data class Post(
    val id: Int,
    val title: String,
    val body: String,

    val points : List<LatLon>,


//    val post_tags : List<PostTag>
    val post_type : PostType,
    val post_tags : List<PostTag> = emptyList(),

    val images_names : List<String> = emptyList(),
    val images_bytes : List<Bitmap> = emptyList()
)
