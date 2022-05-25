package com.freezer.chatapp.hilt

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.annotation.Nullable
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {
    @Singleton
    @Provides
    @Named("user")
    @Nullable
    fun providesUser() = FirebaseAuth.getInstance().currentUser

    @Singleton
    @Provides
    @Named("database")
    fun providesFirestoreDatabase() = Firebase.firestore

    @Singleton
    @Provides
    @Named("storage")
    fun providesFirebaseStorage() = FirebaseStorage.getInstance("gs://chatapp-68a8d.appspot.com").reference
}