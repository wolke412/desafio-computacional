package com.dc.entities

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString

@Serializable
data class User(
    val id: String? = null, // server may return an ID
    val name: String,
    val email: String
)

@Serializable
data class SerializableList<out R >(
    val items: List<R>,
)