package com.example.eyesinthesky

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ViewObservations : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var database: DatabaseReference
    private lateinit var observationsList: MutableList<BirdsObservation>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_view_observations)

        // Initialize Firebase Realtime Database
        database = FirebaseDatabase.getInstance().reference

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        observationsList = mutableListOf()

        // Fetch data from Firebase
        fetchObservations()

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
    }

    private fun fetchObservations() {
        database.child("observations").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                observationsList.clear()

                // Loop through the snapshot and retrieve observations
                for (observationSnapshot in snapshot.children) {
                    val observation = observationSnapshot.getValue(BirdsObservation::class.java)
                    if (observation != null) {
                        observationsList.add(observation)
                    }
                }

                // Set up the RecyclerView adapter with the fetched data
                recyclerView.adapter = ObservationAdapter(observationsList.toList())
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ViewObservations, "Failed to load observations", Toast.LENGTH_SHORT).show()
            }
        })
    }
}

