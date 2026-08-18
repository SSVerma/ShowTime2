package com.ssverma.shared.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.VideoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ssverma.shared.domain.model.AppTheme
import com.ssverma.shared.ui.LocalAppStateHolder
import com.ssverma.shared.ui.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomePageAppBar(
    onSearchIconPressed: () -> Unit,
    onAccountIconPressed: () -> Unit,
    modifier: Modifier = Modifier,
    onLibraryIconPressed: (() -> Unit)? = null,
    colors: TopAppBarColors = TopAppBarDefaults.topAppBarColors()
) {
    var extendedMenuExpanded by remember { mutableStateOf(false) }
    var showThemeMenu by remember { mutableStateOf(false) }
    val triggerAppInfo = com.ssverma.shared.ui.LocalAppInfoTrigger.current

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
            LocalizationSelector()

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
                    onLibraryIconPressed?.let { onLibraryClick ->
                        DropdownMenuItem(
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Rounded.VideoLibrary,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            text = { Text(stringResource(R.string.library)) },
                            onClick = {
                                extendedMenuExpanded = false
                                onLibraryClick()
                            }
                        )
                    }
                    DropdownMenuItem(
                        leadingIcon = {
                            Icon(
                                Icons.Rounded.Palette,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        text = { Text(stringResource(R.string.theme)) },
                        onClick = {
                            extendedMenuExpanded = false
                            showThemeMenu = true
                        }
                    )
                    DropdownMenuItem(
                        leadingIcon = {
                            Icon(
                                Icons.Rounded.Info,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        text = { Text(stringResource(R.string.app_info)) },
                        onClick = {
                            extendedMenuExpanded = false
                            triggerAppInfo()
                        }
                    )
                    DropdownMenuItem(
                        leadingIcon = {
                            Icon(
                                Icons.Rounded.AccountCircle,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        text = { Text(stringResource(R.string.account)) },
                        onClick = {
                            extendedMenuExpanded = false
                            onAccountIconPressed()
                        }
                    )
                }
            }

            if (showThemeMenu) {
                AppThemeDialog(
                    onDismiss = { showThemeMenu = false },
                    onProRequired = {
                        showThemeMenu = false
                        onAccountIconPressed()
                    }
                )
            }
        },
        colors = colors,
        modifier = modifier
    )
}

@Composable
fun AppThemeDialog(
    onDismiss: () -> Unit,
    onProRequired: () -> Unit = {}
) {
    val appStateHolder = LocalAppStateHolder.current
    val appTheme by appStateHolder.appTheme.collectAsState(initial = AppTheme.System)
    val isProActive by appStateHolder.isProActive.collectAsState(initial = false)
    val isDynamicColorEnabled by appStateHolder.isDynamicColorEnabled.collectAsState(initial = false)

    val handleThemeSelection: (AppTheme) -> Unit = { theme ->
        if (theme == AppTheme.OledMidnight && !isProActive) {
            onDismiss()
            onProRequired()
        } else {
            appStateHolder.updateAppTheme(theme)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = stringResource(id = R.string.theme))
        },
        text = {
            Column {
                AppTheme.entries.forEach { theme ->
                    val themeTitle = when (theme) {
                        AppTheme.System -> stringResource(R.string.theme_system)
                        AppTheme.Light -> stringResource(R.string.theme_light)
                        AppTheme.Dark -> stringResource(R.string.theme_dark)
                        AppTheme.OledMidnight -> stringResource(R.string.theme_oled_midnight)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = (theme == appTheme),
                                onClick = { handleThemeSelection(theme) }
                            )
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (theme == appTheme),
                            onClick = { handleThemeSelection(theme) }
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = themeTitle,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        if (theme == AppTheme.OledMidnight) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = MaterialTheme.shapes.extraSmall,
                                color = Color(0xFFFF9800)
                            ) {
                                Text(
                                    text = "PRO",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
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
