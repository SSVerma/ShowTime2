package com.ssverma.common.ui.appinfo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ssverma.common.ui.R
import com.ssverma.core.ui.layout.ShowTimeBottomSheet
import com.ssverma.core.ui.theme.spacing

@Immutable
private data class OpenSourceLibrary(
    val name: String,
    val author: String,
    val license: String,
    val description: String
)

private val OpenSourceLibraries = listOf(
    OpenSourceLibrary(
        name = "Jetpack Compose",
        author = "The Android Open Source Project",
        license = "Apache 2.0",
        description = "Modern declarative UI toolkit for Android"
    ),
    OpenSourceLibrary(
        name = "Kotlin & Coroutines",
        author = "JetBrains s.r.o.",
        license = "Apache 2.0",
        description = "First-class asynchronous programming and serialization"
    ),
    OpenSourceLibrary(
        name = "AndroidX Room",
        author = "The Android Open Source Project",
        license = "Apache 2.0",
        description = "Local SQLite object mapping and offline caching"
    ),
    OpenSourceLibrary(
        name = "Hilt & Dagger",
        author = "Google LLC",
        license = "Apache 2.0",
        description = "Compile-time dependency injection framework"
    ),
    OpenSourceLibrary(
        name = "Retrofit & OkHttp",
        author = "Square, Inc.",
        license = "Apache 2.0",
        description = "Type-safe HTTP client for TMDB and cloud services"
    ),
    OpenSourceLibrary(
        name = "Coil",
        author = "Coil Contributors",
        license = "Apache 2.0",
        description = "Fast, lightweight image loading for Android Compose"
    ),
    OpenSourceLibrary(
        name = "AndroidX WorkManager",
        author = "The Android Open Source Project",
        license = "Apache 2.0",
        description = "Reliable background execution for release reminders & backup"
    ),
    OpenSourceLibrary(
        name = "Material 3 Design System",
        author = "Google LLC",
        license = "Apache 2.0",
        description = "Expressive Material You theming and adaptive components"
    ),
    OpenSourceLibrary(
        name = "Firebase Android SDK",
        author = "Google LLC",
        license = "Apache 2.0",
        description = "Remote Config, Firestore community, and Authentication"
    ),
    OpenSourceLibrary(
        name = "Google Play Billing Library",
        author = "Google LLC",
        license = "Android SDK",
        description = "Secure in-app purchases and ShowTime Pro entitlements"
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OpenSourceLicensesBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    ShowTimeBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MaterialTheme.spacing.large)
                .padding(bottom = MaterialTheme.spacing.large)
        ) {
            Text(
                text = stringResource(R.string.open_source_licenses_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.extraSmall))

            Text(
                text = stringResource(R.string.open_source_licenses_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

            LazyColumn(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.smallMedium)
            ) {
                items(
                    items = OpenSourceLibraries,
                    key = { it.name },
                    contentType = { "library_card" }
                ) { lib ->
                    LibraryLicenseCard(library = lib)
                }
            }

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))

            Button(
                onClick = onDismissRequest,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = MaterialTheme.shapes.large
            ) {
                Text(
                    text = stringResource(R.string.app_info_close),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun LibraryLicenseCard(
    library: OpenSourceLibrary,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(MaterialTheme.spacing.medium)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = library.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                ) {
                    Text(
                        text = library.license,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = library.author,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = library.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
