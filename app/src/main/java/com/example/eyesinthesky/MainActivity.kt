package com.example.eyesinthesky

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase

class MainActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var signInButton: SignInButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        mAuth = FirebaseAuth.getInstance()

        val usernameInput = findViewById<EditText>(R.id.username_input)
        val passwordInput = findViewById<EditText>(R.id.password_input)
        val registerbtn = findViewById<Button>(R.id.register_btn)
        val loginbtn = findViewById<Button>(R.id.login_btn)

        //Configure Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("159766664790-1afhhkjfom3q1h75f6pjjnmc2e6pmc36.apps.googleusercontent.com")
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        signInButton = findViewById(R.id.sign_in_with_google_button)

        //onClickListener for Google sign-in button
        signInButton.setOnClickListener{
            signInWithGoogle()
        }


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
        loginbtn.setOnClickListener {
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
        }
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)
    }

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ){ result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        if (task.isSuccessful){
            val account = task.result
                    firebaseAuthWithGoogle(account.idToken!!)
        } else {
            Toast.makeText(this, "Google sign-in failed", Toast.LENGTH_SHORT).show()
        }
    }

    // Authenticate with Firebase using Google account
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign-in successful
                    Toast.makeText(this, "Google sign-in successful", Toast.LENGTH_SHORT).show()
                    // Navigate to the main activity
                    val intent = Intent(this, Hotspots::class.java)
                    startActivity(intent)
                    finish() // Optional: finish the login activity
                } else {
                    // Sign-in failed
                    Toast.makeText(
                        this,
                        "Google sign-in failed: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }



    private fun isValidEmail(email: String): Boolean {
        return email.contains("@") && email.contains(".")
    }
}
