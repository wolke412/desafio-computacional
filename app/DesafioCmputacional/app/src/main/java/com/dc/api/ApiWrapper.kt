package com.dc.api

import android.util.Log
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.json.Json
import com.dc.entities.User
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import kotlinx.serialization.builtins.ListSerializer
import java.io.Serializable

object ApiWrapper {

    var jwtToken: String? = null  // You can set this later

    private val client = HttpClient(CIO)
    {
        // Usado para debugging
        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.HEADERS
            filter { request ->
                request.url.host.contains("ktor.io")
            }
            sanitizeHeader { header -> header == HttpHeaders.Authorization }
        }

        install(ContentNegotiation) {
            json(Json { prettyPrint = true; isLenient = true; ignoreUnknownKeys=true })
        }
    }

    private const val BASE_URL = "http://10.0.2.2:8080/api/v1"

    suspend fun createUser(user: User): User {
        return client.post("$BASE_URL/user/create") {
            contentType(ContentType.Application.Json)
            setBody(user)
//            jwtToken?.let { header("Authorization", "Bearer $it") }
        }.body()
    }

    suspend fun findAllUsers(): List<User> {
        Log.d( "QUERYING ALL USERS: ",  "$BASE_URL/prefeitura/users"  )
        val a = client.get("$BASE_URL/prefeitura/users")
        val b = a.body<List<User>>(  )
        return b
    }
}