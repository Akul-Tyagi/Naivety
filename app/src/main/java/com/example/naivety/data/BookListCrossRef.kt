package com.example.naivety.data

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "book_list_cross_ref",
    primaryKeys = ["bookKey", "listId"],
    foreignKeys = [
        ForeignKey(
            entity = List::class,
            parentColumns = ["id"],
            childColumns = ["listId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class BookListCrossRef(
    val bookKey: String,  // OpenLibrary book key
    val listId: String,
    val dateAdded: Long = System.currentTimeMillis()
)