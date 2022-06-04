package com.ssverma.feature.library.navigation

import androidx.navigation.NavGraphBuilder
import com.ssverma.core.navigation.composable

fun NavGraphBuilder.libraryHomeGraph() = composable(
    destination = LibraryHomeDestination
) {
    com.ssverma.feature.library.ui.LibraryScreen()
}