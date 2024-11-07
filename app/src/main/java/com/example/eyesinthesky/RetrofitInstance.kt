package com.example.eyesinthesky

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    private const val BASE_URL = "https://console.cloud.google.com/welcome/new?project=eyesinthesky-437719"

    val api: GoogleApiServices by lazy {
         Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GoogleApiServices::class.java)
    }
}


