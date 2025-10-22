package com.aplicaciones_android.ae1abp1

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "note")
data class NoteEntity(
    @PrimaryKey
    val id: Int = 0, // usamos id fijo para mantener una única nota
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

