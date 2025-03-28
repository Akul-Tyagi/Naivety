package com.example.naivety.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reading_days")
data class ReadingDay(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: Long, // timestamp in milliseconds
    val bookId: String,
    val pagesRead: Int,
    val timeSpentMinutes: Int
)