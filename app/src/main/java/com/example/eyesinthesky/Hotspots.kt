@file:Suppress("UNREACHABLE_CODE")

package com.example.eyesinthesky

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class Hotspots : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mapView: MapView
    private lateinit var googleMap: GoogleMap
    private lateinit var birdNameEditText: EditText
    private lateinit var notesEditText: EditText
    private lateinit var saveObservationButton: Button
    private lateinit var viewObservationsButton: Button
    private val apiKey = "AIzaSyDpDcJ4mf7EdS_nJOzYJCgKDMHJFSS1O5Y"

    private var selectedDistance: Int = 10  // Default distance: 10 km

    private lateinit var currentLocation: LatLng // Set this to user's current location


    private lateinit var fusedLocationClient: FusedLocationProviderClient

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hotspots)

        mapView = findViewById(R.id.mapView)
        birdNameEditText = findViewById(R.id.birdNameEditText)
        notesEditText = findViewById(R.id.notesEditText)
        saveObservationButton = findViewById(R.id.saveObservationButton)
        viewObservationsButton = findViewById(R.id.viewObservationsButton)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync {map ->
            googleMap = map
            setupMapListeners()
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)


        val distanceSeekBar = findViewById<SeekBar>(R.id.distanceSeekBar)
        val distanceTextView = findViewById<TextView>(R.id.distanceTextView)

// Set up listener for distance changes on the SeekBar
        distanceSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                selectedDistance = progress
                distanceTextView.text = "Distance: $selectedDistance km"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // Not used
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }
        })
        var currentLocation: Location? = null

        saveObservationButton.setOnClickListener {
            val birdName = birdNameEditText.text.toString()
            val notes = notesEditText.text.toString()
            if (currentLocation == null) {
                saveObservation(birdName, notes, currentLocation!!)
            } else{
                //The case where currentLocation is null
                Toast.makeText(this,"Location not available", Toast.LENGTH_SHORT).show()
            }
        }

        viewObservationsButton.setOnClickListener {
            fetchAllObservations()
        }
    }

    private fun setupMapListeners() {
        googleMap.setOnMarkerClickListener { marker ->
            // Handle marker click event here
            val hotspotLocation = marker.position
            getDirectionsToHotspot(hotspotLocation) // Get directions to the selected hotspot
            true  // Return true to indicate that the event was handled
        }
    }

    private fun fetchAllObservations() {
        CoroutineScope(Dispatchers.IO).launch {
            val observations = BirdObservationDatabase.getDatabase(applicationContext).birdObservations().getAllObservations()
            withContext(Dispatchers.Main) {
                displayObservationsOnMap(observations)
            }
        }
    }

    private fun displayObservationsOnMap(observations: List<BirdObservation>) {
        googleMap.clear() // Clear existing markers
        for (observation in observations) {
            val position = LatLng(observation.latitude, observation.longitude)
            googleMap.addMarker(
                MarkerOptions().position(position).title(observation.birdName)
            )
        }
    }

    private fun saveObservation(birdName: String, notes: String, currentLocation: LatLng) {
        val observation = BirdObservation(
            birdName = birdName,
            latitude = currentLocation.latitude,
            longitude = currentLocation.longitude,
            notes = notes
        )

        CoroutineScope(Dispatchers.IO).launch {
            // Insert observation into Room Database
            try {
                BirdObservationDatabase.getDatabase(applicationContext).birdObservations().insertObservation(observation)

                withContext(Dispatchers.Main) {
                    Toast.makeText(applicationContext, "Bird observation saved!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                // Handle database insertion error
                Log.e("Database Error", "Error inserting observation: ${e.message}")
            }
        }

    }


    private fun getCurrentLocation(context: Context) {
        if ((ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED) && (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED)
        ) {

            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                val currentLocation = LatLng(location.latitude, location.longitude)
                fetchAndDisplayHotspots(currentLocation)  // Fetch hotspots around the user's real location
            } else {
                // Handle if the location is null (e.g., location not available)
                Toast.makeText(
                    context,
                    "Unable to get your current location",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun getDirectionsToHotspot(hotspotLocation: LatLng) {
        // Get the user's current location (you can use the FusedLocationProviderClient if needed)
        val userLocation = googleMap.myLocation
        if (userLocation != null) {
            val userLatLng = LatLng(userLocation.latitude, userLocation.longitude)

            // Build a URI to open Google Maps with driving directions
            val gmmIntentUri = Uri.parse(
                "https://www.google.com/maps/dir/?api=1&origin=${userLatLng.latitude},${userLatLng.longitude}&destination=${hotspotLocation.latitude},${hotspotLocation.longitude}&travelmode=driving"
            )

            // Create an intent to open Google Maps
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")

            // Check if Google Maps is installed and start the activity
            if (mapIntent.resolveActivity(packageManager) != null) {
                startActivity(mapIntent)
            } else {
                Toast.makeText(
                    this@Hotspots,
                    "Google Maps is not installed",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            Toast.makeText(
                this@Hotspots,
                "Unable to get your current location",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        enableLocation()

        val currentLocation = LatLng(-34.0, 151.0)
        fetchAndDisplayHotspots(currentLocation)
    }

    // Fetch hotspots using the eBird API and display markers on the map
    private fun fetchAndDisplayHotspots(currentLocation: LatLng) {
        // Use the ViewModel scope to handle coroutines in a lifecycle-aware manner
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitInstance.api.getNearbyHotspots(
                    location = "${currentLocation.latitude},${currentLocation.longitude}",
                    radius = selectedDistance * 1000,  // Convert km to meters
                    apiKey = "AIzaSyDpDcJ4mf7EdS_nJOzYJCgKDMHJFSS1O5Y"
                )

                // Log the full response to check its structure
                Log.d("API_RESPONSE", "Response: $response")



                if (response.isSuccessful) {
                    val responseBody = response.body()

                    if (responseBody != null && responseBody.status == "ok") {
                        withContext(Dispatchers.Main) {
                            googleMap.clear()  // Clear existing markers
                            for (hotspot in responseBody.hotspots) {
                                val position = LatLng(
                                    hotspot.geometry.location.lat,
                                    hotspot.geometry.location.lng
                                )
                                googleMap.addMarker(
                                    MarkerOptions().position(position).title(hotspot.name)
                                )
                            }
                        }
                    } else {
                        Log.e("API_ERROR", "Error fetching hotspots: ${responseBody?.status}")
                    }
                } else {
                    Log.e("API_ERROR", "Error fetching hotspots: ${response.errorBody()}")
                }
            }catch (e: Exception) {
                        Log.e("API_ERROR", "Exception: ${e.message}")
            }
        }
    }


    // Check for location permission
    private fun enableLocation() {

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            return
        }
        googleMap.isMyLocationEnabled = true
    }


    // Handle permission request result
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    enableLocation()
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }
}

