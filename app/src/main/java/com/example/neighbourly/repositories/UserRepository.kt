package com.example.neighbourly.repositories

import com.example.neighbourly.models.User
import com.example.neighbourly.utils.Constants.USER_COLLECTION
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * Repository class to handle user-related operations with Firebase Authentication and Firestore.
 * This class provides methods for user authentication, saving user data, and fetching user details.
 */
class UserRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {

    /**
     * Creates a new user with the given email and password.
     * @param email The email address of the user.
     * @param password The password for the user's account.
     * @return Task for creating a user, which can be observed for success or failure.
     */
    fun createNewUser(email: String, password: String) = firebaseAuth.createUserWithEmailAndPassword(email, password)


    /**
     * Saves user information to Firestore under the specified user ID.
     * @param userUid The unique ID of the user (Firebase Authentication UID).
     * @param user The user object containing the user's information.
     * @throws Exception if the Firestore operation fails.
     */
    suspend fun saveUserInformation(userUid: String, user: User) {
        val userWithJoinedDate = user.copy(joinedSince = com.google.firebase.Timestamp.now())
        firestore.collection(USER_COLLECTION).document(userUid).set(userWithJoinedDate).await()
    }

    fun loginUser(email: String, password: String) = firebaseAuth.signInWithEmailAndPassword(email, password)

    /**
     * Retrieves the current authenticated user's ID (UID).
     * @return The user ID if authenticated, or null if no user is logged in.
     */
    fun getCurrentUserId(): String? {
        return firebaseAuth.currentUser?.uid
    }

    /**
     * Retrieves the current authenticated user.
     * @return The FirebaseUser object representing the current user, or null if not authenticated.
     */
    fun getCurrentUser() = firebaseAuth.currentUser

    /**
     * Checks if the user is logging in for the first time.
     * @param userId The unique ID of the user (Firebase Authentication UID).
     * @return True if this is the first time the user is logging in, or false otherwise.
     * Default is true if the "firstTimeLogin" field is not present in the Firestore document.
     * @throws Exception if the Firestore operation fails.
     */
    suspend fun isFirstTimeLogin(userId: String): Boolean {
        val userDoc = firestore.collection(USER_COLLECTION).document(userId).get().await()
        return userDoc.getBoolean("firstTimeLogin") ?: true // Default to true if the field is missing
    }

    /**
     * Fetches user details from Firestore by user ID.
     * @param userId The unique ID of the user (Firebase Authentication UID).
     * @return The User object if the user exists, or null if the user document does not exist.
     * @throws Exception if the Firestore operation fails.
     */
    suspend fun fetchUser(userId: String): User? {
        val userDoc = firestore.collection(USER_COLLECTION).document(userId).get().await()
        return if (userDoc.exists()) {
            userDoc.toObject(User::class.java) // Convert Firestore document to User object
        } else null
    }
}