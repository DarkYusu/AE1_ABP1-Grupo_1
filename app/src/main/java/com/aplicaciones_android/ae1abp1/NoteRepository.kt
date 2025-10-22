//region Imports y declaración de clase
package com.aplicaciones_android.ae1abp1

import kotlinx.coroutines.flow.Flow
//endregion

//region NoteRepository: Repositorio para acceso y manipulación de notas en Room
class NoteRepository(private val dao: NoteDao) {
    //region Obtener la nota como Flow (observación reactiva)
    fun getNoteFlow(): Flow<NoteEntity?> = dao.getNoteFlow()
    //endregion

    //region Obtener la nota una sola vez (suspend)
    suspend fun getNoteOnce(): NoteEntity? = dao.getNoteOnce()
    //endregion

    //region Guardar o actualizar la nota en la base de datos
    suspend fun saveNote(content: String) {
        val entity = NoteEntity(id = 0, content = content, timestamp = System.currentTimeMillis())
        dao.insert(entity)
    }
    //endregion

    //region Eliminar la nota de la base de datos
    suspend fun deleteNote() {
        dao.deleteById()
    }
    //endregion
}
//endregion
