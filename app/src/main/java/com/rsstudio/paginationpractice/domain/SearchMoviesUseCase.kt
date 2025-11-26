package com.rsstudio.paginationpractice.domain

import com.rsstudio.paginationpractice.data.repositories.MovieRepository
import javax.inject.Inject

class SearchMoviesUseCase @Inject constructor(
    private val movieRepository: MovieRepository
) {
    suspend  operator fun invoke(
        apiKey: String,
        query: String,
        pageNo: String
    ) = movieRepository.searchMovies(
        apiKey,
        query,
        pageNo
    )
}