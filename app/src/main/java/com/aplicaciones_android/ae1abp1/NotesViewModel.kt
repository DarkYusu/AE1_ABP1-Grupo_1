package com.aplicaciones_android.ae1abp1

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class NotesViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        private const val TAG = "NotesViewModel"
    }

    // Repositorio basado en Room
    private val database = AppDatabase.getInstance(application)
    private val repository = NoteRepository(database.noteDao())

    // LiveData que contiene la nota actual en edición
    private val _note = MutableLiveData<String>("")
    val note: LiveData<String> = _note

    // Nota guardada explícitamente (persistencia en DB)
    private var _savedNote: String? = null
    val savedNote: String?
        get() = _savedNote

    init {
        // Recoger cambios desde la base de datos y actualizar LiveData
        viewModelScope.launch {
            try {
                repository.getNoteFlow().collect { entity ->
                    val content = entity?.content ?: ""
                    _savedNote = if (content.isBlank()) null else content
                    // postValue por si viene desde background
                    _note.postValue(content)
                    Log.d(TAG, "Flow recibido desde DB (length=${content.length})")
                }
            } catch (t: Throwable) {
                Log.w(TAG, "Error recolectando notas desde DB", t)
            }
        }
    }

    fun setNote(text: String) {
        _note.value = text
    }

    fun saveNote() {
        val current = _note.value ?: ""
        viewModelScope.launch {
            repository.saveNote(current)
            _savedNote = if (current.isBlank()) null else current
            Log.d(TAG, "Nota guardada en DB (length=${current.length})")
        }
    }

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "onCleared() — ViewModel será destruido")
    }
}
