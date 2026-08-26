package com.ssverma.shared.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ssverma.core.ui.UiState
import com.ssverma.core.ui.theme.spacing
import com.ssverma.shared.domain.model.Language
import com.ssverma.shared.domain.model.WatchProviderRegion
import com.ssverma.shared.ui.R
import com.ssverma.shared.ui.viewmodel.WatchRegionViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocalizationSelector(
    modifier: Modifier = Modifier,
    viewModel: WatchRegionViewModel = hiltViewModel()
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    val currentRegion by viewModel.currentRegion.collectAsStateWithLifecycle()

    IconButton(
        onClick = {
            scope.launch { sheetState.show() }
            viewModel.loadAvailableRegions()
            viewModel.loadAvailableLanguages()
        },
        modifier = modifier
    ) {
        BadgedBox(
            badge = {
                Badge {
                    Text(text = currentRegion)
                }
            }
        ) {
            Icon(
                imageVector = Icons.Rounded.Public,
                contentDescription = stringResource(R.string.localization_settings)
            )
        }
    }

    if (sheetState.isVisible) {
        LocalizationSettingsBottomSheet(
            viewModel = viewModel,
            sheetState = sheetState,
            onDismiss = { scope.launch { sheetState.hide() } }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocalizationSettingsBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: WatchRegionViewModel = hiltViewModel()
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(Unit) {
        viewModel.loadAvailableRegions()
        viewModel.loadAvailableLanguages()
    }

    LocalizationSettingsBottomSheet(
        viewModel = viewModel,
        sheetState = sheetState,
        onDismiss = onDismissRequest
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocalizationSettingsBottomSheet(
    viewModel: WatchRegionViewModel,
    sheetState: SheetState,
    onDismiss: () -> Unit
) {
    val regionsState by viewModel.regionsState.collectAsStateWithLifecycle()
    val languagesState by viewModel.languagesState.collectAsStateWithLifecycle()

    val currentRegion by viewModel.currentRegion.collectAsStateWithLifecycle()
    val isTranslationEnabled by viewModel.isTranslationEnabled.collectAsStateWithLifecycle()
    val contentLanguage by viewModel.contentLanguage.collectAsStateWithLifecycle()
    val preferredOriginalLanguage by viewModel.preferredOriginalLanguage.collectAsStateWithLifecycle()

    var selectedRegionIso by remember(currentRegion) { mutableStateOf(currentRegion) }
    var translationEnabled by remember(isTranslationEnabled) { mutableStateOf(isTranslationEnabled) }
    var selectedLanguageIso by remember(contentLanguage) { mutableStateOf(contentLanguage) }
    var selectedOriginalLanguageIso by remember(preferredOriginalLanguage) {
        mutableStateOf(preferredOriginalLanguage)
    }

    val regionPickerSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val languagePickerSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    val selectedRegionName = remember(selectedRegionIso, regionsState) {
        (regionsState as? UiState.Success)?.data?.find {
            it.iso31661.equals(
                selectedRegionIso,
                ignoreCase = true
            )
        }?.englishName
            ?: selectedRegionIso
    }

    val selectedLanguageName = remember(selectedLanguageIso, languagesState) {
        (languagesState as? UiState.Success)?.data?.find {
            it.iso6391.equals(
                selectedLanguageIso,
                ignoreCase = true
            )
        }?.englishName
            ?: selectedLanguageIso
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        Text(
                            text = stringResource(R.string.localization_settings),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(horizontal = MaterialTheme.spacing.large)
                        )
                        Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))
                    }

                    // Region Selection Hub Row
                    item {
                        Surface(
                            onClick = { scope.launch { regionPickerSheetState.show() } },
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = MaterialTheme.spacing.large)
                                .padding(top = MaterialTheme.spacing.small)
                        ) {
                            ListItem(
                                headlineContent = {
                                    Text(
                                        text = stringResource(R.string.select_region),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                },
                                supportingContent = {
                                    Text(
                                        text = selectedRegionName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                trailingContent = {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                            )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))
                    }

                    // Translation Toggle
                    item {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = MaterialTheme.spacing.large)
                        ) {
                            ListItem(
                                headlineContent = {
                                    Text(
                                        text = stringResource(R.string.translate_app_content),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                },
                                supportingContent = {
                                    Text(
                                        text = stringResource(R.string.translate_app_desc),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                trailingContent = {
                                    Switch(
                                        checked = translationEnabled,
                                        onCheckedChange = { translationEnabled = it }
                                    )
                                },
                                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                            )
                        }
                    }

                    // Metadata Language Selection (when translation is enabled)
                    if (translationEnabled) {
                        item {
                            Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))
                        }
                        item {
                            Surface(
                                onClick = { scope.launch { languagePickerSheetState.show() } },
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = MaterialTheme.spacing.large)
                            ) {
                                ListItem(
                                    headlineContent = {
                                        Text(
                                            text = stringResource(R.string.select_language),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    },
                                    supportingContent = {
                                        Text(
                                            text = selectedLanguageName,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    },
                                    trailingContent = {
                                        Icon(
                                            imageVector = Icons.Default.Language,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    },
                                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                                )
                            }
                        }
                    }

                    // Content Language Filter (Quick Select)
                    item {
                        SettingsSectionHeader(stringResource(R.string.preferred_content_language))
                    }

                    item {
                        OriginalLanguageQuickSelect(
                            regionIso = selectedRegionIso,
                            selectedLanguageIso = selectedOriginalLanguageIso,
                            allLanguages = (languagesState as? UiState.Success)?.data.orEmpty(),
                            onLanguageSelected = { selectedOriginalLanguageIso = it }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))
                    }
                }
            }

            Button(
                onClick = {
                    viewModel.updateRegion(selectedRegionIso)
                    viewModel.updateTranslationEnabled(translationEnabled)
                    viewModel.updateContentLanguage(selectedLanguageIso)
                    viewModel.updatePreferredOriginalLanguage(selectedOriginalLanguageIso)
                    onDismiss()
                },
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .padding(horizontal = MaterialTheme.spacing.large)
            ) {
                Text(
                    text = stringResource(id = R.string.done),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))
        }
    }

    if (regionPickerSheetState.isVisible) {
        RegionPickerBottomSheet(
            regionsState = regionsState,
            selectedRegionIso = selectedRegionIso,
            sheetState = regionPickerSheetState,
            onRegionSelected = {
                selectedRegionIso = it
                scope.launch { regionPickerSheetState.hide() }
            },
            onRetry = { viewModel.loadAvailableRegions() },
            onDismiss = { scope.launch { regionPickerSheetState.hide() } }
        )
    }

    if (languagePickerSheetState.isVisible) {
        LanguagePickerBottomSheet(
            languagesState = languagesState,
            selectedLanguageIso = selectedLanguageIso,
            sheetState = languagePickerSheetState,
            onLanguageSelected = {
                selectedLanguageIso = it
                scope.launch { languagePickerSheetState.hide() }
            },
            onRetry = { viewModel.loadAvailableLanguages() },
            onDismiss = { scope.launch { languagePickerSheetState.hide() } }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegionPickerBottomSheet(
    regionsState: UiState<List<WatchProviderRegion>, *>,
    selectedRegionIso: String,
    sheetState: SheetState,
    onRegionSelected: (String) -> Unit,
    onRetry: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(bottom = MaterialTheme.spacing.medium)
        ) {
            Text(
                text = stringResource(R.string.select_region),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(horizontal = MaterialTheme.spacing.large)
            )
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))
            RegionPicker(
                regionsState = regionsState,
                selectedRegionIso = selectedRegionIso,
                onRegionSelected = onRegionSelected,
                onRetry = onRetry
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguagePickerBottomSheet(
    languagesState: UiState<List<Language>, *>,
    selectedLanguageIso: String,
    sheetState: SheetState,
    onLanguageSelected: (String) -> Unit,
    onRetry: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(bottom = MaterialTheme.spacing.medium)
        ) {
            Text(
                text = stringResource(R.string.select_language),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(horizontal = MaterialTheme.spacing.large)
            )
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))
            LanguagePicker(
                languagesState = languagesState,
                selectedLanguageIso = selectedLanguageIso,
                onLanguageSelected = onLanguageSelected,
                onRetry = onRetry
            )
        }
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(
            horizontal = MaterialTheme.spacing.large,
            vertical = MaterialTheme.spacing.medium
        )
    )
}

