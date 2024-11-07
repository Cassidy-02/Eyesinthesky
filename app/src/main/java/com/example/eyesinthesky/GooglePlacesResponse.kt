package com.example.eyesinthesky

import com.google.gson.annotations.SerializedName
import java.util.UUID
import androidx.room.Entity
import androidx.room.PrimaryKey

data class GooglePlacesResponse(
    @SerializedName("results") val hotspots: List<Hotspot>,
    @SerializedName("status") val status: String,

)

data class Hotspot(
    @SerializedName("place_id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("geometry") val geometry: Geometry
)

data class Geometry(
    @SerializedName("location") val location: Location
)

data class Location(
    @SerializedName("lat") val lat: Double,
    @SerializedName("lng") val lng: Double
)
@Entity
data class BirdObservation(
    val id: String = UUID.randomUUID().toString(),
    val birdName: String,
    val observationTime: Long = System.currentTimeMillis(),
    val latitude: Double,
    val longitude: Double,
    val notes: String
)


