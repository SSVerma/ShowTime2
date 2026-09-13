package com.ssverma.showtime.ui.onboarding.step

import android.app.Activity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CloudSync
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ssverma.core.backup.model.BackupMetadata
import com.ssverma.core.backup.model.GoogleUser
import com.ssverma.core.ui.theme.spacing
import com.ssverma.core.ui.util.findActivity
import com.ssverma.shared.backup.ui.component.CloudBackupFoundCard
import com.ssverma.showtime.R
import com.ssverma.showtime.ui.onboarding.illustration.CloudVaultArt

@Composable
fun OnboardingCloudStep(
    googleUser: GoogleUser?,
    isSigningIn: Boolean,
    lastBackupMetadata: BackupMetadata?,
    isRestoringBackup: Boolean,
    isBackupRestored: Boolean,
    isBackupRestoreSkipped: Boolean,
    onSignInWithGoogle: (Activity) -> Unit,
    onRestoreBackup: () -> Unit,
    onSkipRestoreBackup: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showStartFreshConfirmDialog by remember { mutableStateOf(false) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = MaterialTheme.spacing.large)
    ) {
        Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

        // Badge
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                    shape = CircleShape
                )
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Text(
                text = stringResource(id = R.string.onboarding_cloud_badge),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                ),
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

        // Vault graphic
        CloudVaultArt(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        )

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

        Text(
            text = stringResource(id = R.string.onboarding_cloud_title),
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

        Text(
            text = stringResource(id = R.string.onboarding_cloud_tagline),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = MaterialTheme.spacing.small)
        )

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

        // 2 Perk Cards
        CloudPerkCard(
            icon = Icons.Rounded.Storage,
            title = stringResource(id = R.string.onboarding_cloud_perk_offline_title),
            description = stringResource(id = R.string.onboarding_cloud_perk_offline_desc)
        )

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

        CloudPerkCard(
            icon = Icons.Rounded.CloudSync,
            title = stringResource(id = R.string.onboarding_cloud_perk_backup_title),
            description = stringResource(id = R.string.onboarding_cloud_perk_backup_desc)
        )

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))

        // Google Sign-In, Backup Found Card, or Connected state
        if (googleUser != null) {
            val hasBackup = lastBackupMetadata != null && (
                    lastBackupMetadata.favoritesCount + lastBackupMetadata.watchlistCount +
                            lastBackupMetadata.historyCount + lastBackupMetadata.customListsCount +
                            lastBackupMetadata.showProgressCount > 0
                    )

            if (hasBackup && !isBackupRestoreSkipped) {
                CloudBackupFoundCard(
                    metadata = lastBackupMetadata,
                    isRestoring = isRestoringBackup,
                    isRestored = isBackupRestored,
                    onRestoreClick = onRestoreBackup,
                    onSkipClick = { showStartFreshConfirmDialog = true },
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = stringResource(
                                    id = R.string.onboarding_cloud_signed_in_as,
                                    googleUser.displayName.ifBlank { googleUser.email }
                                ),
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = googleUser.email,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
                            )
                        }
                    }
                }
            }
        } else {
            OutlinedButton(
                onClick = {
                    context.findActivity()?.let { onSignInWithGoogle(it) }
                },
                enabled = !isSigningIn,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                if (isSigningIn) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Rounded.CloudSync,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = stringResource(id = R.string.onboarding_cloud_sign_in_google),
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

            Text(
                text = stringResource(id = R.string.onboarding_cloud_guest_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = MaterialTheme.spacing.medium)
            )
        }

        if (showStartFreshConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showStartFreshConfirmDialog = false },
                shape = RoundedCornerShape(24.dp),
                title = {
                    Text(
                        text = stringResource(id = R.string.onboarding_cloud_start_fresh_dialog_title),
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(text = stringResource(id = R.string.onboarding_cloud_start_fresh_dialog_msg))
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showStartFreshConfirmDialog = false
                            onSkipRestoreBackup()
                        }
                    ) {
                        Text(text = stringResource(id = R.string.onboarding_cloud_start_fresh_dialog_confirm))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showStartFreshConfirmDialog = false }) {
                        Text(text = stringResource(id = R.string.cancel))
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))
    }
}

@Composable
private fun CloudPerkCard(
    icon: ImageVector,
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(
                horizontal = MaterialTheme.spacing.medium,
                vertical = MaterialTheme.spacing.small
            )
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(MaterialTheme.spacing.medium))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
