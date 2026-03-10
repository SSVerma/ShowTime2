package com.ssverma.shared.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Surface
import com.ssverma.shared.ui.R
import com.ssverma.shared.ui.LocalAppStateHolder
import com.ssverma.shared.domain.model.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomePageAppBar(
    onSearchIconPressed: () -> Unit,
    onAccountIconPressed: () -> Unit,
    modifier: Modifier = Modifier,
    colors: TopAppBarColors = TopAppBarDefaults.topAppBarColors()
) {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .size(32.dp),
                    painter = painterResource(id = R.drawable.ic_launcher),
                    contentDescription = null
                )
                Text(
                    text = stringResource(id = R.string.app_name),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        },
        actions = {
            WatchRegionSelector()

            IconButton(onClick = onSearchIconPressed) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = stringResource(R.string.search)
                )
            }

            var extendedMenuExpanded by remember { mutableStateOf(false) }
            var showThemeMenu by remember { mutableStateOf(false) }
            val triggerAppInfo = com.ssverma.shared.ui.LocalAppInfoTrigger.current

            Box {
                IconButton(onClick = { extendedMenuExpanded = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More Options"
                    )
                }

                DropdownMenu(
                    expanded = extendedMenuExpanded,
                    onDismissRequest = { extendedMenuExpanded = false },
                    modifier = Modifier.width(200.dp)
                ) {
                    DropdownMenuItem(
                        leadingIcon = {
                            Icon(Icons.Rounded.Palette, contentDescription = null, modifier = Modifier.size(20.dp))
                        },
                        text = { Text(stringResource(R.string.theme)) },
                        onClick = {
                            extendedMenuExpanded = false
                            showThemeMenu = true
                        }
                    )
                    DropdownMenuItem(
                        leadingIcon = {
                            Icon(Icons.Rounded.Info, contentDescription = null, modifier = Modifier.size(20.dp))
                        },
                        text = { Text(stringResource(R.string.app_info)) },
                        onClick = {
                            extendedMenuExpanded = false
                            triggerAppInfo()
                        }
                    )
                    DropdownMenuItem(
                        leadingIcon = {
                            Icon(Icons.Rounded.AccountCircle, contentDescription = null, modifier = Modifier.size(20.dp))
                        },
                        text = { Text(stringResource(R.string.account)) },
                        onClick = {
                            extendedMenuExpanded = false
                            onAccountIconPressed()
                        }
                    )
                    // Theme Dialog logic handles showing the selection
                }
            }

            if (showThemeMenu) {
                AppThemeDialog(
                    onDismiss = { showThemeMenu = false }
                )
            }
        },
        colors = colors,
        modifier = modifier
    )
}

@Composable
fun AppThemeDialog(
    onDismiss: () -> Unit
) {
    val appStateHolder = LocalAppStateHolder.current
    val appTheme by appStateHolder.appTheme.collectAsState(initial = AppTheme.System)
    val isDynamicColorEnabled by appStateHolder.isDynamicColorEnabled.collectAsState(initial = false)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = stringResource(id = R.string.theme))
        },
        text = {
            Column {
                AppTheme.entries.forEach { theme ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = (theme == appTheme),
                                onClick = { appStateHolder.updateAppTheme(theme) }
                            )
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (theme == appTheme),
                            onClick = { appStateHolder.updateAppTheme(theme) }
                        )
                        Text(
                            text = theme.name,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                }

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(id = R.string.dynamic_color),
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = stringResource(id = R.string.dynamic_color_desc),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = isDynamicColorEnabled,
                            onCheckedChange = { appStateHolder.updateDynamicColor(it) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.done))
            }
        }
    )
}
