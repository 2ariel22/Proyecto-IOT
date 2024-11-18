package com.example.rvoz

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class LoginActivity : AppCompatActivity() {
    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val etUsername = findViewById<EditText>(R.id.etUsername)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnRegister = findViewById<Button>(R.id.btnGoToRegister)

        // Verificar si ya hay una sesión activa
        val sharedPreferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        if (sharedPreferences.getBoolean("isLoggedIn", false)) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        btnLogin.setOnClickListener {
            val username = etUsername.text.toString()
            val password = etPassword.text.toString()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Por favor ingrese las credenciales", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Hacer la solicitud HTTP para login
            login(username, password)
        }

        btnRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun login(username: String, password: String) {
        val url = "http://192.168.40.113:8080/login/authenticate"  // Asegúrate de usar la URL correcta de tu API
        val JSON = "application/json; charset=utf-8".toMediaType()
        val payload = "{\"user\":\"$username\",\"password\":\"$password\"}"

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val body = payload.toRequestBody(JSON)
                val request = Request.Builder().url(url).post(body).build()
                val response = client.newCall(request).execute()

                if (!response.isSuccessful) throw Exception("Error en la respuesta del servidor")

                val responseBody = response.body?.string() ?: "Sin respuesta"
                // Suponiendo que la API devuelve un token o estado de éxito
                val sharedPreferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
                val editor = sharedPreferences.edit()

                if (responseBody.contains("successful")) {
                    // Guarda el estado de sesión como activo
                    editor.putBoolean("isLoggedIn", true)
                    editor.apply()

                    // Inicia la actividad principal
                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    finish()
                } else {
                    runOnUiThread {
                        Toast.makeText(this@LoginActivity, "Credenciales incorrectas", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@LoginActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
