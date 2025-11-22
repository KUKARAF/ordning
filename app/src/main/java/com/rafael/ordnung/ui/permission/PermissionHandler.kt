package com.rafael.ordnung.ui.permission

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale

data class Permission(
    val permission: String,
    val title: String,
    val description: String,
    val isRequired: Boolean = true
)

object AppPermissions {
    val CALENDAR = listOf(
        Permission(
            permission = Manifest.permission.READ_CALENDAR,
            title = "Calendar Access",
            description = "Needed to create calendar events from your tickets"
        ),
        Permission(
            permission = Manifest.permission.WRITE_CALENDAR,
            title = "Calendar Write",
            description = "Needed to create calendar events from your tickets"
        )
    )
    
    val STORAGE = listOf(
        Permission(
            permission = Manifest.permission.READ_EXTERNAL_STORAGE,
            title = "Storage Access",
            description = "Needed to read PDF tickets from your device"
        ),
        Permission(
            permission = Manifest.permission.WRITE_EXTERNAL_STORAGE,
            title = "Storage Write",
            description = "Needed to save tickets and sync with Google Drive"
        )
    )
    
    val NOTIFICATIONS = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        listOf(
            Permission(
                permission = Manifest.permission.POST_NOTIFICATIONS,
                title = "Notifications",
                description = "Needed to show travel reminders and lockscreen QR codes"
            )
        )
    } else emptyList()
    
    val OVERLAY = listOf(
        Permission(
            permission = Manifest.permission.SYSTEM_ALERT_WINDOW,
            title = "Overlay Permission",
            description = "Needed to display QR codes on lockscreen",
            isRequired = false
        )
    )
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionHandler(
    permissions: List<Permission>,
    onAllPermissionsGranted: () -> Unit,
    content: @Composable (requestPermissions: () -> Unit) -> Unit
) {
    val context = LocalContext.current
    
    // Check if all permissions are granted
    val allPermissionsGranted = permissions.all { permission ->
        ContextCompat.checkSelfPermission(
            context,
            permission.permission
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    // Create permission states
    val permissionStates = permissions.map { permission ->
        rememberPermissionState(permission.permission) {
            // Handle permission denial
        }
    }
    
    // Permission launcher for multiple permissions
    val multiplePermissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            onAllPermissionsGranted()
        }
    }
    
    // Check if any permission requires rationale
    val shouldShowRationale = permissionStates.any { it.status.shouldShowRationale }
    
    LaunchedEffect(allPermissionsGranted) {
        if (allPermissionsGranted) {
            onAllPermissionsGranted()
        }
    }
    
    content {
        multiplePermissionsLauncher.launch(
            permissions.map { it.permission }.toTypedArray()
        )
    }
}

@Composable
fun PermissionRationaleDialog(
    permissions: List<Permission>,
    onDismiss: () -> Unit,
    onGrant: () -> Unit
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { androidx.compose.material3.Text("Permissions Required") },
        text = {
            androidx.compose.material3.Column {
                permissions.forEach { permission ->
                    androidx.compose.material3.Text(
                        text = "${permission.title}: ${permission.description}",
                        modifier = androidx.compose.foundation.layout.padding(vertical = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            androidx.compose.material3.TextButton(onClick = onGrant) {
                androidx.compose.material3.Text("Grant Permissions")
            }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) {
                androidx.compose.material3.Text("Cancel")
            }
        }
    )
}