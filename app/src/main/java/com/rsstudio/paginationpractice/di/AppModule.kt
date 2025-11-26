package com.rsstudio.paginationpractice.di

import com.google.gson.GsonBuilder
import com.rsstudio.paginationpractice.data.AppApiClientService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Singleton
    @Provides
    fun provideAppApiClientService(): AppApiClientService =
        Retrofit.Builder()
            .baseUrl(Constant.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
            .build()
            .create(AppApiClientService::class.java)
}

object Constant {
    const val BASE_URL = "https://www.omdbapi.com/"
}