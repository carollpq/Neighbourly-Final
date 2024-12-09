import com.example.neighbourly.repositories.UserRepository
import com.example.neighbourly.utils.OperationResult
import com.example.neighbourly.viewmodel.auth.LoginViewModel
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class LoginViewModelTest {

    private val mockFirebaseAuth = mockk<FirebaseAuth>()
    private val userRepository: UserRepository = mockk()
    private val viewModel = LoginViewModel(userRepository)

    @Test
    fun `login emits Success state when repository succeeds`() = runTest {
        // Arrange
        val mockAuthResult = mockk<AuthResult>() // Mock AuthResult
        val mockTask = mockk<Task<AuthResult>>()

        every { mockTask.isSuccessful } returns true
        every { mockTask.result } returns mockAuthResult
        coEvery { mockTask.await() } returns mockAuthResult
        coEvery { mockFirebaseAuth.signInWithEmailAndPassword(any(), any()) } returns mockTask

        viewModel.login("test@example.com", "password")
        val result = viewModel.loginState.first()

        assertEquals(OperationResult.Success(Unit), result)
    }

    @Test
    fun `login emits Error state when repository throws exception`() = runTest {
        coEvery { userRepository.loginUser(any(), any()).await() } throws Exception("Login failed")

        viewModel.login("test@example.com", "password")
        val result = viewModel.loginState.first()

        assert(result is OperationResult.Error && result.message == "Login failed")
    }
}