@Composable
fun RegionPicker(
    regionsState: UiState<List<WatchProviderRegion>, *>,
    selectedRegionIso: String,
    onRegionSelected: (String) -> Unit,
    onRetry: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MaterialTheme.spacing.large),
            placeholder = { Text(stringResource(R.string.search_region)) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = null)
                    }
                }
            },
            singleLine = true,
            shape = MaterialTheme.shapes.medium
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            when (regionsState) {
                is UiState.Loading -> {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(MaterialTheme.spacing.medium)
                    )
                }

                is UiState.Success -> {
                    val filtered = regionsState.data.filter {
                        it.englishName.contains(searchQuery, ignoreCase = true) ||
                                it.iso31661.contains(searchQuery, ignoreCase = true)
                    }
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(filtered) { region ->
                            ListItem(
                                headlineContent = { Text(region.englishName) },
                                leadingContent = {
                                    RadioButton(
                                        selected = region.iso31661.equals(
                                            selectedRegionIso,
                                            ignoreCase = true
                                        ),
                                        onClick = null
                                    )
                                },
                                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                                modifier = Modifier.selectable(
                                    selected = region.iso31661.equals(
                                        selectedRegionIso,
                                        ignoreCase = true
                                    ),
                                    onClick = { onRegionSelected(region.iso31661) }
                                )
                            )
                        }
                    }
                }

                is UiState.Error -> {
                    TextButton(onClick = onRetry, modifier = Modifier.align(Alignment.Center)) {
                        Text(stringResource(R.string.retry))
                    }
                }

                else -> {}
            }
        }
    }
}

