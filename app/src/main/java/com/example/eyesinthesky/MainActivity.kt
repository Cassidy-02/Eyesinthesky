package com.example.eyesinthesky

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class MainActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        mAuth = FirebaseAuth.getInstance()

        val usernameInput = findViewById<EditText>(R.id.username_input)
        val passwordInput = findViewById<EditText>(R.id.password_input)
        val registerbtn = findViewById<Button>(R.id.register_btn)


        usernameInput.setOnClickListener {
            val email = usernameInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()


            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!isValidEmail(email)) {
                Toast.makeText(this, "Invalid Email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
        }
        passwordInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val password = passwordInput.text.toString()
                if (!password.matches(Regex("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@\$!%*?&])[A-Za-z\\d@\$!%*?&]{8,}\$"))) {
                    if (!password.contains(Regex("[A-Za-z]"))) {
                        Toast.makeText(
                            this,
                            "Please include at least one letter in your password.",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else if (!password.contains(Regex("\\d"))) {
                        Toast.makeText(
                            this,
                            "Please include at least one number in your password.",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else if (!password.contains(Regex("[@\$!%*?&]"))) {
                        Toast.makeText(
                            this,
                            "Please include at least one special character in your password.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                true
            } else {
                false
            }
        }
        registerbtn.setOnClickListener {
            val email = usernameInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!isValidEmail(email)) {
                Toast.makeText(this, "Invalid Email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 8) {
                Toast.makeText(this, "Password must be at least 8 characters", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            // Register the user with Firebase
            mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Registration successful
                        val user = mAuth.currentUser
                        val userMap = mapOf(
                            "email" to email
                        )
                        //Reference Realtime Database
                        val database = FirebaseDatabase.getInstance().reference
                        //Save user data to Realtime Database
                        user?.uid?.let { uid ->
                            database.child("users").child(uid).setValue(userMap)
                                .addOnCompleteListener { dbTask ->
                                    if (dbTask.isSuccessful) {
                                        Toast.makeText(
                                            this,
                                            "Registration successful!",
                                            Toast.LENGTH_SHORT
                                        ).show()

                                        // Navigate to Login Activity
                                        val intent = Intent(this, Login::class.java)
                                        startActivity(intent)
                                        finish()
                                    } else {
                                        Toast.makeText(this, "Database Error: ${dbTask.exception?.message}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                        }
                    } else {
                        //Registration failed ,show an error message
                        Toast.makeText(
                            this,
                            "Registration failed: ${task.exception?.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
    }


    private fun isValidEmail(email: String): Boolean {
        return email.contains("@") && email.contains(".")
    }
}
