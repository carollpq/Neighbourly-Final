package com.example.neighbourly

import com.example.neighbourly.di.FirebaseModule
import com.example.neighbourly.models.Task
import com.example.neighbourly.models.User
import com.example.neighbourly.repositories.TaskMarketplaceRepository
import com.example.neighbourly.utils.OperationResult
import com.example.neighbourly.viewmodel.taskMarketplace.ProfileViewModel
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class ProfileViewModelTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @MockK
    private lateinit var repository: TaskMarketplaceRepository

    private lateinit var viewModel: ProfileViewModel

//    @Before
//    fun setup() {
//        MockKAnnotations.init(this, relaxed = true)
//        viewModel = ProfileViewModel(repository)
//    }

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun `fetchAuthenticatedUserDetails updates state with user data`() = runTest {
        val user = User(id = "user1", name = "John Doe", email = "johndoe@example.com")
        coEvery { repository.fetchCurrentUser() } returns user

        viewModel.fetchAuthenticatedUserDetails()

        val state = viewModel.userDetails.first()
        assertTrue(state is OperationResult.Success && state.data == user)
    }

    @Test
    fun `fetchUserPostedTasks updates state with task data`() = runTest {
        val tasks = listOf(Task(id = "task1", title = "Test Task"))
        coEvery { repository.fetchTasksByCurrentUser() } returns tasks

        viewModel.fetchUserPostedTasks()

        val state = viewModel.userTasks.first()
        assertTrue(state is OperationResult.Success && state.data == tasks)
    }

    @Test
    fun `toggleHelperProfile updates user details`() = runTest {
        val user = User(id = "user1", isHelper = false)
        coEvery { repository.fetchCurrentUser() } returns user
        coJustRun { repository.updateUserDetails(any()) }

        viewModel.toggleHelperProfile(isHelper = true, skills = listOf("Skill1"), helperDescription = "Helper Desc")

        coVerify { repository.updateUserDetails(any()) }
        assertTrue(viewModel.userDetails.first() is OperationResult.Success)
    }

    @Test
    fun `signOut triggers signOutEvent`() = runTest {
        // Act
        viewModel.signOut()

        // Assert
        assertTrue(viewModel.signOutEvent.first())
    }

}
