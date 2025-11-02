package com.dc.api

import android.graphics.Bitmap
import android.util.Log
import com.dc.entities.E_CreatePostBody
import com.dc.entities.Post
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
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

import androidx.lifecycle.LifecycleCoroutineScope
import com.dc.entities.PostInteraction
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.Serializable
import kotlinx.coroutines.launch

object ApiWrapper {

    var jwtToken: String? = null

    private val client = HttpClient(CIO)
    {
        // usado para debugging
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

//    public const val HOSTNAME = "http://192.168.11.115:44444"
//    public const val HOSTNAME = "http://10.1.1.7:44444"
    public const val HOSTNAME = "http://10.0.2.2:44444"
    private const val BASE_URL = "$HOSTNAME/api/v1"

    /**
     * A generic function to safely execute API calls from a Fragment or Activity.
     * It handles launching a coroutine, executing the network request on a background
     * thread, and delivering the result back on the main thread.
     *
     * @param T The type of the expected successful response content.
     * @param scope The lifecycleScope from the calling Fragment/Activity.
     * @param apiCall A suspend function that returns an ApiResponse.
     * @param onSuccess A lambda executed on the main thread with the success data.
     * @param onError A lambda executed on the main thread with the error data.
     */
    fun <T : Any> call(
        scope: LifecycleCoroutineScope,
        apiCall: suspend () -> ApiResponse<T>,
        onSuccess: (data: T) -> Unit,
        onError: (error: ApiError) -> Unit
    ) {
        scope.launch(Dispatchers.IO) {
            try {
                val response = apiCall()
                withContext(Dispatchers.Main) {
                    when (response) {
                        is ApiResponse.Success -> onSuccess(response.data)
                        is ApiResponse.Error -> onError(response.error)
                    }
                }
            } catch (e: Exception) {
                Log.e("ApiWrapper", "API call failed with exception: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    onError(ApiError("Connection failed: ${e.message}"))
                }
            }
        }
    }


    /**
     */
    private suspend inline fun <reified T> getResponse(response: HttpResponse): ApiResponse<T> {
        return try {
            if (response.status.isSuccess()) {
                ApiResponse.Success(response.body())
            } else {
                ApiResponse.Error(response.body())
            }
        } catch (e: Exception) {
            Log.e("ApiWrapper", "Error processing response: ${e.message}")
            ApiResponse.Error(
                ApiError("Failed to parse response: ${e.message}"))
        }
    }

    suspend fun createUser(user: User): User {
        return client.post("$BASE_URL/users/create") {
            contentType(ContentType.Application.Json)
            setBody(user)
//            jwtToken?.let { header("Authorization", "Bearer $it") }
        }.body()
    }

    @Serializable
    private data class LoginBody(val email: String, val password: String)

    suspend fun tryLogin(email: String, password: String): ApiResponse<User> {
        val res = client.post("$BASE_URL/users/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginBody(email, password))
//            jwtToken?.let { header("Authorization", "Bearer $it") }
        }

        return getResponse(res)
    }

    /**
     * The server should create the post and return the new post object, or an error.
     * @param post The data for the new post.
     * @return An ApiResponse containing either the created Post or an ApiError.
     */
    suspend fun createPost(post: E_CreatePostBody): ApiResponse<Post> {
        try {
            val res: HttpResponse = client.post("$BASE_URL/posts") {
                contentType(ContentType.Application.Json)
                // jwtToken?.let { header("Authorization", "Bearer $it") }
                setBody(post)
            }

            val rawResponse = res.body<String>()
            Log.d("ApiWrapper", "Raw response from createPost: $rawResponse")

            return getResponse<Post>(res)
        }

        catch (e: Exception ) {
            return ApiResponse.Error(ApiError("Failed to create post: ${e.message}"))
        }
    }

    /**
     * This sends the image as multipart/form-data.
     */
    suspend fun uploadPostImage(postId: Int, image: Bitmap) : ApiResponse<Unit> {

        val stream = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        val byteArray = stream.toByteArray()

        val res = client.post("$BASE_URL/posts/$postId/upload-image") {
            // jwtToken?.let { header("Authorization", "Bearer $it") }
            setBody(MultiPartFormDataContent(
                formData {
                    append("image", byteArray, Headers.build {
                        append(HttpHeaders.ContentType, "image/jpeg")
                        append(HttpHeaders.ContentDisposition, "filename=\"image.jpg\"")
                    })
                }
            ))
        }

        return getResponse(res)
    }

    /**
     *  register a user interaction
     * @param postId The ID of the post to interact with.
     * @param interaction A string representing the interaction (e.g., "like").
     * @return An ApiResponse indicating success or failure.
     */
    suspend fun postInteraction(postId: Int, userId: Int, interaction: String): ApiResponse<PostInteraction> {
        val res = client.post("$BASE_URL/posts/$postId/interaction") {
            contentType(ContentType.Application.Json)
            // jwtToken?.let { header("Authorization", "Bearer $it") }
            setBody(
                PostInteraction(
                    postId,
                    userId,
                    interaction
                )
            )
        }

        return getResponse(res)
    }

    /**
     *  fetches a user interaction
     * @param postId The ID of the post to interact with.
     * @param userId The ID of the user.
     * @return An ApiResponse indicating success or failure.
     */
    @Serializable
    data class SubjectUser(val id_user: Int)

    suspend fun fetchPostInteraction(postId: Int, userId: Int): ApiResponse<PostInteraction> {
        val res = client.get("$BASE_URL/posts/$postId/interaction") {
            contentType(ContentType.Application.Json)
            // jwtToken?.let { header("Authorization", "Bearer $it") }
            setBody(SubjectUser(userId))
        }

        return getResponse(res)
    }

    suspend fun fetchPreviewPosts(): ApiResponse<List<Post>> {
        val res = client.get("$BASE_URL/posts/preview") {
            contentType(ContentType.Application.Json)
            // jwtToken?.let { header("Authorization", "Bearer $it") }
        }

        return getResponse(res)
    }

    suspend fun getPostById(id: Int): ApiResponse<Post> {
        val res = client.get("$BASE_URL/posts/$id") {
            contentType(ContentType.Application.Json)
            // jwtToken?.let { header("Authorization", "Bearer $it") }
        }

        return getResponse(res)
    }



}