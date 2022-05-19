package com.freezer.chatapp.data.model

/**
 * Data class that captures user information for logged in users retrieved from LoginRepository
 */
data class LoggedInUser(
    val phoneNumber: String,
    val firstName: String,
    val lastName: String,
    val avatar: String // Image Path
)