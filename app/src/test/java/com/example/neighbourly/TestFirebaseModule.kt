package com.example.neighbourly

import com.example.neighbourly.di.FirebaseModule
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import io.mockk.mockk
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [FirebaseModule::class]
)
object TestFirebaseModule {

    @Provides
    @Singleton
    fun provideMockFirebaseFirestore(): FirebaseFirestore = mockk(relaxed = true)

    @Provides
    @Singleton
    fun provideMockFirebaseAuth(): FirebaseAuth = mockk(relaxed = true)

    @Provides
    @Singleton
    fun provideMockFirebaseStorage(): FirebaseStorage = mockk(relaxed = true)
}
