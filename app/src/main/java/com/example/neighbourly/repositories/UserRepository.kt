package com.example.neighbourly.repositories

import com.example.neighbourly.models.User
import com.example.neighbourly.utils.Constants.USER_COLLECTION
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    fun createNewUser(email: String, password: String) = firebaseAuth.createUserWithEmailAndPassword(email, password)

    fun saveUserInformation(userUid: String, user: User) = firestore.collection(USER_COLLECTION).document(userUid).set(user)

    fun loginUser(email: String, password: String) = firebaseAuth.signInWithEmailAndPassword(email, password)

    // Fetch the current user's ID
    fun getCurrentUserId(): String? {
        return firebaseAuth.currentUser?.uid
    }

    // Check if the user is logging in for the first time
    suspend fun isFirstTimeLogin(userId: String): Boolean {
        val userDoc = firestore.collection(USER_COLLECTION).document(userId).get().await()
        return userDoc.getBoolean("firstTimeLogin") ?: true // Default to true if the field is missing
    }

    // Fetch user details by user ID
    suspend fun fetchUser(userId: String): User? {
        val userDoc = firestore.collection(USER_COLLECTION).document(userId).get().await()
        return if (userDoc.exists()) {
            userDoc.toObject(User::class.java)
        } else null
    }
}