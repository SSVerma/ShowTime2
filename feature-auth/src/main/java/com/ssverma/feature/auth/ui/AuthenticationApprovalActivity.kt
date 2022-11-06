package com.ssverma.feature.auth.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.ssverma.feature.auth.domain.AuthManager
import com.ssverma.feature.auth.domain.model.AuthState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AuthenticationApprovalActivity : ComponentActivity() {
    @Inject
    lateinit var authManager: AuthManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            authManager.updateApprovalStatus(AuthState.Approval.Granted)
            finish()
        }
    }
}