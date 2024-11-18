package com.example.rvoz

import android.content.Context
import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.speech.RecognizerIntent
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var tvResult: TextView
    private lateinit var tvLedStatus: TextView
    private lateinit var tvMotorStatus: TextView
    private lateinit var ledIndicator: View
    private lateinit var motorIndicator: View
    private val client = OkHttpClient()
    private val REQUEST_CODE_SPEECH_INPUT = 100
    private var currentLedState = false
    private var currentMotorState = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Verificar si el usuario está autenticado
        val sharedPreferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)
        if (!isLoggedIn) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        setContentView(R.layout.activity_main)

        // Inicializar vistas
        tvResult = findViewById(R.id.tvResult)
        tvLedStatus = findViewById(R.id.tvLedStatus)
        tvMotorStatus = findViewById(R.id.tvMotorStatus)
        ledIndicator = findViewById(R.id.ledIndicator)
        motorIndicator = findViewById(R.id.motorIndicator)
        val btnSpeak: Button = findViewById(R.id.btnSpeak)
        val btnLogout: Button = findViewById(R.id.btnLogout)

        // Configurar indicadores circulares
        setupCircleIndicator(ledIndicator)
        setupCircleIndicator(motorIndicator)

        btnSpeak.setOnClickListener {
            getCurrentState {
                startVoiceRecognition()
            }
        }

        btnLogout.setOnClickListener {
            logoutFromServer()
            val editor = sharedPreferences.edit()
            editor.putBoolean("isLoggedIn", false)
            editor.apply()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        // Obtener estado inicial
        getCurrentState()
    }

    private fun setupCircleIndicator(view: View) {
        val shape = GradientDrawable()
        shape.shape = GradientDrawable.OVAL
        shape.setSize(24.dpToPx(), 24.dpToPx())
        view.background = shape
    }

    private fun Int.dpToPx(): Int {
        return (this * resources.displayMetrics.density).toInt()
    }

    private fun updateStatusIndicators() {
        // Actualizar LED
        tvLedStatus.text = "Estado LED: ${if (currentLedState) "Encendido" else "Apagado"}"
        val ledColor = if (currentLedState)
            ContextCompat.getColor(this, android.R.color.holo_green_light)
        else
            ContextCompat.getColor(this, android.R.color.holo_red_light)
        (ledIndicator.background as GradientDrawable).setColor(ledColor)

        // Actualizar Motor
        tvMotorStatus.text = "Estado Motor: ${if (currentMotorState) "Encendido" else "Apagado"}"
        val motorColor = if (currentMotorState)
            ContextCompat.getColor(this, android.R.color.holo_green_light)
        else
            ContextCompat.getColor(this, android.R.color.holo_red_light)
        (motorIndicator.background as GradientDrawable).setColor(motorColor)
    }

    private fun getCurrentState(onComplete: (() -> Unit)? = null) {
        val url = "http://192.168.40.113:8080/components/getState"

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val request = Request.Builder()
                    .url(url)
                    .get()
                    .build()

                val response = client.newCall(request).execute()
                if (!response.isSuccessful) throw Exception("Error obteniendo el estado actual")

                val responseBody = response.body?.string()
                responseBody?.let {
                    val json = JSONObject(it)
                    currentLedState = json.getBoolean("state_led")
                    currentMotorState = json.getBoolean("state_Motor")

                    runOnUiThread {
                        updateStatusIndicators()
                        onComplete?.invoke()
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(
                        this@MainActivity,
                        "Error al obtener estado actual: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    onComplete?.invoke()
                }
            }
        }
    }

    private fun startVoiceRecognition() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Hable ahora...")

        try {
            startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT)
        } catch (e: Exception) {
            Toast.makeText(this, "Error al iniciar el reconocimiento de voz", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SPEECH_INPUT && resultCode == RESULT_OK && data != null) {
            val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val recognizedText = result?.get(0)?.lowercase(Locale.getDefault()) ?: ""
            tvResult.text = recognizedText

            when {
                recognizedText.contains("encender led") -> {
                    makeHttpRequest(true, currentMotorState)
                }
                recognizedText.contains("apagar led") -> {
                    makeHttpRequest(false, currentMotorState)
                }
                recognizedText.contains("encender motor") -> {
                    makeHttpRequest(currentLedState, true)
                }
                recognizedText.contains("apagar motor") -> {
                    makeHttpRequest(currentLedState, false)
                }
            }
        }
    }

    private fun makeHttpRequest(ledState: Boolean, motorState: Boolean) {
        val url = "http://192.168.40.113:8080/components/addComponent"
        val JSON = "application/json; charset=utf-8".toMediaType()
        val payload = """
            {
                "state_led": $ledState,
                "state_motor": $motorState
            }
        """.trimIndent()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val body = payload.toRequestBody(JSON)
                val request = Request.Builder().url(url).post(body).build()
                val response = client.newCall(request).execute()

                if (!response.isSuccessful) throw Exception("Error en la respuesta del servidor")

                currentLedState = ledState
                currentMotorState = motorState

                runOnUiThread {
                    updateStatusIndicators()
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(
                        this@MainActivity,
                        "Error al actualizar el estado: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun logoutFromServer() {
        val url = "http://192.168.40.113:8080/login/logout"

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val request = Request.Builder()
                    .url(url)
                    .post("".toRequestBody())
                    .build()

                val response = client.newCall(request).execute()

                if (!response.isSuccessful) {
                    throw Exception("Error al cerrar sesión en el servidor")
                }

                runOnUiThread {
                    Log.d("Logout", "Sesión cerrada exitosamente")
                }

            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(
                        this@MainActivity,
                        "Error al cerrar sesión en el servidor: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}