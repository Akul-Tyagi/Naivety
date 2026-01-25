package com.abundance.naivety.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Tracks reading activity for both PDF and EPUB formats.
 *
 * For PDFs: pagesRead represents actual document pages
 * For EPUBs: pagesRead represents estimated pages based on reading time
 *           chaptersRead tracks actual chapters progressed
 *
 * The time-based estimation for EPUBs uses average reading speed (250 words/min)
 * with ~250 words per page, resulting in roughly 1 page per minute of reading.
 */
@Entity(tableName = "reading_days")
data class ReadingDay(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: Long, // timestamp in milliseconds (start of day)
    val bookId: String,
    val pagesRead: Int, // PDF: actual pages, EPUB: estimated from time
    val timeSpentMinutes: Int,
    val bookType: String = "PDF", // "PDF" or "EPUB"
    val chaptersRead: Int = 0 // EPUB specific: actual chapters progressed
)