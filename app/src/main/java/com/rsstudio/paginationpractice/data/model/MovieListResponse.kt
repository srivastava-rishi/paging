package com.rsstudio.paginationpractice.data.model

import com.google.gson.annotations.SerializedName

data class MovieListResponse(
    @SerializedName("Search")
    val movieList: List<MovieResponse>?, // CRASH FIX: Made nullable - API returns null when no results found
    @SerializedName("totalResults")
    val totalCount: String?,
    @SerializedName("Response")
    val response: String
)