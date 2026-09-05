package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.example.data.AuthRepository
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.theme.ForestObsidian
import com.example.ui.theme.WildGuardTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WildGuardTheme(darkTheme = true) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = ForestObsidian
                ) {
                    val authRepository = remember { AuthRepository.getInstance() }
                    val currentUser by authRepository.currentUser.collectAsState()

                    val user = currentUser
                    if (user != null) {
                        DashboardScreen(
                            user = user,
                            onSignOut = {
                                authRepository.signOut()
                            }
                        )
                    } else {
                        AuthScreen(
                            onAuthSuccess = {
                                // currentUser will update automatically via StateFlow
                            }
                        )
                    }
                }
            }
        }
    }
}
