package com.rsstudio.paginationpractice.common

import retrofit2.Response
import java.io.IOException

sealed class ApiResult<out T> {
    data class Success<out T>(val data: T) : ApiResult<T>()
    data class Failure(val error: ApiError) : ApiResult<Nothing>()
}

sealed class ApiError {
    data class HttpError(val code: Int, val message: String) : ApiError()
    data class NetworkError(val exception: IOException) : ApiError()
    data class UnknownError(val throwable: Throwable) : ApiError()
}

suspend fun <T> safeApiCall(apiCall: suspend () -> Response<T>): ApiResult<T> {
    return try {
        val response = apiCall()
        if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                ApiResult.Success(body)
            } else {
                ApiResult.Failure(ApiError.UnknownError(Throwable("Response body is null")))
            }
        } else {
            ApiResult.Failure(
                ApiError.HttpError(
                    code = response.code(),
                    message = response.message()
                )
            )
        }
    } catch (e: IOException) {
        ApiResult.Failure(ApiError.NetworkError(e))
    } catch (e: Exception) {
        ApiResult.Failure(ApiError.UnknownError(e))
    }
}
