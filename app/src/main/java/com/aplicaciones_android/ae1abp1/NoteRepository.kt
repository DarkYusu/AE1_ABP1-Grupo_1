package com.aplicaciones_android.ae1abp1

import kotlinx.coroutines.flow.Flow

class NoteRepository(private val dao: NoteDao) {
    fun getNoteFlow(): Flow<NoteEntity?> = dao.getNoteFlow()

    suspend fun getNoteOnce(): NoteEntity? = dao.getNoteOnce()

    suspend fun saveNote(content: String) {
        val entity = NoteEntity(id = 0, content = content, timestamp = System.currentTimeMillis())
        dao.insert(entity)
    }

    suspend fun deleteNote() {
        dao.deleteById()
    }
}

