//region Imports y declaración de clase
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
//endregion

//region MainActivity: Activity principal de la app de notas
class MainActivity : AppCompatActivity() {

    //region Constantes y variables globales
    companion object {
        private const val TAG = "MainActivity"
        private const val KEY_UNSAVED_NOTE = "unsaved_note"
        private const val START_DELAY_MS = 4000L // Delay visible: 4 segundos
    }

    private val notesViewModel: NotesViewModel by viewModels()
    private lateinit var editNote: EditText
    private lateinit var btnSave: Button

    // Flags y job para coordinar el delay seguro entre onCreate y onStart
    private var isStarted = false
    private var startReady = false
    private var startWorkExecuted = false
    private var startDelayJob: Job? = null
    //endregion

    //region onCreate: inicialización de la Activity y la UI
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "Mensaje del onCreate")
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        Toast.makeText(this, "Prueba onCreate", Toast.LENGTH_SHORT).show()

        // Referencias UI
        editNote = findViewById(R.id.editNote)
        btnSave = findViewById(R.id.btnSave)

        // Deshabilitar la UI durante el delay
        editNote.isEnabled = false
        btnSave.isEnabled = false

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

        //region Botón para guardar la nota
        btnSave.setOnClickListener {
            val current = editNote.text.toString()
            notesViewModel.setNote(current)
            notesViewModel.saveNote()
            Log.d(TAG, "Nota guardada por el usuario (length=${current.length})")
            Toast.makeText(this, "Nota guardada", Toast.LENGTH_SHORT).show()
        }
        //endregion

        //region Ajuste de insets para edge-to-edge
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        //endregion

        //region Programar delay seguro entre onCreate y onStart
        startDelayJob = lifecycleScope.launch {
            delay(START_DELAY_MS)
            startReady = true
            Log.d(TAG, "startDelayJob: delay finalizado (startReady=true)")
            if (isStarted) {
                executeStartWorkIfNeeded()
            }
        }
        //endregion
    }
    //endregion

    //region onStart: se ejecuta cuando la Activity pasa a visible
    override fun onStart() {
        super.onStart()
        Log.d(TAG, "Mensaje del onStart")
        isStarted = true
        if (startReady) {
            executeStartWorkIfNeeded()
        }
    }
    //endregion

    //region onRestart: se ejecuta cuando la Activity vuelve a primer plano tras onStop
    override fun onRestart() {
        super.onRestart()
        Log.d(TAG, "Mensaje del onRestart")
        Toast.makeText(this, "onRestart detectado", Toast.LENGTH_SHORT).show()
    }
    //endregion

    //region Lógica pospuesta tras delay (ejecutada una sola vez)
    private fun executeStartWorkIfNeeded() {
        if (startWorkExecuted) return
        startWorkExecuted = true
        Log.d(TAG, "executeStartWorkIfNeeded: ejecutando lógica de inicio tras delay")
        Toast.makeText(this, "¡Listo! Ahora puedes escribir tu nota.", Toast.LENGTH_LONG).show()
        // Habilitar la UI después del delay
        editNote.isEnabled = true
        btnSave.isEnabled = true
        // Aquí puedes poner cualquier inicialización que quieras posponer
    }
    //endregion

    //region onResume: la Activity está lista para interactuar con el usuario
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
    //endregion

    //region onPause: la Activity está parcialmente visible
    override fun onPause() {
        super.onPause()
        Log.d(TAG, "Mensaje del onPause")
        // Guardar temporalmente la nota actual en el ViewModel
        notesViewModel.setNote(editNote.text.toString())
        Toast.makeText(this, "onPause detectado", Toast.LENGTH_LONG).show()
    }
    //endregion

    //region onStop: la Activity ya no está visible
    override fun onStop() {
        super.onStop()
        Log.d(TAG, "Mensaje del onStop")
        Toast.makeText(this, "onStop detectado", Toast.LENGTH_SHORT).show()
        isStarted = false
    }
    //endregion

    //region onDestroy: limpieza final antes de destruir la Activity
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Mensaje onDestroy")
        // Cancelar el job si la Activity se destruye antes de que termine el delay
        startDelayJob?.cancel()
    }
    //endregion

    //region Persistencia temporal entre recreaciones de Activity
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
    //endregion
}
//endregion
