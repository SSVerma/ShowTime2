package com.ssverma.showtime.ui.library.navigation

import androidx.navigation.NavGraphBuilder
import com.ssverma.core.navigation.StandaloneDestination
import com.ssverma.core.navigation.composable
import com.ssverma.showtime.ui.library.LibraryScreen

object LibraryHomeDestination : StandaloneDestination("library/home")

fun NavGraphBuilder.libraryHomeGraph() = composable(
    destination = LibraryHomeDestination
) {
    LibraryScreen()
}