package com.abundance.naivety.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ReadingDayDao {
    @Query("SELECT * FROM reading_days ORDER BY date DESC")
    fun getAllReadingDaysFlow(): Flow<kotlin.collections.List<ReadingDay>>

    @Query("SELECT * FROM reading_days WHERE bookId = :bookId")
    fun getReadingDaysForBook(bookId: String): Flow<kotlin.collections.List<ReadingDay>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReadingDay(readingDay: ReadingDay)

    @Query("SELECT * FROM reading_days WHERE date >= :startDate AND date <= :endDate")
    suspend fun getReadingDaysInRange(startDate: Long, endDate: Long): kotlin.collections.List<ReadingDay>

    @Delete
    suspend fun deleteReadingDay(readingDay: ReadingDay)

    @Query("SELECT * FROM reading_days")
    suspend fun getAllReadingDays(): kotlin.collections.List<ReadingDay>

    @Query("SELECT * FROM reading_days WHERE date = :date LIMIT 1")
    suspend fun getReadingDayForDate(date: Long): ReadingDay?

    // Book type specific queries
    @Query("SELECT * FROM reading_days WHERE bookType = :bookType")
    suspend fun getReadingDaysByType(bookType: String): kotlin.collections.List<ReadingDay>

    @Query("SELECT * FROM reading_days WHERE date = :date AND bookId = :bookId LIMIT 1")
    suspend fun getReadingDayForDateAndBook(date: Long, bookId: String): ReadingDay?

    @Query("SELECT SUM(pagesRead) FROM reading_days WHERE bookType = :bookType")
    suspend fun getTotalPagesReadByType(bookType: String): Int?

    @Query("SELECT SUM(timeSpentMinutes) FROM reading_days WHERE bookType = :bookType")
    suspend fun getTotalTimeByType(bookType: String): Int?

    @Query("SELECT SUM(chaptersRead) FROM reading_days WHERE bookType = 'EPUB'")
    suspend fun getTotalChaptersRead(): Int?
}