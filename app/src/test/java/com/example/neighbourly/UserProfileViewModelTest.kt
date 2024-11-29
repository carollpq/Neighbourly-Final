package com.example.neighbourly

import com.example.neighbourly.models.User
import com.example.neighbourly.utils.Constants.USER_COLLECTION
import com.example.neighbourly.viewmodel.taskMarketplace.UserProfileViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@ExperimentalCoroutinesApi
class UserProfileViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule() // For coroutine testing

    private lateinit var viewModel: UserProfileViewModel
    private val mockAuth = mock<FirebaseAuth> {
        on { currentUser }.thenReturn(mock<FirebaseUser> { on { uid }.thenReturn("12345") })
    }
    private val mockFirestore = mock<FirebaseFirestore>()

    @Before
    fun setup() {
        viewModel = UserProfileViewModel(mockAuth, mockFirestore)
    }

    @Test
    fun `fetchAuthenticatedUserDetails emits success`() = runTest {
        val mockUser = User(id = "12345", name = "John Doe")
        whenever(mockFirestore.collection(USER_COLLECTION).document(any()).get())
            .thenReturn(mockDocumentSnapshot(mockUser))

        viewModel.fetchAuthenticatedUserDetails()

        val result = viewModel.user.value
        assertEquals(mockUser, result)
    }

    @Test
    fun `fetchAuthenticatedUserDetails emits error on failure`() = runTest {
        whenever(mockFirestore.collection(USER_COLLECTION).document(any()).get())
            .thenThrow(RuntimeException("Firestore error"))

        viewModel.fetchAuthenticatedUserDetails()

        val error = viewModel.error.value
        assertEquals("An error occurred while fetching user details.", error)
    }

    @Test
    fun `signOut triggers signOutEvent`() {
        viewModel.signOut()

        assertTrue(viewModel.signOutEvent.value)
        verify(mockAuth).signOut()
    }
}
