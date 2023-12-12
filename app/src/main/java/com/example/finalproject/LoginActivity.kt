package com.example.finalproject

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth;
    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 123
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val editTextUsername = findViewById<EditText>(R.id.editTextUsername)
        val editTextPassword = findViewById<EditText>(R.id.editTextPassword)
        val buttonLogin = findViewById<Button>(R.id.loginButton)
        val buttonRegister =
            findViewById<Button>(R.id.registerButton) // Add the Register button in your layout XML
        val buttonGoogle = findViewById<MaterialButton>(R.id.googleButton)
        auth = FirebaseAuth.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Set click listener for the Google Sign-In button
        buttonGoogle.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }


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
                        //intent.putExtra("USERNAME_EXTRA", enteredUsername) // Set extra here
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
                                this,
                                "User with this email already exists. Please use a different email.",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            // User does not exist, proceed with registration
                            auth.createUserWithEmailAndPassword(enteredUsername, enteredPassword)
                                .addOnCompleteListener(this) { task ->
                                    if (task.isSuccessful) {
                                        auth.currentUser?.sendEmailVerification()
                                            ?.addOnCompleteListener { emailVerificationTask ->
                                                if (emailVerificationTask.isSuccessful) {
                                                    // Check if the email is verified
                                                    if (auth.currentUser?.isEmailVerified == true) {
                                                        // Email verified, show success message
                                                        Toast.makeText(
                                                            this, "Registration successful.",
                                                            Toast.LENGTH_SHORT
                                                        ).show()

                                                        // Log the user in after registration
                                                        auth.signInWithEmailAndPassword(
                                                            enteredUsername,
                                                            enteredPassword
                                                        )
                                                            .addOnCompleteListener { signInTask ->
                                                                if (signInTask.isSuccessful) {
                                                                    val intent = Intent(
                                                                        this,
                                                                        MainActivity::class.java
                                                                    )
                                                                    intent.putExtra(
                                                                        "USERNAME_EXTRA",
                                                                        enteredUsername
                                                                    )
                                                                    startActivity(intent)
                                                                    finish()
                                                                } else {
                                                                    Toast.makeText(
                                                                        this,
                                                                        "Sign-in after registration failed.",
                                                                        Toast.LENGTH_SHORT
                                                                    ).show()
                                                                }
                                                            }
                                                    } else {
                                                        // Email not verified yet
                                                        Toast.makeText(
                                                            this, "Please verify your email...",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
                                                } else {
                                                    // Email verification sending failed
                                                    Toast.makeText(
                                                        this, "Failed to send verification email.",
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
                        Toast.makeText(
                            this, "Error checking user existence: ${fetchTask.exception?.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }

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
//            val intent = Intent(this, MainActivity::class.java)
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            // Handle UI update if the user is not signed in
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign-In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Google Sign-In failed
                Toast.makeText(this, "Google Sign-In Failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val username = user?.displayName ?: ""
                    Toast.makeText(this, "Google Login Successful", Toast.LENGTH_SHORT).show()


                    val sharedPreferences = getSharedPreferences("user", MODE_PRIVATE)
                    val editor = sharedPreferences.edit()
                    editor.putString("Username", username)
                    editor.apply()


                    val intent = Intent(this, MainActivity::class.java)
                    intent.putExtra("USERNAME_EXTRA", username)
                    startActivity(intent)
                    finish()
                } else {
                    // Firebase authentication failed
                    Toast.makeText(this, "Firebase Authentication Failed", Toast.LENGTH_SHORT)
                        .show()
                    updateUI(null)
                }
            }
    }
}

