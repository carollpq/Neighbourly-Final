package com.example.neighbourly.repositories

import com.example.neighbourly.data.Task
import com.example.neighbourly.data.User
import com.example.neighbourly.utils.Constants.TASK_COLLECTION
import com.example.neighbourly.utils.Constants.USER_COLLECTION
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class TaskMarketplaceRepository @Inject constructor(
    private val firestore: FirebaseFirestore
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
}