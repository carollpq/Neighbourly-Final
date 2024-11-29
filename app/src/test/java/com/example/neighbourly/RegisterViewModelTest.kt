import com.example.neighbourly.data.User
import com.example.neighbourly.repositories.UserRepository
import com.example.neighbourly.utils.OperationResult
import com.example.neighbourly.utils.RegisterValidation
import com.example.neighbourly.viewmodel.auth.RegisterViewModel
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RegisterViewModelTest {

    private lateinit var viewModel: RegisterViewModel
    private val userRepository: UserRepository = mockk()

    @Before
    fun setUp() {
        viewModel = RegisterViewModel(userRepository)
    }

    @Test
    fun `createAccountWithEmailAndPassword emits Success when registration succeeds`() = runTest {
        val user = User(name = "John", email = "john@example.com")
        val password = "password123"
        val confirmPassword = "password123"

        // Mock repository behavior
        coEvery { userRepository.createNewUser(user.email!!, password).await() } returns Unit
        coEvery { userRepository.saveUserInformation(any(), user) } returns Unit

        // Perform the action
        viewModel.createAccountWithEmailAndPassword(user, password, confirmPassword)

        // Assert the result
        val result = viewModel.register.first()
        assertEquals(OperationResult.Success(Unit), result)
    }

    @Test
    fun `createAccountWithEmailAndPassword emits Error when repository throws an exception`() = runTest {
        val user = User(name = "John", email = "john@example.com")
        val password = "password123"
        val confirmPassword = "password123"

        // Mock repository behavior to throw an exception
        coEvery { userRepository.createNewUser(user.email!!, password).await() } throws Exception("Registration failed")

        // Perform the action
        viewModel.createAccountWithEmailAndPassword(user, password, confirmPassword)

        // Assert the result
        val result = viewModel.register.first()
        assert(result is OperationResult.Error && result.message == "Registration failed")
    }

    @Test
    fun `createAccountWithEmailAndPassword emits validation errors for invalid email`() = runTest {
        val user = User(name = "John", email = "invalid-email")
        val password = "password123"
        val confirmPassword = "password123"

        // Perform the action
        viewModel.createAccountWithEmailAndPassword(user, password, confirmPassword)

        // Assert validation errors
        val validationState = viewModel.validation.first()
        assert(validationState.email is RegisterValidation.Failed)
        assertEquals("Invalid email address", (validationState.email as RegisterValidation.Failed).message)
    }

    @Test
    fun `createAccountWithEmailAndPassword emits validation errors for mismatched passwords`() = runTest {
        val user = User(name = "John", email = "john@example.com")
        val password = "password123"
        val confirmPassword = "password456"

        // Perform the action
        viewModel.createAccountWithEmailAndPassword(user, password, confirmPassword)

        // Assert validation errors
        val validationState = viewModel.validation.first()
        assert(validationState.password is RegisterValidation.Failed)
        assertEquals("Passwords do not match or are too short", (validationState.password as RegisterValidation.Failed).message)
    }
}
