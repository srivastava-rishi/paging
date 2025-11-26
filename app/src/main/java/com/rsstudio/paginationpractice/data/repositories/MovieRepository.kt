package com.rsstudio.paginationpractice.data.repositories

import com.rsstudio.paginationpractice.common.ApiResult
import com.rsstudio.paginationpractice.common.safeApiCall
import com.rsstudio.paginationpractice.data.AppApiClientService
import javax.inject.Inject

class MovieRepository @Inject constructor(
    private val apiClientService: AppApiClientService
) {
    suspend fun searchMovies(
        apiKey: String,
        query: String,
        pageNo: String
    ) = safeApiCall {
        apiClientService.searchMovies(
            apiKey,
            query,
            pageNo
        )
    }
}