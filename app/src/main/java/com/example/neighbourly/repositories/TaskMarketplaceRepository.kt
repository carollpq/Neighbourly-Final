package com.example.neighbourly.repositories

import android.location.Location
import android.net.Uri
import android.util.Log
import com.example.neighbourly.models.Task
import com.example.neighbourly.models.User
import com.example.neighbourly.utils.Constants.TASK_COLLECTION
import com.example.neighbourly.utils.Constants.USER_COLLECTION
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Repository class to handle task and helper-related operations for the task marketplace.
 * Uses Firebase Firestore and Storage for backend functionality.
 */
class TaskMarketplaceRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val storage: FirebaseStorage
) {

    /**
     * Fetch tasks from Firestore with pagination.
     * @param limit The number of tasks to fetch per page.
     * @param page The page number to fetch.
     * @return List of Task objects.
     */
    suspend fun fetchTasks(limit: Long = 20, page: Long = 1): List<Task> {
        return firestore.collection(TASK_COLLECTION)
            .limit(limit * page)
            .get()
            .await()
            .map { document ->
                document.toObject(Task::class.java).apply { id = document.id }
            }
    }

    /**
     * Fetch helpers (users with the helper role) from Firestore with pagination.
     * @param limit The number of helpers to fetch per page.
     * @param page The page number to fetch.
     * @return List of User objects.
     */
    suspend fun fetchHelpers(limit: Long = 20, page: Long = 1): List<User> {
        return firestore.collection(USER_COLLECTION)
            .whereEqualTo("helper", true) // Query only helpers
            .limit(limit * page)
            .get()
            .await()
            .map { document ->
                document.toObject(User::class.java).apply { id = document.id } // Map Firestore document ID to the 'id' field
            }
    }

    /**
     * Search tasks by query and location within a given radius.
     * @param query The search query.
     * @param lat Latitude of the search center.
     * @param lon Longitude of the search center.
     * @param radius The search radius in kilometers.
     * @return List of Task objects matching the search criteria.
     */
    suspend fun searchTasksByLocation(query: String, lat: Double?, lon: Double?, radius: Double = 10.0): List<Task> {
        val regex = Regex(query, RegexOption.IGNORE_CASE)
        val tasks = firestore.collection(TASK_COLLECTION)
            .get()
            .await()
            .documents.mapNotNull { it.toObject(Task::class.java) }

        return tasks.filter { task ->
            val taskLat = task.latitude ?: return@filter false
            val taskLon = task.longitude ?: return@filter false
            val isWithinRadius = calculateDistance(lat ?: 0.0, lon ?: 0.0, taskLat, taskLon) <= radius
            val matchesQuery = regex.containsMatchIn(task.title) || regex.containsMatchIn(task.description)
            isWithinRadius && matchesQuery
        }
    }

    /**
     * Search helpers by query and location within a given radius.
     * @param query The search query.
     * @param lat Latitude of the search center.
     * @param lon Longitude of the search center.
     * @param radius The search radius in kilometers.
     * @return List of User objects matching the search criteria.
     */
    suspend fun searchHelpersByLocation(query: String, lat: Double?, lon: Double?, radius: Double = 10.0): List<User> {
        val regex = Regex(query, RegexOption.IGNORE_CASE)
        val helpers = firestore.collection(USER_COLLECTION)
            .whereEqualTo("helper", true)
            .get()
            .await()
            .documents.mapNotNull { it.toObject(User::class.java) }

        return helpers.filter { helper ->
            val helperLat = helper.latitude ?: return@filter false
            val helperLon = helper.longitude ?: return@filter false
            val isWithinRadius = calculateDistance(lat ?: 0.0, lon ?: 0.0, helperLat, helperLon) <= radius
            val matchesQuery = regex.containsMatchIn(helper.name ?: "") ||
                    regex.containsMatchIn(helper.skills ?: "") ||
                    regex.containsMatchIn(helper.helperDescription ?: "")

            isWithinRadius && matchesQuery
        }
    }

    /**
     * Calculate the distance between two geographical points.
     * @param lat1 Latitude of the first point.
     * @param lon1 Longitude of the first point.
     * @param lat2 Latitude of the second point.
     * @param lon2 Longitude of the second point.
     * @return Distance in kilometers.
     */
    fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
        val startPoint = Location("").apply {
            latitude = lat1
            longitude = lon1
        }
        val endPoint = Location("").apply {
            latitude = lat2
            longitude = lon2
        }
        return startPoint.distanceTo(endPoint) / 1000 // Convert meters to kilometers
    }

    suspend fun searchTasks(query: String): List<Task> {
        // Compile the regex once
        val regex = Regex(query.trim(), RegexOption.IGNORE_CASE)
        // Fetch documents from Firestore
        val tasks = firestore.collection(TASK_COLLECTION)
            .get()
            .await()
            .documents.mapNotNull { it.toObject(Task::class.java) }

        // Filter tasks locally based on the regex
        return tasks.filter { task ->
            val titleMatches = task.title?.let { regex.containsMatchIn(it) } == true
            val descriptionMatches = task.description?.let { regex.containsMatchIn(it) } == true
            titleMatches || descriptionMatches
        }
    }

    suspend fun searchHelpers(query: String): List<User> {
        // Compile the regex once
        val regex = Regex(query.trim(), RegexOption.IGNORE_CASE)

        // Fetch helper documents from Firestore
        val helpers = firestore.collection(USER_COLLECTION)
            .whereEqualTo("helper", true) // Ensure we're only fetching helpers
            .get()
            .await()
            .documents.mapNotNull { it.toObject(User::class.java) }

        // Filter helpers locally based on the regex
        return helpers.filter { helper ->
            val nameMatches = helper.name?.let { regex.containsMatchIn(it) } == true
            val skillsMatches = helper.skills?.let { regex.containsMatchIn(it.toString()) } == true
            val descriptionMatches = helper.helperDescription?.let { regex.containsMatchIn(it) } == true
            nameMatches || skillsMatches || descriptionMatches
        }
    }

    /**
     * Submit a new task to Firestore.
     * @param task The Task object to be submitted.
     * @return The Task object with its generated ID.
     */
    suspend fun submitTask(task: Task): Task {
        val taskRef = firestore.collection(TASK_COLLECTION).document() // Generate a new document reference with an ID
        val taskWithId = task.copy(id = taskRef.id)
        taskRef.set(taskWithId).await()
        return taskWithId
    }

    /**
     * Upload an image to Firebase Storage and return the download URL.
     * @param uri The URI of the image to upload.
     * @return The download URL of the uploaded image.
     */
    suspend fun uploadImageToStorage(uri: Uri): String {
        val imageRef = storage.reference.child("task_images/${System.currentTimeMillis()}")
        val uploadTask = imageRef.putFile(uri).await()
        return uploadTask.storage.downloadUrl.await().toString() // Return the download URL
    }

    fun getCurrentUserId(): String {
        return auth.currentUser?.uid.orEmpty()
    }

    suspend fun fetchTaskById(taskId: String): Task? {
        return firestore.collection(TASK_COLLECTION)
            .document(taskId)
            .get()
            .await()
            .toObject(Task::class.java)
    }

    /**
     * Fetch tasks posted by the currently authenticated user.
     */
    suspend fun fetchTasksByCurrentUser(): List<Task> {
        val currentUserId = auth.currentUser?.uid.orEmpty()
        return firestore.collection(TASK_COLLECTION)
            .whereEqualTo("userId", currentUserId)
            .get()
            .await()
            .map { document ->
                document.toObject(Task::class.java).apply { id = document.id }
            }.also { tasks ->
                Log.d("Repository", "Fetched tasks: ${tasks.size}")
            }
    }

    /**
     * Fetch user details by user ID.
     */
    suspend fun fetchUserById(userId: String): User? {
        return firestore.collection(USER_COLLECTION)
            .document(userId)
            .get()
            .await()
            .toObject(User::class.java)
    }

    /**
     * Fetch details of the authenticated user.
     */
    suspend fun fetchCurrentUser(): User {
        val currentUserId = auth.currentUser?.uid.orEmpty()
        return firestore.collection(USER_COLLECTION)
            .document(currentUserId)
            .get()
            .await()
            .toObject(User::class.java)
            ?: throw Exception("User not found")
    }

    /**
     * Update user details.
     */
    suspend fun updateUserDetails(user: User) {
        val currentUserId = auth.currentUser?.uid.orEmpty()
        firestore.collection(USER_COLLECTION)
            .document(currentUserId)
            .set(user)
            .await()
    }

    /**
     * Update task details.
     */
    suspend fun updateTaskDetails(task: Task, taskId: String) {
        firestore.collection(TASK_COLLECTION)
            .document(taskId)
            .set(task)
            .await()
    }

    suspend fun clearFCMToken(userId: String) {
        firestore.collection(USER_COLLECTION).document(userId)
            .update("fcmToken", null)
            .await()
    }


    suspend fun deleteTask(taskId: String) {
        firestore.collection(TASK_COLLECTION).document(taskId).delete().await()
    }

    /**
     * Sign out the currently logged-in user.
     */
    fun signOut() {
        auth.signOut()
    }

}