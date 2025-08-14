package com.example.pomodoro

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var timerText: TextView
    private lateinit var startButton: Button
    private lateinit var resetButton: Button
    private lateinit var timeSpinner: Spinner

    private var countDownTimer: CountDownTimer? = null
    private var timeLeftInMillis: Long = 0
    private var selectedTimeInMillis: Long = 0
    private var isTimerRunning: Boolean = false

    private var focusTimeInMinutes: Long = 25
    private var shortBreakTimeInMinutes: Long = 5
    private var longBreakTimeInMinutes: Long = 15

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        timerText = findViewById(R.id.timerText)
        startButton = findViewById(R.id.startButton)
        resetButton = findViewById(R.id.resetButton)
        timeSpinner = findViewById(R.id.timeSpinner)

        // A chamada 'setupSpinner()' foi REMOVIDA daqui.

        startButton.setOnClickListener {
            if (isTimerRunning) {
                pauseTimer()
            } else {
                startTimer()
            }
        }

        resetButton.setOnClickListener {
            resetTimer()
        }
    }

    override fun onResume() {
        super.onResume()
        loadTimerSettingsAndUpdateSpinner()
    }

    private fun loadTimerSettingsAndUpdateSpinner() {
        val sharedPreferences = getSharedPreferences("PomodoroSettings", Context.MODE_PRIVATE)
        focusTimeInMinutes = sharedPreferences.getLong("foco_time", 25)
        shortBreakTimeInMinutes = sharedPreferences.getLong("pausa_curta_time", 5)
        longBreakTimeInMinutes = sharedPreferences.getLong("pausa_longa_time", 15)

        val options = mutableListOf<String>()
        options.add("Foco ($focusTimeInMinutes Minutos)")
        options.add("Pausa Curta ($shortBreakTimeInMinutes Minutos)")
        options.add("Pausa Longa ($longBreakTimeInMinutes Minutos)")

        setupSpinner(options) // A chamada correta agora é feita aqui.

        val currentPosition = if (timeSpinner.selectedItemPosition >= 0) timeSpinner.selectedItemPosition else 0
        timeSpinner.setSelection(currentPosition, false) // Evita o gatilho inicial desnecessário
        handleSpinnerSelection(currentPosition)
    }

    private fun handleSpinnerSelection(position: Int) {
        val newTime = when (position) {
            0 -> focusTimeInMinutes * 60 * 1000
            1 -> shortBreakTimeInMinutes * 60 * 1000
            2 -> longBreakTimeInMinutes * 60 * 1000
            else -> focusTimeInMinutes * 60 * 1000
        }
        selectTime(newTime)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupSpinner(options: List<String>) {
        val adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_item, options
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        timeSpinner.adapter = adapter
        timeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                handleSpinnerSelection(position)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun selectTime(timeInMillis: Long) {
        pauseTimer()
        selectedTimeInMillis = timeInMillis
        timeLeftInMillis = selectedTimeInMillis
        updateCountDownText()
    }

    private fun startTimer() {
        countDownTimer = object : CountDownTimer(timeLeftInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis = millisUntilFinished
                updateCountDownText()
            }
            override fun onFinish() {
                isTimerRunning = false
                startButton.text = "Iniciar"

                try {
                    val notificationSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                    val mediaPlayer = MediaPlayer.create(applicationContext, notificationSoundUri)
                    mediaPlayer?.start()
                    mediaPlayer?.setOnCompletionListener { it.release() }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(500)
                }
            }
        }.start()
        isTimerRunning = true
        startButton.text = "Pausar"
    }

    private fun pauseTimer() {
        countDownTimer?.cancel()
        isTimerRunning = false
        startButton.text = "Iniciar"
    }

    private fun resetTimer() {
        pauseTimer()
        timeLeftInMillis = selectedTimeInMillis
        updateCountDownText()
    }

    private fun updateCountDownText() {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(timeLeftInMillis)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(timeLeftInMillis) - TimeUnit.MINUTES.toSeconds(minutes)
        val timeFormatted = String.format("%02d:%02d", minutes, seconds)
        timerText.text = timeFormatted
    }
}