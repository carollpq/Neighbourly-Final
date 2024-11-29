package com.example.neighbourly.repositories

import android.net.Uri
import com.example.neighbourly.models.Task
import com.example.neighbourly.models.User
import com.example.neighbourly.utils.Constants.TASK_COLLECTION
import com.example.neighbourly.utils.Constants.USER_COLLECTION
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class TaskMarketplaceRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val storage: FirebaseStorage
) {
    suspend fun fetchTasks(limit: Long, page: Long): List<Task> {
        return firestore.collection(TASK_COLLECTION)
            .limit(limit * page)
            .get()
            .await()
            .map { document ->
                document.toObject(Task::class.java).apply { id = document.id }
            }
    }

    suspend fun fetchHelpers(limit: Long, page: Long): List<User> {
        return firestore.collection(USER_COLLECTION)
            .whereEqualTo("isHelper", true) // Query only helpers
            .limit(limit * page)
            .get()
            .await()
            .map { document ->
                document.toObject(User::class.java).apply { id = document.id } // Map Firestore document ID to the 'id' field
            }
    }

    suspend fun searchTasks(query: String): List<Task> {
        val regex = Regex(query, RegexOption.IGNORE_CASE)
        return firestore.collection(TASK_COLLECTION)
            .get()
            .await()
            .documents.mapNotNull { it.toObject(Task::class.java) }
            .filter { task ->
                task.title?.let { regex.containsMatchIn(it) } == true ||
                        task.description?.let { regex.containsMatchIn(it) } == true
            }
    }

    suspend fun searchHelpers(query: String): List<User> {
        val regex = Regex(query, RegexOption.IGNORE_CASE)
        return firestore.collection(USER_COLLECTION)
            .whereEqualTo("isHelper", true)
            .get()
            .await()
            .documents.mapNotNull { it.toObject(User::class.java) }
            .filter { helper ->
                helper.name?.let { regex.containsMatchIn(it) } == true ||
                        helper.skills?.let { regex.containsMatchIn(it.toString()) } == true ||
                        helper.helperDescription?.let { regex.containsMatchIn(it) } == true
            }
    }

    suspend fun submitTask(task: Task): String {
        val taskRef = firestore.collection(TASK_COLLECTION).add(task).await()
        return taskRef.id // Return the task ID for reference
    }

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

    suspend fun fetchUserNameById(userId: String): String? {
        return firestore.collection(USER_COLLECTION)
            .document(userId)
            .get()
            .await()
            .getString("name")
    }


}