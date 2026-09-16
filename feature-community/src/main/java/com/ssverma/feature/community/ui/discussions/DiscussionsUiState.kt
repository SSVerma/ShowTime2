package com.ssverma.feature.community.ui.discussions

import androidx.compose.runtime.Immutable
import com.ssverma.common.ui.community.CommentUiModel

@Immutable
data class DiscussionsUiState(
    val isLoading: Boolean = true,
    val comments: List<CommentUiModel> = emptyList()
)
