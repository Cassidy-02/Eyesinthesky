package com.example.eyesinthesky


import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.FirebaseDatabase




class Setting : AppCompatActivity() {
    private lateinit var preferences: SharedPreferences
    private val database = FirebaseDatabase.getInstance().reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_setting)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        // Set the selected item in the bottom navigation bar
        bottomNavigationView.selectedItemId = R.id.navigation_home

        // Handle bottom navigation item clicks
        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.navigation_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    true
                } // Already on the Home screen
                R.id.navigation_settings -> {
                    startActivity(Intent(this, Setting::class.java))
                    true
                }

                R.id.navigation_notes -> {
                    startActivity(Intent(this, ViewObservations::class.java))
                    true
                }

                R.id.navigation_map -> {
                    startActivity(Intent(this, Hotspots::class.java))
                    true
                }

                else -> false
            }
        }

            preferences = getSharedPreferences("User Preferences", Context.MODE_PRIVATE)

            val metricSwitch = findViewById<Switch>(R.id.Metricswitch)
            val distanceEditText = findViewById<EditText>(R.id.distanceText)
            val save = findViewById<Button>(R.id.savebutton)

            metricSwitch.isChecked = preferences.getBoolean("userMetric", true)
            distanceEditText.setText(preferences.getInt("maxDistance", 10).toString())

            //Listen for switch toggle and save preference
            metricSwitch.setOnCheckedChangeListener { _, isChecked ->
                with(preferences.edit()) {
                    putBoolean("useMetric", isChecked)
                    apply()
                }
            }

            save.setOnClickListener {
                val maxDistance = distanceEditText.text.toString().toIntOrNull() ?: 10
                val useMetric = metricSwitch.isChecked

                //Save to Shared Preferences
                with(preferences.edit()) {
                    putInt("maxDistance", maxDistance)
                    putBoolean("useMetric", useMetric)
                    apply()
                }
                //Save to Firebase
                val userSettings = mapOf(
                    "maxDistance" to maxDistance,
                    "useMetric" to useMetric
                )
                database.child("userSettings")
                    .setValue(userSettings)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Settings Saved", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Error Saving Settings", Toast.LENGTH_SHORT).show()
                    }
                val intent = Intent(this, Hotspots::class.java)
                startActivity(intent)

            }


    }
}