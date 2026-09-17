package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AiModality

@Composable
fun ModeSelectorBar(
    selectedModality: AiModality,
    onModalitySelected: (AiModality) -> Unit,
    selectedImageStyle: String,
    onImageStyleSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val stylesScroll = rememberScrollState()

    val styles = listOf("Realista", "Cyberpunk", "Anime", "Render 3D", "Pintura Digital", "Aquarela")

    Column(modifier = modifier.fillMaxWidth()) {
        // Main Modality Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
                .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AiModality.values().forEach { modality ->
                val isSelected = modality == selectedModality
                val icon = when (modality) {
                    AiModality.CHAT -> Icons.Default.Chat
                    AiModality.CODE -> Icons.Default.Code
                    AiModality.SHEET -> Icons.Default.TableChart
                    AiModality.IMAGE -> Icons.Default.Image
                }

                FilterChip(
                    selected = isSelected,
                    onClick = { onModalitySelected(modality) },
                    label = { Text(modality.label, fontSize = 12.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = icon,
                            contentDescription = modality.label,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        selectedLeadingIconColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }

        // Secondary style chips if in IMAGE mode
        AnimatedVisibility(visible = selectedModality == AiModality.IMAGE) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(stylesScroll)
                    .padding(horizontal = 12.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Palette,
                    contentDescription = "Estilos",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "Estilo:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                styles.forEach { style ->
                    val isStyleSelected = style == selectedImageStyle
                    FilterChip(
                        selected = isStyleSelected,
                        onClick = { onImageStyleSelected(style) },
                        label = { Text(style, fontSize = 11.sp) },
                        modifier = Modifier.padding(0.dp)
                    )
                }
            }
        }
    }
}
