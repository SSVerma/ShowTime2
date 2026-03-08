package com.ssverma.showtime

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint

import androidx.compose.runtime.CompositionLocalProvider
import com.ssverma.shared.ui.AppStateHolder
import com.ssverma.shared.ui.LocalAppStateHolder
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    @Inject lateinit var appStateHolder: AppStateHolder

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            CompositionLocalProvider(LocalAppStateHolder provides appStateHolder) {
                ShowTime()
            }
        }
    }
}