package com.aplicaciones_android.ae1abp1

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Query("SELECT * FROM note WHERE id = :id LIMIT 1")
    fun getNoteFlow(id: Int = 0): Flow<NoteEntity?>

    @Query("SELECT * FROM note WHERE id = :id LIMIT 1")
    suspend fun getNoteOnce(id: Int = 0): NoteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: NoteEntity)

    @Query("DELETE FROM note WHERE id = :id")
    suspend fun deleteById(id: Int = 0)
}

