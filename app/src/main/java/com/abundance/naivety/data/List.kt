package com.abundance.naivety.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "lists")
data class List(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val dateCreated: Long = System.currentTimeMillis(),
    val ordinal: Int = 0 // Add this field for ordering
)