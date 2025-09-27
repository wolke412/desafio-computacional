package com.dc.entities

data class Post(
    val id: Int,
    val title: String,
    val body: String,

    val images: List<String>
)
