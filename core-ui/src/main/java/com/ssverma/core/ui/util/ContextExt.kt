package com.ssverma.core.ui.util

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.core.net.toUri

/**
 * Opens the application's system settings screen.
 */
fun Context.openAppSettings() {
    try {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
    } catch (_: Exception) {
    }
}

/**
 * Opens the given [url] in an external browser via [Intent.ACTION_VIEW].
 */
fun Context.openWebUrl(
    url: String,
    onError: (() -> Unit)? = null
) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, url.toUri()).apply {
            addCategory(Intent.CATEGORY_BROWSABLE)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
    } catch (_: Exception) {
        onError?.invoke()
    }
}

/**
 * Launches an email client to compose an email to [toEmail] with optional [subject] and [body].
 */
fun Context.sendEmail(
    toEmail: String,
    subject: String? = null,
    body: String? = null,
    onError: (() -> Unit)? = null
) {
    try {
        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = "mailto:$toEmail".toUri()
            if (!subject.isNullOrBlank()) {
                putExtra(Intent.EXTRA_SUBJECT, subject)
            }
            if (!body.isNullOrBlank()) {
                putExtra(Intent.EXTRA_TEXT, body)
            }
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(emailIntent)
    } catch (_: Exception) {
        onError?.invoke()
    }
}

/**
 * Shares plain [text] via [Intent.ACTION_SEND] with an optional chooser [title] and [subject].
 */
fun Context.shareText(
    text: String,
    title: String? = null,
    subject: String? = null,
    onError: (() -> Unit)? = null
) {
    try {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
            if (!subject.isNullOrBlank()) {
                putExtra(Intent.EXTRA_SUBJECT, subject)
            }
        }
        val chooserIntent = Intent.createChooser(sendIntent, title).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(chooserIntent)
    } catch (_: Exception) {
        onError?.invoke()
    }
}

/**
 * Launches the Google Play Store for the app or falls back to [webFallbackUrl].
 */
fun Context.openPlayStore(
    marketUri: String = "market://details?id=$packageName",
    webFallbackUrl: String = "https://play.google.com/store/apps/details?id=$packageName",
    onError: (() -> Unit)? = null
) {
    try {
        val playStoreIntent = Intent(Intent.ACTION_VIEW, marketUri.toUri()).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(playStoreIntent)
    } catch (_: Exception) {
        openWebUrl(url = webFallbackUrl, onError = onError)
    }
}

/**
 * Recursively unwraps this [Context] to find the hosting [Activity], or `null` if none.
 */
tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
