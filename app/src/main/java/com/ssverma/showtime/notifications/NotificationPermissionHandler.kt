package com.ssverma.showtime.notifications

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.ssverma.core.notifications.ShowTimeNotificationManager

@Composable
fun NotificationPermissionHandler(
    notificationManager: ShowTimeNotificationManager,
    canRequest: Boolean
) {
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { _ ->
        // Permission result handled by system
    }

    LaunchedEffect(canRequest) {
        if (canRequest && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!notificationManager.hasNotificationPermission()) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
