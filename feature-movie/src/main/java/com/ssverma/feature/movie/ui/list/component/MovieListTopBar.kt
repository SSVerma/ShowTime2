package com.ssverma.feature.movie.ui.list.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.ssverma.core.ui.component.ShowTimeTopAppBar
import com.ssverma.feature.movie.R
import com.ssverma.feature.movie.ui.list.MoviePaginatedListUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieListTopBar(
    uiState: MoviePaginatedListUiState,
    onToggleViewMode: () -> Unit,
    onOpenFilters: () -> Unit,
    onBackPressed: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior
) {
    val title = uiState.title ?: stringResource(id = uiState.titleRes)

    ShowTimeTopAppBar(
        title = title,
        onBackPressed = onBackPressed,
        navIcon = Icons.Default.ArrowBack,
        scrollBehavior = scrollBehavior,
        actions = {
            IconButton(onClick = onToggleViewMode) {
                Icon(
                    imageVector = if (uiState.isGridView) Icons.AutoMirrored.Filled.List else Icons.Default.Menu,
                    contentDescription = null
                )
            }

            if (uiState.isFilterApplicable) {
                IconButton(onClick = onOpenFilters) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_filter),
                        contentDescription = stringResource(id = R.string.filter)
                    )
                }
            }
        }
    )
}
