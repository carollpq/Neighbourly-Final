package com.example.neighbourly.fragments

import android.content.Context
import android.location.Location
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.FragmentScenario
import androidx.test.core.app.ApplicationProvider
import com.example.neighbourly.fragments.taskMarketplace.HomeFragment
import com.example.neighbourly.launchFragmentInHiltContainer
import com.example.neighbourly.utils.PermissionUtils
import com.example.neighbourly.viewmodel.taskMarketplace.HomeViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.tasks.Tasks
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import junit.framework.TestCase.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

@HiltAndroidTest
class HomeFragmentTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Mock
    lateinit var homeViewModel: HomeViewModel

    @Mock
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private lateinit var scenario: FragmentScenario<HomeFragment>

    @Before
    fun setUp() {
        hiltRule.inject()
        scenario = launchFragmentInHiltContainer<HomeFragment>()
    }

    @Test
    fun testFetchNearbyTasksAndHelpers_Success() {
        // Mock location data
        val mockLocation = Location("").apply {
            latitude = 40.7128
            longitude = -74.0060
        }
        `when`(fusedLocationProviderClient.lastLocation).thenReturn(Tasks.forResult(mockLocation))

        scenario.onFragment { fragment ->
            fragment.fetchNearbyTasksAndHelpers()

            verify(homeViewModel).fetchNearbyTasks(40.7128, -74.0060, 10.0)
            verify(homeViewModel).fetchNearbyHelpers(40.7128, -74.0060, 10.0)
        }
    }

    @Test
    fun testFetchNearbyTasksAndHelpers_LocationNull_Fallback() {
        `when`(fusedLocationProviderClient.lastLocation).thenReturn(Tasks.forResult(null))

        scenario.onFragment { fragment ->
            fragment.fetchNearbyTasksAndHelpers()

            verify(homeViewModel).fetchNearbyTasks(0.0, 0.0, Double.MAX_VALUE)
            verify(homeViewModel).fetchNearbyHelpers(0.0, 0.0, Double.MAX_VALUE)
        }
    }

    @Test
    fun testCheckLocationPermissionAndFetch_PermissionGranted() {
        // Mock permission granted
        val context = ApplicationProvider.getApplicationContext<Context>()
        `when`(PermissionUtils.isLocationPermissionGranted(context)).thenReturn(true)

        scenario.onFragment { fragment ->
            fragment.checkLocationPermissionAndFetch()

            verify(fusedLocationProviderClient).lastLocation
        }
    }

    @Test
    fun testCheckLocationPermissionAndFetch_PermissionDenied() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        `when`(PermissionUtils.isLocationPermissionGranted(context)).thenReturn(false)

        scenario.onFragment { fragment ->
            fragment.checkLocationPermissionAndFetch()

            // Verify location permission is requested
            verify(fragment.locationPermissionLauncher).launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    @Test
    fun testSetUpNearbyTasksRv() {
        scenario.onFragment { fragment ->
            assertNotNull(fragment.binding.rvNearbyTasks.adapter)
        }
    }

    @Test
    fun testSetUpNearbyHelpersRv() {
        scenario.onFragment { fragment ->
            assertNotNull(fragment.binding.rvNearbyHelpers.adapter)
        }
    }



}
