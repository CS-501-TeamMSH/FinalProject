package com.example.finalproject

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        val editTextUsername = findViewById<EditText>(R.id.editTextUsername)
        val editTextPassword = findViewById<EditText>(R.id.editTextPassword)
        val buttonLogin = findViewById<Button>(R.id.buttonLogin)

        buttonLogin.setOnClickListener {
            val enteredUsername = editTextUsername.text.toString()
            val enteredPassword = editTextPassword.text.toString()

            // setup basic login page with hardcoded strings
            //Need firebase authentication!!

            if (enteredUsername == "user" && enteredPassword == "p") {
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("USERNAME_EXTRA", enteredUsername) // Set extra here
                startActivity(intent) // Start the MainActivity
                finish() // Finish the LoginActivity
            } else {
                // Incorrect credentials, show a message or handle accordingly
                Toast.makeText(this, "Incorrect password. Please try again.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}