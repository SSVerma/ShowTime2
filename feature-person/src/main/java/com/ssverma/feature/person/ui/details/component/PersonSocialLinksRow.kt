package com.ssverma.feature.person.ui.details.component

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ssverma.core.ui.theme.spacing
import com.ssverma.feature.person.R
import com.ssverma.shared.domain.model.person.PersonExternalIds

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PersonSocialLinksRow(
    externalIds: PersonExternalIds?,
    homepage: String?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val hasLinks = (externalIds?.hasSocialLinks == true) || !homepage.isNullOrBlank()

    if (!hasLinks) return

    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = MaterialTheme.spacing.medium)
    ) {
        // IMDb Profile
        externalIds?.imdbId?.takeIf { it.isNotBlank() }?.let { imdbId ->
            SocialPill(
                label = stringResource(id = R.string.person_imdb),
                icon = Icons.AutoMirrored.Rounded.OpenInNew,
                onClick = { openUrl(context, "https://www.imdb.com/name/$imdbId") }
            )
        }

        // Instagram
        externalIds?.instagramId?.takeIf { it.isNotBlank() }?.let { instaId ->
            SocialPill(
                label = stringResource(id = R.string.person_instagram),
                icon = Icons.Rounded.Link,
                onClick = { openUrl(context, "https://instagram.com/$instaId") }
            )
        }

        // X / Twitter
        externalIds?.twitterId?.takeIf { it.isNotBlank() }?.let { twitterId ->
            SocialPill(
                label = stringResource(id = R.string.person_twitter),
                icon = Icons.Rounded.Link,
                onClick = { openUrl(context, "https://x.com/$twitterId") }
            )
        }

        // TikTok
        externalIds?.tiktokId?.takeIf { it.isNotBlank() }?.let { tiktokId ->
            SocialPill(
                label = stringResource(id = R.string.person_tiktok),
                icon = Icons.Rounded.Link,
                onClick = { openUrl(context, "https://www.tiktok.com/@$tiktokId") }
            )
        }

        // Official Website
        homepage?.takeIf { it.isNotBlank() }?.let { url ->
            SocialPill(
                label = stringResource(id = R.string.person_website),
                icon = Icons.Rounded.Public,
                onClick = { openUrl(context, url) }
            )
        }
    }
}

@Composable
private fun SocialPill(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)
        ),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

private fun openUrl(context: Context, url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    } catch (_: Exception) {
        // Safe fallback if no browser is installed
    }
}

