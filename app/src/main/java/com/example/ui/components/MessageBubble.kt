package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.example.data.local.MessageEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MessageBubble(
    message: MessageEntity,
    modifier: Modifier = Modifier
) {
    val isUser = message.role == "user"
    val context = LocalContext.current
    var showFullImageDialog by remember { mutableStateOf(false) }
    var showFallbackDetails by remember { mutableStateOf(false) }

    val formattedTime = remember(message.timestamp) {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        sdf.format(Date(message.timestamp))
    }

    if (showFullImageDialog && !message.imageUrl.isNullOrBlank()) {
        ImageViewerDialog(
            imageUrl = message.imageUrl,
            prompt = message.content,
            onDismiss = { showFullImageDialog = false }
        )
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            // AI Avatar
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.tertiary
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "AI",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            modifier = Modifier.widthIn(max = 340.dp),
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
        ) {
            // Provider info header for assistant
            if (!isUser) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 4.dp, start = 2.dp)
                ) {
                    Text(
                        text = message.providerUsed ?: "IA Inteligente",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (!message.modelUsed.isNullOrBlank()) {
                        Text(
                            text = " • ${message.modelUsed}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = " • $formattedTime",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                // Fallback indication badge if rotation took place!
                if (message.fallbackOccurred) {
                    Surface(
                        modifier = Modifier
                            .padding(bottom = 6.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { showFallbackDetails = !showFallbackDetails },
                        color = Color(0xFFF59E0B).copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Rotação Automática",
                                tint = Color(0xFFF59E0B),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Rotação Automática de Cota Ativada",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFF59E0B)
                            )
                            Icon(
                                imageVector = if (showFallbackDetails) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = "Ver Detalhes",
                                tint = Color(0xFFF59E0B),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    AnimatedVisibility(visible = showFallbackDetails) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 6.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(
                                    text = "Histórico de Fallback:",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = message.fallbackDetails ?: "Transição entre provedores realizada.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }

            // Message Bubble Box
            Surface(
                shape = RoundedCornerShape(
                    topStart = if (isUser) 16.dp else 4.dp,
                    topEnd = if (isUser) 4.dp else 16.dp,
                    bottomStart = 16.dp,
                    bottomEnd = 16.dp
                ),
                color = if (isUser) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                },
                tonalElevation = if (isUser) 0.dp else 2.dp,
                modifier = Modifier.border(
                    width = if (isUser) 0.dp else 1.dp,
                    color = if (isUser) Color.Transparent else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(16.dp)
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    // 1. If Image is attached
                    if (!message.imageUrl.isNullOrBlank()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.Black.copy(alpha = 0.2f))
                                .clickable { showFullImageDialog = true },
                            contentAlignment = Alignment.Center
                        ) {
                            SubcomposeAsyncImage(
                                model = message.imageUrl,
                                contentDescription = "Imagem Gerada",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxWidth(),
                                loading = {
                                    Box(
                                        modifier = Modifier.fillMaxWidth(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(32.dp),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            )

                            // Zoom overlay icon
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(8.dp)
                                    .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                    .padding(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ZoomIn,
                                    contentDescription = "Ampliar",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // 2. Parse Markdown Table if present
                    val parsedTable = remember(message.content) {
                        if (!isUser) TableParser.parseMarkdownTable(message.content) else null
                    }

                    // 3. Parse Code blocks if present
                    val codeBlocks = remember(message.content) {
                        if (!isUser) CodeParser.extractCodeBlocks(message.content) else emptyList()
                    }

                    if (parsedTable != null) {
                        DataTableView(table = parsedTable)
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    if (codeBlocks.isNotEmpty()) {
                        codeBlocks.forEach { snippet ->
                            CodeBlockView(
                                language = snippet.language,
                                code = snippet.code,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                        // Remaining non-code text preview if needed
                        val cleanText = message.content.replace(Regex("```[\\s\\S]*?```"), "").trim()
                        if (cleanText.isNotBlank()) {
                            Text(
                                text = cleanText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    } else if (parsedTable == null) {
                        // Standard Text
                        Text(
                            text = message.content,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                            lineHeight = 20.sp
                        )
                    }

                    // Quick copy icon for assistant
                    if (!isUser) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 6.dp),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copiar resposta",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier
                                    .size(16.dp)
                                    .clickable {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText("Mensagem IA", message.content))
                                        Toast.makeText(context, "Resposta copiada!", Toast.LENGTH_SHORT).show()
                                    }
                            )
                        }
                    }
                }
            }

            // User timestamp
            if (isUser) {
                Text(
                    text = formattedTime,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(top = 2.dp, end = 2.dp)
                )
            }
        }

        if (isUser) {
            Spacer(modifier = Modifier.width(8.dp))
            // User Avatar
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "User",
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
