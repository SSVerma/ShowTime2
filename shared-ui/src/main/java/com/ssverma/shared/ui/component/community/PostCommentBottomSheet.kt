package com.ssverma.shared.ui.component.community

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ssverma.shared.ui.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostCommentBottomSheet(
    sheetState: SheetState,
    onDismissRequest: () -> Unit,
    onPostComment: (content: String, isSpoiler: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var text by remember { mutableStateOf("") }
    var isSpoiler by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = stringResource(id = R.string.join_the_discussion),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = text,
                onValueChange = { if (it.length <= 500) text = it },
                placeholder = {
                    Text(text = stringResource(id = R.string.post_thought_hint))
                },
                minLines = 3,
                maxLines = 6,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
                supportingText = {
                    Text(
                        text = "${text.length}/500",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Spoiler warning toggle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = isSpoiler,
                    onCheckedChange = { isSpoiler = it }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(id = R.string.contains_spoilers_toggle),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = if (isSpoiler) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Action Buttons
            Row(
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                TextButton(onClick = onDismissRequest) {
                    Text(text = stringResource(id = R.string.cancel_action))
                }

                Spacer(modifier = Modifier.width(12.dp))

                Button(
                    onClick = {
                        if (text.isNotBlank()) {
                            onPostComment(text.trim(), isSpoiler)
                            text = ""
                            isSpoiler = false
                            onDismissRequest()
                        }
                    },
                    enabled = text.isNotBlank(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Send,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 6.dp)
                    )
                    Text(
                        text = stringResource(id = R.string.post_action),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
