// app/src/main/java/com/example/naivety/di/NetworkModule.kt
package com.example.naivety.di


import OpenLibraryApi
import BrowseRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://openlibrary.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideOpenLibraryApi(retrofit: Retrofit): OpenLibraryApi {
        return retrofit.create(OpenLibraryApi::class.java)
    }

    @Provides
    @Singleton
    fun provideBrowseRepository(api: OpenLibraryApi): BrowseRepository {
        return BrowseRepository(api)
    }
}