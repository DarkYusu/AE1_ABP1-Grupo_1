package com.aplicaciones_android.ae1abp1

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
        private const val KEY_UNSAVED_NOTE = "unsaved_note"
        private const val START_DELAY_MS = 2000L // Ajusta el delay aquí (milisegundos)
    }

    private val notesViewModel: NotesViewModel by viewModels()
    private lateinit var editNote: EditText
    private lateinit var btnSave: Button

    // Flags y job para coordinar el delay seguro entre onCreate y onStart
    private var isStarted = false
    private var startReady = false
    private var startWorkExecuted = false
    private var startDelayJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "Mensaje del onCreate")
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        Toast.makeText(this, "Prueba onCreate", Toast.LENGTH_SHORT).show()

        // Referencias UI
        editNote = findViewById(R.id.editNote)
        btnSave = findViewById(R.id.btnSave)

        // Restauración desde savedInstanceState (refuerzo)
        if (savedInstanceState != null) {
            val tmp = savedInstanceState.getString(KEY_UNSAVED_NOTE, "") ?: ""
            Log.d(TAG, "onCreate: restaurando nota desde savedInstanceState (${tmp.length} chars)")
            notesViewModel.setNote(tmp)
        } else {
            // si el ViewModel ya tiene una nota guardada en memoria, usarla para rellenar
            notesViewModel.savedNote?.let {
                notesViewModel.setNote(it)
            }
        }

        // Observa cambios en la nota (LiveData)
        notesViewModel.note.observe(this) { text ->
            // Evitar actualizar el EditText si el texto ya coincide (para no mover el cursor)
            if (editNote.text.toString() != text) {
                editNote.setText(text)
            }
        }

        btnSave.setOnClickListener {
            val current = editNote.text.toString()
            notesViewModel.setNote(current)
            notesViewModel.saveNote()
            Log.d(TAG, "Nota guardada por el usuario (length=${current.length})")
            Toast.makeText(this, "Nota guardada", Toast.LENGTH_SHORT).show()
        }

        // Manejo de Insets para edge-to-edge
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Programar el delay sin bloquear el hilo UI.
        // Cuando expire el delay, si la Activity ya está en STARTED ejecutaremos la tarea de inicio;
        // si aún no está STARTED, la tarea se ejecutará desde onStart().
        startDelayJob = lifecycleScope.launch {
            delay(START_DELAY_MS)
            startReady = true
            Log.d(TAG, "startDelayJob: delay finalizado (startReady=true)")
            if (isStarted) {
                executeStartWorkIfNeeded()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "Mensaje del onStart")
        // No bloqueamos aquí. Si el delay ya terminó, ejecutamos la lógica de inicio.
        isStarted = true
        if (startReady) {
            executeStartWorkIfNeeded()
        }
    }

    override fun onRestart() {
        super.onRestart()
        // onRestart se llama cuando la Activity vuelve a iniciarse después de haber sido parada (onStop)
        Log.d(TAG, "Mensaje del onRestart")
        Toast.makeText(this, "onRestart detectado", Toast.LENGTH_SHORT).show()
    }

    private fun executeStartWorkIfNeeded() {
        if (startWorkExecuted) return
        startWorkExecuted = true
        // Lógica que antes estaba en onStart
        Log.d(TAG, "executeStartWorkIfNeeded: ejecutando lógica de inicio tras delay")
        Toast.makeText(this, "Prueba onStart (ejecutado tras delay)", Toast.LENGTH_SHORT).show()
        // Aquí puedes poner cualquier inicialización que quieras posponer
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "Mensaje del onResume")
        // Restaurar texto desde ViewModel si está disponible
        notesViewModel.note.value?.let { current ->
            if (editNote.text.toString() != current) {
                editNote.setText(current)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "Mensaje del onPause")
        // Guardar temporalmente la nota actual en el ViewModel
        notesViewModel.setNote(editNote.text.toString())
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
        // Cancelar el job si la Activity se destruye antes de que termine el delay
        startDelayJob?.cancel()
    }

    // Persistencia temporal adicional entre recreaciones de Activity
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val current = editNote.text.toString()
        outState.putString(KEY_UNSAVED_NOTE, current)
        Log.d(TAG, "onSaveInstanceState: guardando nota temporal (length=${current.length})")
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        val restored = savedInstanceState.getString(KEY_UNSAVED_NOTE, "") ?: ""
        Log.d(TAG, "onRestoreInstanceState: restaurando nota temporal (length=${restored.length})")
        notesViewModel.setNote(restored)
    }
}