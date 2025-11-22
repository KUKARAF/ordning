package com.rafael.ordnung

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.rafael.ordnung.ui.screen.AuthScreen
import com.rafael.ordnung.ui.screen.TicketListScreen
import com.rafael.ordnung.ui.theme.OrdnungTheme
import com.rafael.ordnung.ui.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OrdnungTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    OrdnungApp()
                }
            }
        }
    }
}

@Composable
fun OrdnungApp(
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by authViewModel.uiState.collectAsStateWithLifecycle()
    
    if (uiState.isAuthenticated) {
        TicketListScreen()
    } else {
        AuthScreen(onAuthSuccess = {
            // Navigation will be handled by AuthScreen
        })
    }
}