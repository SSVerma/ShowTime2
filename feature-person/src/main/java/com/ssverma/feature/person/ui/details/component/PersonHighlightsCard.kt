package com.ssverma.feature.person.ui.details.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cake
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ssverma.core.ui.theme.spacing
import com.ssverma.feature.person.R
import com.ssverma.shared.domain.model.person.Person

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PersonHighlightsCard(
    person: Person,
    modifier: Modifier = Modifier
) {
    val hasHighlights = person.isDeceased ||
            !person.dob.isNullOrBlank() ||
            person.placeOfBirth.isNotBlank() ||
            person.knownFor.isNotBlank() ||
            person.alsoKnownAs.isNotEmpty()

    if (!hasHighlights) return

    var showAllAliases by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
        ),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = MaterialTheme.spacing.medium)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Life Status / Age Row
            if (person.isDeceased) {
                DeceasedLifeInfoRow(person = person)
            } else if (person.dob != null) {
                LivingLifeInfoRow(person = person)
            }

            // Place of Birth Row
            if (person.placeOfBirth.isNotBlank()) {
                HighlightInfoRow(
                    icon = Icons.Rounded.Place,
                    label = stringResource(id = R.string.place_of_birth),
                    value = person.placeOfBirth
                )
            }

            // Known For Department
            if (person.knownFor.isNotBlank()) {
                HighlightInfoRow(
                    icon = getDepartmentIcon(person.knownFor),
                    label = stringResource(id = R.string.known_for),
                    value = person.knownFor
                )
            }

            // Also Known As / Aliases
            if (person.alsoKnownAs.isNotEmpty()) {
                AliasesInfoRow(
                    aliases = person.alsoKnownAs,
                    showAll = showAllAliases,
                    onToggleShowAll = { showAllAliases = !showAllAliases }
                )
            }
        }
    }
}

@Composable
private fun LivingLifeInfoRow(person: Person) {
    val age = person.age
    val ageValue = if (age != null && !person.dob.isNullOrBlank()) {
        stringResource(id = R.string.person_age_born, age, person.dob.orEmpty())
    } else if (!person.dob.isNullOrBlank()) {
        person.dob.orEmpty()
    } else if (age != null) {
        stringResource(id = R.string.person_age_format, age)
    } else {
        ""
    }

    if (ageValue.isNotBlank()) {
        HighlightInfoRow(
            icon = Icons.Rounded.Cake,
            label = stringResource(id = R.string.person_born),
            value = ageValue
        )
    }
}

@Composable
private fun DeceasedLifeInfoRow(person: Person) {
    val age = person.age
    val lifeSpanText = if (age != null) {
        stringResource(
            id = R.string.person_deceased_format,
            person.dob.orEmpty(),
            person.deathday.orEmpty(),
            age
        )
    } else {
        "${person.dob.orEmpty()} – ${person.deathday.orEmpty()}"
    }

    HighlightInfoRow(
        icon = Icons.Rounded.CalendarMonth,
        label = stringResource(id = R.string.person_deceased_badge),
        value = lifeSpanText,
        badge = stringResource(id = R.string.person_deceased_badge)
    )
}

@Composable
private fun HighlightInfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    badge: String? = null,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.Top,
        modifier = modifier.fillMaxWidth()
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
            modifier = Modifier.size(32.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(top = 1.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (badge != null) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
                    ) {
                        Text(
                            text = badge,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AliasesInfoRow(
    aliases: List<String>,
    showAll: Boolean,
    onToggleShowAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.Top,
        modifier = modifier.fillMaxWidth()
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
            modifier = Modifier.size(32.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Rounded.Translate,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(top = 1.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(id = R.string.person_aliases),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (aliases.size > 3) {
                    Surface(
                        onClick = onToggleShowAll,
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceContainerHigh
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "${aliases.size}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Icon(
                                imageVector = if (showAll) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            val displayedAliases = if (showAll) aliases else aliases.take(3)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                displayedAliases.forEach { alias ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        border = BorderStroke(
                            width = 0.8.dp,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                        )
                    ) {
                        Text(
                            text = alias,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun getDepartmentIcon(department: String): ImageVector {
    return when {
        department.contains("Direct", ignoreCase = true) -> Icons.Rounded.Videocam
        department.contains("Writ", ignoreCase = true) -> Icons.Rounded.Edit
        else -> Icons.Rounded.Movie
    }
}

