package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.TableRows
import androidx.compose.material.icons.filled.Transform
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AiModality
import com.example.data.model.ExecutionMode
import com.example.data.model.TaskCategory

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ModelAndCategorySelectorBar(
    executionMode: ExecutionMode,
    onExecutionModeSelected: (ExecutionMode) -> Unit,
    selectedCategory: TaskCategory?,
    onCategorySelected: (TaskCategory?) -> Unit,
    selectedModality: AiModality,
    onModalitySelected: (AiModality) -> Unit,
    onOpenProviderSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isModelMenuOpen by remember { mutableStateOf(false) }
    var isCategoryPanelExpanded by remember { mutableStateOf(false) }
    val groupScrollState = rememberScrollState()

    val groups = remember {
        listOf("Todas", "Planilhas & Dados", "Conversão de Documentos", "Executivo & Gestão", "Desenvolvimento", "Conteúdo", "Multimídia", "Idiomas")
    }
    var selectedGroup by remember { mutableStateOf("Todas") }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        border = androidx.compose.foundation.BorderStroke(
            0.5.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp, horizontal = 10.dp)
        ) {
            // Row 1: Model & Execution Mode Switcher + Category Quick Trigger
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Execution Mode Dropdown pill
                Box {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = if (executionMode == ExecutionMode.AUTO_ROTATION) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.secondaryContainer
                        },
                        modifier = Modifier
                            .clip(RoundedCornerShape(18.dp))
                            .clickable { isModelMenuOpen = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (executionMode == ExecutionMode.AUTO_ROTATION) Icons.Default.Refresh else Icons.Default.AutoAwesome,
                                contentDescription = "Modo de Execução",
                                tint = if (executionMode == ExecutionMode.AUTO_ROTATION) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSecondaryContainer
                                },
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = executionMode.label,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (executionMode == ExecutionMode.AUTO_ROTATION) {
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.onSecondaryContainer
                                    }
                                )
                                Text(
                                    text = if (executionMode == ExecutionMode.AUTO_ROTATION) "Fallback cota 429" else "Modelo fixado",
                                    fontSize = 9.sp,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.ExpandMore,
                                contentDescription = "Abrir menu de modelos",
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    // Dropdown menu to switch execution mode & models
                    DropdownMenu(
                        expanded = isModelMenuOpen,
                        onDismissRequest = { isModelMenuOpen = false }
                    ) {
                        ExecutionMode.values().forEach { mode ->
                            val isSelected = mode == executionMode
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = mode.label,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                            )
                                            if (mode == ExecutionMode.AUTO_ROTATION) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(4.dp))
                                                        .background(Color(0xFF10B981).copy(alpha = 0.2f))
                                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                                ) {
                                                    Text(
                                                        text = "Recomendado",
                                                        fontSize = 9.sp,
                                                        color = Color(0xFF047857),
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                        Text(
                                            text = mode.description,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 10.sp
                                        )
                                    }
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = if (mode == ExecutionMode.AUTO_ROTATION) Icons.Default.Refresh else Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                    )
                                },
                                onClick = {
                                    onExecutionModeSelected(mode)
                                    isModelMenuOpen = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Category Quick Filter toggle button
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = if (selectedCategory != null || isCategoryPanelExpanded) {
                        MaterialTheme.colorScheme.tertiaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(18.dp))
                        .clickable { isCategoryPanelExpanded = !isCategoryPanelExpanded }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = getCategoryIcon(selectedCategory),
                            contentDescription = "Categoria",
                            tint = if (selectedCategory != null) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = selectedCategory?.title ?: "Categorias & Tarefas",
                            fontSize = 12.sp,
                            fontWeight = if (selectedCategory != null) FontWeight.Bold else FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = if (selectedCategory != null) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = if (isCategoryPanelExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = "Expandir categorias",
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Configure providers shortcut icon
                IconButton(
                    onClick = onOpenProviderSettings,
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Configurar IAs",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Row 2: Selected Category Banner / Pill (if active)
            if (selectedCategory != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Modo Especialista: ${selectedCategory.title} (${selectedCategory.group})",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Limpar categoria",
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier
                                .size(14.dp)
                                .clickable { onCategorySelected(null) }
                        )
                    }
                }
            }

            // Row 3: Expandable Category Grid Panel
            AnimatedVisibility(
                visible = isCategoryPanelExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    // Category Groups horizontal filter pills
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(groupScrollState)
                            .padding(bottom = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        groups.forEach { groupName ->
                            val isSelected = groupName == selectedGroup
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedGroup = groupName },
                                label = { Text(groupName, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                ),
                                modifier = Modifier.height(28.dp)
                            )
                        }
                    }

                    // Filtered Categories Cards
                    val displayedCategories = remember(selectedGroup) {
                        if (selectedGroup == "Todas") {
                            TaskCategory.values().toList()
                        } else {
                            TaskCategory.values().filter { it.group == selectedGroup }
                        }
                    }

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        displayedCategories.forEach { category ->
                            val isCategoryActive = category == selectedCategory
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isCategoryActive) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                },
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isCategoryActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                                ),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        if (isCategoryActive) {
                                            onCategorySelected(null)
                                        } else {
                                            onCategorySelected(category)
                                            isCategoryPanelExpanded = false
                                        }
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = getCategoryIcon(category),
                                        contentDescription = category.title,
                                        tint = if (isCategoryActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text(
                                            text = category.title,
                                            fontSize = 11.sp,
                                            fontWeight = if (isCategoryActive) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isCategoryActive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = category.subtitle,
                                            fontSize = 9.sp,
                                            color = MaterialTheme.colorScheme.outline,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}

private fun getCategoryIcon(category: TaskCategory?): ImageVector {
    return when (category) {
        null -> Icons.Default.AutoAwesome
        TaskCategory.PROGRAMMING -> Icons.Default.Code
        TaskCategory.TEXT_CREATION -> Icons.Default.Edit
        TaskCategory.SHEET_CREATOR -> Icons.Default.TableChart
        TaskCategory.SHEET_ANALYSIS -> Icons.Default.Insights
        TaskCategory.EXECUTIVE_DASHBOARD -> Icons.Default.TableRows
        TaskCategory.DOC_TO_EXCEL -> Icons.Default.Transform
        TaskCategory.PDF_TO_EXCEL -> Icons.Default.PictureAsPdf
        TaskCategory.EXCEL_TO_DOC -> Icons.Default.Description
        TaskCategory.EXCEL_TO_PDF -> Icons.Default.PictureAsPdf
        TaskCategory.IMAGE_CREATOR -> Icons.Default.Image
        TaskCategory.TRANSLATOR -> Icons.Default.Translate
        TaskCategory.VIDEO_CREATOR -> Icons.Default.Movie
    }
}