@Composable
fun LanguagePicker(
    languagesState: UiState<List<Language>, *>,
    selectedLanguageIso: String,
    onLanguageSelected: (String) -> Unit,
    onRetry: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MaterialTheme.spacing.large),
            placeholder = { Text(stringResource(R.string.search_language)) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = null)
                    }
                }
            },
            singleLine = true,
            shape = MaterialTheme.shapes.medium
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            when (languagesState) {
                is UiState.Loading -> {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(MaterialTheme.spacing.medium)
                    )
                }

                is UiState.Success -> {
                    val filtered = languagesState.data.filter {
                        it.englishName.contains(searchQuery, ignoreCase = true) ||
                                it.iso6391.contains(searchQuery, ignoreCase = true)
                    }
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(filtered) { language ->
                            ListItem(
                                headlineContent = { Text(language.englishName) },
                                leadingContent = {
                                    RadioButton(
                                        selected = language.iso6391.equals(
                                            selectedLanguageIso,
                                            ignoreCase = true
                                        ),
                                        onClick = null
                                    )
                                },
                                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                                modifier = Modifier.selectable(
                                    selected = language.iso6391.equals(
                                        selectedLanguageIso,
                                        ignoreCase = true
                                    ),
                                    onClick = { onLanguageSelected(language.iso6391) }
                                )
                            )
                        }
                    }
                }

                is UiState.Error -> {
                    TextButton(onClick = onRetry, modifier = Modifier.align(Alignment.Center)) {
                        Text(stringResource(R.string.retry))
                    }
                }

                else -> {}
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OriginalLanguageQuickSelect(
    regionIso: String,
    selectedLanguageIso: String,
    allLanguages: List<Language>,
    onLanguageSelected: (String) -> Unit
) {
    val languages = remember(regionIso) {
        RegionToOriginalLanguages[regionIso.uppercase()] ?: listOf("en")
    }

    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = MaterialTheme.spacing.large),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
    ) {
        // Option to clear/reset content filter
        FilterChip(
            selected = selectedLanguageIso.isEmpty(),
            onClick = { onLanguageSelected("") },
            label = { Text(stringResource(R.string.global_trending)) }
        )

        languages.forEach { lang ->
            val label =
                allLanguages.find { it.iso6391.equals(lang, ignoreCase = true) }?.englishName
                    ?: lang.uppercase()
            FilterChip(
                selected = selectedLanguageIso.equals(lang, ignoreCase = true),
                onClick = { onLanguageSelected(lang) },
                label = { Text(label) }
            )
        }
    }
}

val RegionToOriginalLanguages = mapOf(
    "IN" to listOf("hi", "ta", "te", "kn", "ml", "bn", "pa", "en"),
    "US" to listOf("en", "es", "fr", "zh"),
    "GB" to listOf("en"),
    "TR" to listOf("tr"),
    "KR" to listOf("ko"),
    "JP" to listOf("ja"),
    "CN" to listOf("zh"),
    "FR" to listOf("fr"),
    "DE" to listOf("de"),
    "ES" to listOf("es"),
    "IT" to listOf("it"),
)
