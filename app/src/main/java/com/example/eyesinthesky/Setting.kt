package com.example.eyesinthesky

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class Setting : AppCompatActivity() {
    private lateinit var preferences: SharedPreferences
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_setting)

        preferences = getSharedPreferences("User Preferences", Context.MODE_PRIVATE)

        val metricSwitch = findViewById<Switch>(R.id.Metricswitch)
        val distanceEditText = findViewById<EditText>(R.id.distanceText)
        val save = findViewById<Button>(R.id.savebutton)

        metricSwitch.isChecked = preferences.getBoolean("userMetric",true)
        distanceEditText.setText(preferences.getInt("maxDistance", 10).toString())

        //Listen for switch toggle and save preference
        metricSwitch.setOnCheckedChangeListener{ _,isChecked ->
            with(preferences.edit()){
                putBoolean("useMetric", isChecked)
                apply()
            }
        }

        save.setOnClickListener{
            val maxDistance = distanceEditText.toString().toInt()
            with(preferences.edit()){
                putInt("maxDistance", maxDistance)
                apply()
            }
            Toast.makeText(this,"Settings Saved", Toast.LENGTH_SHORT).show()

        }

    }
}