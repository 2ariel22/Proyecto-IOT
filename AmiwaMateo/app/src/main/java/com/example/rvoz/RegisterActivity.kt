package com.example.rvoz

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

class RegisterActivity : AppCompatActivity() {
    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val etUsername = findViewById<EditText>(R.id.etRegisterUsername)
        val etPassword = findViewById<EditText>(R.id.etRegisterPassword)
        val etConfirmPassword = findViewById<EditText>(R.id.etRegisterConfirmPassword)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val btnBackToLogin = findViewById<Button>(R.id.btnBackToLogin)

        btnRegister.setOnClickListener {
            val username = etUsername.text.toString()
            val password = etPassword.text.toString()
            val confirmPassword = etConfirmPassword.text.toString()

            if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Hacer la solicitud HTTP para el registro
            register(username, password)
        }

        btnBackToLogin.setOnClickListener {
            finish()
        }
    }

    private fun register(username: String, password: String) {
        val url = "http://192.168.40.113:8080/login"  // Asegúrate de usar la URL correcta de tu API
        val JSON = "application/json; charset=utf-8".toMediaType()
        val payload = "{\"user\":\"$username\",\"password\":\"$password\"}"

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val body = payload.toRequestBody(JSON)
                val request = Request.Builder().url(url).post(body).build()
                val response = client.newCall(request).execute()

                if (!response.isSuccessful) throw Exception("Error en la respuesta del servidor")

                val responseBody = response.body?.string() ?: "Sin respuesta"

                runOnUiThread {
                    if (responseBody.contains("success")) {
                        Toast.makeText(this@RegisterActivity, "Registro exitoso", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this@RegisterActivity, "Error en el registro", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@RegisterActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
