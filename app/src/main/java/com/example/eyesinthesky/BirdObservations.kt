package com.example.eyesinthesky

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface BirdObservations {
    @Insert
    suspend fun insertObservation(observation: BirdObservation): Long

    @Query("SELECT * FROM BirdObservation")
    suspend fun getAllObservations(): List<BirdObservation>
}
