package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ConversationEntity
import com.example.data.model.AiModality
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ConversationsDrawerContent(
    conversations: List<ConversationEntity>,
    activeConversationId: String?,
    onSelectConversation: (String) -> Unit,
    onNewConversation: (AiModality) -> Unit,
    onDeleteConversation: (String) -> Unit,
    onCloseDrawer: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxHeight()
            .width(320.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Chat,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Histórico Unificado",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Todas as IAs em um só lugar",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // New Conversation Action
            Button(
                onClick = {
                    onNewConversation(AiModality.CHAT)
                    onCloseDrawer()
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Nova Conversa",
                    modifier = Modifier.padding(end = 6.dp)
                )
                Text("Nova Conversa")
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Conversas Recentes (${conversations.size})",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            // Conversation items
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(conversations) { conv ->
                    val isSelected = conv.id == activeConversationId
                    val modeEnum = try {
                        AiModality.valueOf(conv.mode)
                    } catch (_: Exception) {
                        AiModality.CHAT
                    }

                    val modeIcon = when (modeEnum) {
                        AiModality.CHAT -> Icons.Default.Chat
                        AiModality.CODE -> Icons.Default.Code
                        AiModality.SHEET -> Icons.Default.TableChart
                        AiModality.IMAGE -> Icons.Default.Image
                    }

                    val formattedDate = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())
                        .format(Date(conv.updatedAt))

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                        },
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSelectConversation(conv.id)
                                onCloseDrawer()
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surface),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = modeIcon,
                                    contentDescription = modeEnum.label,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = conv.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${modeEnum.label} • $formattedDate",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            }

                            IconButton(
                                onClick = { onDeleteConversation(conv.id) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "Excluir",
                                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
