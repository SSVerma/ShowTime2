package com.ssverma.feature.account.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ssverma.core.ui.DefaultCoreErrorIndicator
import com.ssverma.core.ui.Screen
import com.ssverma.core.ui.ScreenLoadingIndicator
import com.ssverma.feature.account.R
import com.ssverma.feature.account.domain.model.Profile
import com.ssverma.shared.ui.component.Avatar

@Composable
fun ProfileScreen(
    onBackPressed: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val profileUiState by viewModel.profileUiState.collectAsState()

    Screen(
        title = stringResource(R.string.profile),
        onBackPressed = onBackPressed,
        modifier = Modifier.statusBarsPadding()
    ) {
        when (profileUiState) {
            is ProfileUiState.ShowProfileContent -> {
                ProfileContent(
                    profile = (profileUiState as ProfileUiState.ShowProfileContent).profile,
                    onLogoutClick = {
                        viewModel.logout()
                    }
                )
            }
            is ProfileUiState.Error -> {
                DefaultCoreErrorIndicator(
                    failure = (profileUiState as ProfileUiState.Error).failure,
                    onRetry = {
                        viewModel.fetchProfile()
                    }
                )
            }
            ProfileUiState.Loading -> {
                ScreenLoadingIndicator()
            }
        }
    }
}

@Composable
private fun ProfileContent(
    profile: Profile,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxSize()
    ) {
        Avatar(imageUrl = profile.imageUrl, onClick = {}, modifier = Modifier.size(72.dp))

        Text(
            text = stringResource(id = R.string.username_n, profile.userName),
            style = MaterialTheme.typography.caption,
            modifier = Modifier.padding(top = 12.dp)
        )
        Text(
            text = profile.displayName,
            style = MaterialTheme.typography.body1,
            modifier = Modifier.padding(top = 12.dp)
        )

        Button(onClick = onLogoutClick, modifier = Modifier.padding(top = 12.dp)) {
            Text(text = stringResource(R.string.logout))
        }
    }
}