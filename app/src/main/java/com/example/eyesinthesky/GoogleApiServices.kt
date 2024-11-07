package com.example.eyesinthesky

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface GoogleApiServices {
    @GET("maps/api/place/nearbysearch/json")
    suspend fun getNearbyHotspots(
        @Query("location") location: String,  // location in "latitude,longitude" format
        @Query("radius") radius: Int,         // Radius in meters
        @Query("key") apiKey: String          // Your Google API key
    ):Response<GooglePlacesResponse>    // Use retrofit2.Response if you need HTTP response metadata
}
