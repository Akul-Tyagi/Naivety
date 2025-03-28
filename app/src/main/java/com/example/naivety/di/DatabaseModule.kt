// app/src/main/java/com/example/naivety/di/DatabaseModule.kt
package com.example.naivety.di

import android.content.Context
import com.example.naivety.data.AppDatabase
import com.example.naivety.data.ReadingDayDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    @Provides
    fun provideReadingDayDao(database: AppDatabase): ReadingDayDao {
        return database.readingDayDao()
    }

}