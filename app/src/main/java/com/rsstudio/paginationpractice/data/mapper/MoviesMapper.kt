package com.rsstudio.paginationpractice.data.mapper

import com.rsstudio.paginationpractice.data.model.MovieResponse
import com.rsstudio.paginationpractice.presentation.MovieItem


// CRASH FIX: Handle nullable list safely
// BEFORE: Expected non-null List, crashed when API returned null
// ISSUE: API returns null for "Search" field when no results found or search fails
// NOW: Returns empty list if input is null, preventing NullPointerException
fun List<MovieResponse>?.toMovieItem() = this?.map {
    MovieItem(
        id = it.id,
        title = it.title,
        description = "",
        imageUrl = it.imageUrl
    )
} ?: emptyList() // Return empty list if movieList is null