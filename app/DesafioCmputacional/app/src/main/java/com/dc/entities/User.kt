package com.dc.entities

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString

@Serializable
data class User(
    val id: Int,
    val name: String,
    val email: String
)