package com.example.eyesinthesky

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [BirdObservation::class], version = 1)
abstract class BirdObservationDatabase: RoomDatabase(){

    abstract fun birdObservations(): BirdObservations

    companion object {
        @Volatile
        private var INSTANCE: BirdObservationDatabase? = null

        fun getDatabase(context: Context): BirdObservationDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BirdObservationDatabase::class.java,
                    "bird_observation_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}