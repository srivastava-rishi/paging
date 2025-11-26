package com.rsstudio.paginationpractice.data

import com.rsstudio.paginationpractice.data.model.MovieListResponse
import com.rsstudio.paginationpractice.data.model.MovieResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query


interface AppApiClientService {
    @GET(".")
    suspend fun searchMovies(
        @Query("apikey") apikey: String,
        @Query("s") query: String,
        @Query("page") page: String
    ): Response<MovieListResponse>
}