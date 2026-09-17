package com.ssverma.feature.account.ui.profile.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ssverma.core.backup.model.GoogleUser
import com.ssverma.core.ui.component.ShowTimeLoadingIndicator
import com.ssverma.core.ui.theme.spacing
import com.ssverma.feature.account.R
import com.ssverma.feature.account.domain.model.Profile
import com.ssverma.shared.ui.component.Avatar
import com.ssverma.shared.ui.component.ProfileAvatarSharedKey

@Composable
fun ProfileHeader(
    profile: Profile,
    googleUser: GoogleUser?,
    guestPseudonym: String,
    isProActive: Boolean,
    isGuest: Boolean,
    isSigningOut: Boolean,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val displayName = googleUser?.displayName?.takeIf { it.isNotBlank() }
        ?: googleUser?.email?.substringBefore("@")
        ?: profile.displayName.takeIf {
            it.isNotBlank() && !it.equals(
                "Guest User",
                ignoreCase = true
            ) && !it.equals("guest", ignoreCase = true)
        }
        ?: guestPseudonym
    val avatarUrl = googleUser?.photoUrl?.ifBlank { null } ?: profile.imageUrl

    Avatar(
        imageUrl = avatarUrl,
        onClick = {},
        size = 96.dp,
        enableSharedTransition = true,
        sharedContentKey = ProfileAvatarSharedKey
    )

    Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

    Text(
        text = displayName,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
    )

    if (googleUser != null && googleUser.email.isNotBlank()) {
        Text(
            text = googleUser.email,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
        modifier = modifier.padding(top = MaterialTheme.spacing.small)
    ) {
        Surface(
            shape = CircleShape,
            color = if (isProActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
            border = if (!isProActive) BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
            ) else null
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                if (isProActive) {
                    Icon(
                        imageVector = Icons.Rounded.Star,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.showtime_pro),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                } else if (isGuest) {
                    Icon(
                        imageVector = Icons.Rounded.AccountCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.guest),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Icon(
                        imageVector = Icons.Rounded.AccountCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.google_account),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (!isGuest) {
            OutlinedButton(
                onClick = onLogoutClick,
                enabled = !isSigningOut,
                shape = CircleShape,
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                modifier = Modifier.height(32.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                ),
                border = BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                )
            ) {
                if (isSigningOut) {
                    ShowTimeLoadingIndicator(
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.signing_out),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                } else {
                    Text(
                        text = stringResource(R.string.sign_out),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
