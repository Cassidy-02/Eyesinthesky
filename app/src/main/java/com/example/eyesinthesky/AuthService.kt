package com.example.eyesinthesky


import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class AuthService {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().reference

    fun registerUser(email: String, password: String, onComplete: (Boolean, String?) -> Unit) {
        val addOnCompleteListener = auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid ?: ""
                    // Save additional user data in Realtime Database
                    val userMap = mapOf("email" to email)
                    database.child("users").child(userId).setValue(userMap)
                        .addOnCompleteListener { dbTask ->
                            if (dbTask.isSuccessful) {
                                onComplete(true, null)
                            } else {
                                onComplete(false, dbTask.exception?.message)
                            }
                        }
                } else {
                    onComplete(false, task.exception?.message)
                }
            }
    }

    fun loginUser(email: String, password: String, onComplete: (Boolean, String?) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onComplete(true, null)
                } else {
                    onComplete(false, task.exception?.message)
                }
            }
    }
    //Interacts with Firebase Realtime Database to save user profile data
    //Writing Data
    fun saveUserProfile(userId: String, userProfile: Map<String, Any>, onComplete: (Boolean) -> Unit) {
        database.child("users").child(userId).updateChildren(userProfile)
            .addOnCompleteListener { task ->
                onComplete(task.isSuccessful)
            }
    }

    //Reading Data from Realtime Database
    fun getUserProfile(userId: String, onComplete: (Map<String, Any>?, String?) -> Unit) {
        database.child("users").child(userId).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                onComplete(snapshot.value as Map<String, Any>?, null)
            } else {
                onComplete(null, "User not found")
            }
        }.addOnFailureListener { exception ->
            onComplete(null, exception.message)
        }
    }
}
