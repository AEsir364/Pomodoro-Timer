// Em SettingsActivity.kt
package com.example.pomodoro

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    private lateinit var editTextFoco: EditText
    private lateinit var editTextPausaCurta: EditText
    private lateinit var editTextPausaLonga: EditText
    private lateinit var saveButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        editTextFoco = findViewById(R.id.editTextFoco)
        editTextPausaCurta = findViewById(R.id.editTextPausaCurta)
        editTextPausaLonga = findViewById(R.id.editTextPausaLonga)
        saveButton = findViewById(R.id.saveButton)

        loadSettings()

        saveButton.setOnClickListener {
            saveSettings()
        }
    }

    private fun loadSettings() {
        val sharedPreferences = getSharedPreferences("PomodoroSettings", Context.MODE_PRIVATE)
        val focoTime = sharedPreferences.getLong("foco_time", 25)
        val pausaCurtaTime = sharedPreferences.getLong("pausa_curta_time", 5)
        val pausaLongaTime = sharedPreferences.getLong("pausa_longa_time", 15)

        editTextFoco.setText(focoTime.toString())
        editTextPausaCurta.setText(pausaCurtaTime.toString())
        editTextPausaLonga.setText(pausaLongaTime.toString())
    }

    private fun saveSettings() {
        val sharedPreferences = getSharedPreferences("PomodoroSettings", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        val focoTime = editTextFoco.text.toString().toLongOrNull() ?: 25
        val pausaCurtaTime = editTextPausaCurta.text.toString().toLongOrNull() ?: 5
        val pausaLongaTime = editTextPausaLonga.text.toString().toLongOrNull() ?: 15

        editor.putLong("foco_time", focoTime)
        editor.putLong("pausa_curta_time", pausaCurtaTime)
        editor.putLong("pausa_longa_time", pausaLongaTime)
        editor.apply()

        Toast.makeText(this, "Configurações salvas!", Toast.LENGTH_SHORT).show()
        finish() // Fecha a tela de configurações e volta para a principal
    }
}