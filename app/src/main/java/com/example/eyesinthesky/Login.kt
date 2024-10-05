package com.example.eyesinthesky

import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class Login : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        val usernameInput = findViewById<EditText>(R.id.username_input)
        val passwordInput = findViewById<EditText>(R.id.password_input)
        val login = findViewById<Button>(R.id.login_btn)


        usernameInput.setOnClickListener{
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
                        Toast.makeText(this, "Please include at least one letter in your password.", Toast.LENGTH_SHORT).show()
                    } else if (!password.contains(Regex("\\d"))) {
                        Toast.makeText(this, "Please include at least one number in your password.", Toast.LENGTH_SHORT).show()
                    } else if (!password.contains(Regex("[@\$!%*?&]"))) {
                        Toast.makeText(this, "Please include at least one special character in your password.", Toast.LENGTH_SHORT).show()
                    }
                }
                true
            } else {
                false
            }
        }

        login.setOnClickListener{
            val intent = Intent(this,Setting::class.java)
            startActivity(intent)
        }

    }

    private fun isValidEmail(email: String): Boolean {
        return email.contains("@") && email.contains(".")
    }
}