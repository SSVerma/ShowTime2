package com.ssverma.shared.ui.component.notification

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.ssverma.core.ui.util.findActivity
import com.ssverma.core.ui.util.openAppSettings
import com.ssverma.shared.ui.R

@Stable
class NotificationPermissionHandler internal constructor(
    private val context: Context
) {
    var launcher: ManagedActivityResultLauncher<String, Boolean>? = null

    var showRationaleDialog by mutableStateOf(false)
        internal set

    var showSettingsDialog by mutableStateOf(false)
        internal set

    private var pendingAction: (() -> Unit)? = null

    fun checkHasPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun checkAreNotificationsEnabled(): Boolean {
        return NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    fun handleResume() {
        if (checkHasPermission() && checkAreNotificationsEnabled()) {
            val action = pendingAction
            if (action != null) {
                pendingAction = null
                action()
            }
        }
    }

    fun handlePermissionResult(isGranted: Boolean) {
        if (isGranted) {
            if (checkAreNotificationsEnabled()) {
                val action = pendingAction
                pendingAction = null
                action?.invoke()
            } else {
                showSettingsDialog = true
            }
        } else {
            val activity = context.findActivity()
            if (activity != null &&
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                !ActivityCompat.shouldShowRequestPermissionRationale(
                    activity,
                    Manifest.permission.POST_NOTIFICATIONS
                )
            ) {
                showSettingsDialog = true
            } else {
                showRationaleDialog = true
            }
        }
    }

    fun requestPermissionThen(action: () -> Unit) {
        pendingAction = action
        val hasPermission = checkHasPermission()
        val areNotificationsEnabled = checkAreNotificationsEnabled()

        if (!areNotificationsEnabled && hasPermission) {
            showSettingsDialog = true
        } else if (!hasPermission) {
            val activity = context.findActivity()
            if (activity != null &&
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ActivityCompat.shouldShowRequestPermissionRationale(
                    activity,
                    Manifest.permission.POST_NOTIFICATIONS
                )
            ) {
                showRationaleDialog = true
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                launcher?.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                pendingAction = null
                action()
            }
        } else {
            pendingAction = null
            action()
        }
    }

    fun onRationaleConfirmed() {
        showRationaleDialog = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            launcher?.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    fun onRationaleDismissed() {
        showRationaleDialog = false
        pendingAction = null
    }

    fun onSettingsConfirmed() {
        showSettingsDialog = false
        context.openAppSettings()
    }

    fun onSettingsDismissed() {
        showSettingsDialog = false
        pendingAction = null
    }
}

@Composable
fun rememberNotificationPermissionHandler(): NotificationPermissionHandler {
    val context = LocalContext.current
    val handler = remember(context) { NotificationPermissionHandler(context) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        handler.handlePermissionResult(isGranted)
    }
    handler.launcher = launcher

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, handler) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                handler.handleResume()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    return handler
}

@Composable
fun NotificationRationaleDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = stringResource(R.string.notification_rationale_title),
    description: String = stringResource(R.string.notification_rationale_desc)
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        icon = {
            Icon(
                imageVector = Icons.Rounded.NotificationsActive,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(text = title)
        },
        text = {
            Text(text = description)
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = stringResource(R.string.notification_rationale_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.notification_rationale_dismiss))
            }
        }
    )
}

@Composable
fun NotificationSettingsDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = stringResource(R.string.notification_settings_title),
    description: String = stringResource(R.string.notification_settings_desc)
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        icon = {
            Icon(
                imageVector = Icons.Rounded.NotificationsActive,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(text = title)
        },
        text = {
            Text(text = description)
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = stringResource(R.string.notification_settings_action))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.notification_settings_dismiss))
            }
        }
    )
}

@Composable
fun NotificationPermissionDialogs(
    handler: NotificationPermissionHandler,
    modifier: Modifier = Modifier
) {
    if (handler.showRationaleDialog) {
        NotificationRationaleDialog(
            onConfirm = { handler.onRationaleConfirmed() },
            onDismiss = { handler.onRationaleDismissed() },
            modifier = modifier
        )
    }

    if (handler.showSettingsDialog) {
        NotificationSettingsDialog(
            onConfirm = { handler.onSettingsConfirmed() },
            onDismiss = { handler.onSettingsDismissed() },
            modifier = modifier
        )
    }
}
