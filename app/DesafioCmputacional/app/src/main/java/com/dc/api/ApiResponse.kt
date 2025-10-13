package com.dc.api

import kotlinx.serialization.Serializable

/**
 * generic class to represent an error response from the API.
 */
@Serializable
data class ApiError(
    val error: String,
//    val code: String?
)

/**
 * represents the result of an API call.
 *
 *  either
 *  [Success] with data of type [T] or a
 *  [Error] with an [ApiError] object.
 */
sealed class ApiResponse<out T> {
    data class Success<out T>(val data: T) : ApiResponse<T>()
    data class Error(val error: ApiError) : ApiResponse<Nothing>()
}

val ApiResponse<*>.isSuccess: Boolean
    get() = this is ApiResponse.Success

val ApiResponse<*>.isError: Boolean
    get() = this is ApiResponse.Error

/**
 * tipo o unwrap do rust
 */
fun <T> ApiResponse<T>.getContent(): T? =
    (this as? ApiResponse.Success)?.data

/**
 * pra pegar o erro só
 */
fun ApiResponse<*>.getError(): ApiError? =
    (this as? ApiResponse.Error)?.error
