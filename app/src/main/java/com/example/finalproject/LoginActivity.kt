package com.example.finalproject

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.android.gms.common.SignInButton
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val editTextUsername = findViewById<EditText>(R.id.editTextUsername)
        val editTextPassword = findViewById<EditText>(R.id.editTextPassword)
        val buttonLogin = findViewById<Button>(R.id.buttonLogin)
        val buttonRegister = findViewById<Button>(R.id.buttonRegister) // Add the Register button in your layout XML
        val buttonGoogle = findViewById<SignInButton>(R.id.googleSignInButton)
        auth = FirebaseAuth.getInstance()

        // firebaseAuth = FirebaseAuth.getInstance()

        buttonLogin.setOnClickListener {
            val enteredUsername = editTextUsername.text.toString()
            val enteredPassword = editTextPassword.text.toString()


            // Firebase email and password authentication
            auth.signInWithEmailAndPassword(enteredUsername, enteredPassword)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        val user = auth.currentUser
                        Toast.makeText(
                            this, "Authentication successful.",
                            Toast.LENGTH_SHORT
                        ).show()
                        val intent = Intent(this, MainActivity::class.java)
                        intent.putExtra("USERNAME_EXTRA", enteredUsername) // Set extra here
                        startActivity(intent) // Start the MainActivity
                        finish() // Finish the LoginActivity
                    } else {
                        // If sign in fails, display a message to the user.
                        Toast.makeText(
                            this, "Authentication failed. Please try again.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
        buttonRegister.setOnClickListener {
            val enteredUsername = editTextUsername.text.toString()
            val enteredPassword = editTextPassword.text.toString()

            // Check if the user already exists with the entered email
            auth.fetchSignInMethodsForEmail(enteredUsername)
                .addOnCompleteListener { fetchTask ->
                    if (fetchTask.isSuccessful) {
                        val signInMethods = fetchTask.result?.signInMethods ?: emptyList<String>()

                        if (signInMethods.isNotEmpty()) {
                            // User already exists with the entered email, show error message
                            Toast.makeText(
                                this, "User with this email already exists. Please use a different email.",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            // User does not exist, proceed with registration
                            auth.createUserWithEmailAndPassword(enteredUsername, enteredPassword)
                                .addOnCompleteListener(this) { task ->
                                    if (task.isSuccessful) {
                                        Toast.makeText(
                                            this, "Registration successful.",
                                            Toast.LENGTH_SHORT
                                        ).show()

                                        // Automatically signs in the newly registered user
                                        auth.signInWithEmailAndPassword(enteredUsername, enteredPassword)
                                            .addOnCompleteListener { signInTask ->
                                                if (signInTask.isSuccessful) {
                                                    val intent = Intent(this, MainActivity::class.java)
                                                    intent.putExtra("USERNAME_EXTRA", enteredUsername)
                                                    startActivity(intent)
                                                    finish()
                                                } else {
                                                    Toast.makeText(
                                                        this, "Sign-in after registration failed.",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            }
                                    } else {
                                        // Registration failed, display the error message from Firebase
                                        Toast.makeText(
                                            this, "Registration failed: ${task.exception?.message}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                        }
                    } else {
                        // Error while fetching user data, display an error message
                        Toast.makeText(
                            this, "Error checking user existence: ${fetchTask.exception?.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }



        buttonGoogle.setOnClickListener {
            Toast.makeText(this, "TODO", Toast.LENGTH_SHORT).show()
        }

        //TODO: Implement google sign in(getting error)


    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        // Update UI based on authentication state
        updateUI(currentUser)
    }

    private fun updateUI(currentUser: FirebaseUser?) {
        // You can update the UI based on the currentUser object
        // For example, enable/disable certain buttons, show user information, etc.
        if (currentUser != null) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            // Handle UI update if the user is not signed in
        }
    }
}
