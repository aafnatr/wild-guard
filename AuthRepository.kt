package com.example.data

import com.example.model.User
import com.example.model.UserLocation
import com.example.model.UserType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class AuthRepository private constructor() {

    private val defaultUser = User(
        id = "USR-OPERATOR-01",
        name = "Captain Anita Roy",
        email = "anita.roy@wildguard.org",
        userType = UserType.SYSTEM_OPERATOR,
        location = UserLocation.PRESETS[0]
    )

    private val _currentUser = MutableStateFlow<User?>(defaultUser)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    fun signUp(
        name: String,
        email: String,
        password: String,
        userType: UserType,
        location: UserLocation,
        onSuccess: () -> Unit
    ) {
        val user = User(
            id = "USR-" + UUID.randomUUID().toString().take(6).uppercase(),
            name = name.trim().ifEmpty { "Ranger Member" },
            email = email.trim(),
            userType = userType,
            location = location
        )
        _currentUser.value = user
        onSuccess()
    }

    fun signIn(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (email.isBlank() || password.isBlank()) {
            onError("Please enter both email and password")
            return
        }
        val isOperator = email.contains("operator", ignoreCase = true) || email.contains("admin", ignoreCase = true)
        val user = User(
            id = "USR-" + UUID.randomUUID().toString().take(6).uppercase(),
            name = if (isOperator) "Officer Dev Sharma" else email.substringBefore("@").replace(".", " ").capitalizeWords(),
            email = email.trim(),
            userType = if (isOperator) UserType.SYSTEM_OPERATOR else UserType.COMMUNITY_USER,
            location = UserLocation.PRESETS[0]
        )
        _currentUser.value = user
        onSuccess()
    }

    fun switchUserType(newType: UserType) {
        _currentUser.value = _currentUser.value?.copy(userType = newType)
    }

    fun updateLocation(location: UserLocation) {
        _currentUser.value = _currentUser.value?.copy(location = location)
    }

    fun signOut() {
        _currentUser.value = null
    }

    companion object {
        @Volatile
        private var instance: AuthRepository? = null

        fun getInstance(): AuthRepository {
            return instance ?: synchronized(this) {
                instance ?: AuthRepository().also { instance = it }
            }
        }
    }
}

private fun String.capitalizeWords(): String =
    split(" ").joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
