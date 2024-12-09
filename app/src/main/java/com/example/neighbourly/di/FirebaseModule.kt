package com.example.neighbourly.di

import com.example.neighbourly.repositories.TaskMarketplaceRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    @Provides
    @Singleton
    fun provideFirebaseStorage() = FirebaseStorage.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseFirestore() = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseAuth() = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideTaskMarketplaceRepository(
        firestore: FirebaseFirestore,
        auth: FirebaseAuth,
        storage: FirebaseStorage
    ): TaskMarketplaceRepository {
        return TaskMarketplaceRepository(firestore, auth, storage)
    }

}
