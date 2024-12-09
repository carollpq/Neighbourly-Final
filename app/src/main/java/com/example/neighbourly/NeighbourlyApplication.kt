package com.example.neighbourly

import android.app.Application
import com.google.android.libraries.places.api.Places
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class NeighbourlyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // Initialize the Places API with your API Key
        if (!Places.isInitialized()) {
            Places.initialize(this, getString(R.string.google_maps_api_key))
        }
    }
}
