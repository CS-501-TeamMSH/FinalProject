package com.smh.finalproject

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 123
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val editTextUsername = findViewById<EditText>(R.id.editTextUsername)
        val editTextPassword = findViewById<EditText>(R.id.editTextPassword)
        val buttonLogin = findViewById<Button>(R.id.loginButton)
        val buttonRegister = findViewById<TextView>(R.id.registerTextView) // Add the Register button in your layout XML


        //  val buttonGoogle = findViewById<MaterialButton>(R.id.googleButton)
        auth = FirebaseAuth.getInstance()
        buttonLogin.setOnClickListener {
            val enteredUsername = editTextUsername.text.toString()
            val enteredPassword = editTextPassword.text.toString()


            // Firebase email and password authentication
            auth.signInWithEmailAndPassword(enteredUsername, enteredPassword)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Toast.makeText(
                            this, "Authentication successful.", Toast.LENGTH_SHORT
                        ).show()
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent) // Start the ImageDetailActivity
                        finish() // Finish the LoginActivity
                    } else {
                        // If sign in fails, display a message to the user.
                        Toast.makeText(
                            this, "Authentication failed. Please try again.", Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
        buttonRegister.setOnClickListener {
//

            val intent = Intent(this, MainDisplayActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        // Update UI based on authentication state
        updateUI(currentUser)
    }

    private fun updateUI(currentUser: FirebaseUser?) {
        if (currentUser != null) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        } else {
           // showSignInOrRegisterDialog()
            Toast.makeText(this, "Please Sign In", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showSignInOrRegisterDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Sign In Required")
        builder.setMessage("Please sign in or register today using a valid email!")
        builder.setPositiveButton("Cancel") { dialog, which ->
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.show()
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
        auth.signInWithCredential(credential).addOnCompleteListener(this) { task ->
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

