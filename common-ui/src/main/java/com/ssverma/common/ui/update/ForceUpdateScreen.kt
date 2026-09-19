package com.ssverma.common.ui.update

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.SystemUpdate
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ssverma.common.ui.R
import com.ssverma.core.ui.theme.spacing
import com.ssverma.core.ui.util.openPlayStore
import com.ssverma.shared.domain.utils.AppConfigConstants

/**
 * Full-screen blocking UI shown when the currently installed app version is below
 * the minimum supported threshold ([AppConfigConstants.KEY_MIN_SUPPORTED_VERSION_CODE]).
 *
 * System back navigation is blocked, and the user must update via Google Play to proceed.
 */
@Composable
fun ForceUpdateScreen(
    title: String? = null,
    message: String? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Block back button navigation completely
    BackHandler(enabled = true) {
        // Hard-block: User cannot back out of force update
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(MaterialTheme.spacing.large)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(96.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = Icons.Rounded.SystemUpdate,
                        contentDescription = stringResource(id = R.string.update_icon_cd),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))

            Text(
                text = title?.takeIf { it.isNotBlank() }
                    ?: stringResource(id = R.string.update_required_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

            Text(
                text = message?.takeIf { it.isNotBlank() }
                    ?: stringResource(id = R.string.update_required_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = MaterialTheme.spacing.small)
            )

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.extraLarge))

            Button(
                onClick = {
                    context.openPlayStore(
                        marketUri = AppConfigConstants.PLAY_STORE_MARKET_URI,
                        webFallbackUrl = AppConfigConstants.PLAY_STORE_URL
                    )
                },
                shape = MaterialTheme.shapes.large,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.update_action_update_now),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
