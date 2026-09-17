package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.ProviderConfig
import com.example.data.model.ProviderHealthStatus
import com.example.data.model.ProviderType

@Composable
fun ProviderSettingsDialog(
    providers: List<ProviderConfig>,
    onSaveKey: (ProviderType, String) -> Unit,
    onToggleEnabled: (ProviderType, Boolean) -> Unit,
    onReorder: (Int, Int) -> Unit,
    onResetQuotas: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .clip(RoundedCornerShape(20.dp)),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Roteador & IAs Gratuitas",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Gerenciamento de cota e ordem de fallback",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Fechar"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Info banner explaining automatic fallback
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Info",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Conforme qualquer IA atingir o limite de crédito ou cota gratuita (HTTP 429), o app avança automaticamente para a próxima IA configurada.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Action to reset quotas
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Ordem de Prioridade das IAs",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    OutlinedButton(
                        onClick = onResetQuotas,
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Redefinir Cotas",
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Redefinir Cotas", fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Providers List
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    itemsIndexed(providers) { index, providerConfig ->
                        ProviderItemCard(
                            config = providerConfig,
                            index = index,
                            totalCount = providers.size,
                            onSaveKey = { key -> onSaveKey(providerConfig.type, key) },
                            onToggleEnabled = { enabled -> onToggleEnabled(providerConfig.type, enabled) },
                            onMoveUp = { onReorder(index, index - 1) },
                            onMoveDown = { onReorder(index, index + 1) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Salvar e Fechar")
                }
            }
        }
    }
}

@Composable
private fun ProviderItemCard(
    config: ProviderConfig,
    index: Int,
    totalCount: Int,
    onSaveKey: (String) -> Unit,
    onToggleEnabled: (Boolean) -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit
) {
    var keyInput by remember(config.apiKey) { mutableStateOf(config.apiKey) }
    var isKeyVisible by remember { mutableStateOf(false) }
    var isEditingKey by remember { mutableStateOf(false) }

    val statusColor = when (config.currentStatus) {
        ProviderHealthStatus.HEALTHY -> Color(0xFF10B981)
        ProviderHealthStatus.QUOTA_EXHAUSTED -> Color(0xFFF59E0B)
        ProviderHealthStatus.KEY_MISSING -> Color(0xFF94A3B8)
        ProviderHealthStatus.ERROR -> Color(0xFFEF4444)
    }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (config.currentStatus == ProviderHealthStatus.QUOTA_EXHAUSTED) Color(0xFFF59E0B) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Priority position badge
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${index + 1}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = config.type.displayName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(statusColor)
                        )
                    }

                    Text(
                        text = config.type.freeTierInfo,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 11.sp
                    )
                }

                // Reorder controls
                IconButton(
                    onClick = onMoveUp,
                    enabled = index > 0,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowUpward,
                        contentDescription = "Mover para Cima",
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = onMoveDown,
                    enabled = index < totalCount - 1,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowDownward,
                        contentDescription = "Mover para Baixo",
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Enable/Disable switch
                Switch(
                    checked = config.isEnabled,
                    onCheckedChange = onToggleEnabled,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            // Status message
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Status: ${config.statusMessage}",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp,
                    color = if (config.currentStatus == ProviderHealthStatus.QUOTA_EXHAUSTED) Color(0xFFF59E0B) else MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (config.successfulRequests > 0) {
                    Text(
                        text = " • ${config.successfulRequests} reqs com sucesso",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            // API Key field (if provider can take a key)
            if (config.type.requiresKey || config.type == ProviderType.GEMINI || config.type == ProviderType.GEMMA_3) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = keyInput,
                    onValueChange = {
                        keyInput = it
                        onSaveKey(it)
                    },
                    label = { Text("Chave de API (${config.type.displayName})") },
                    placeholder = {
                        Text(
                            when (config.type) {
                                ProviderType.GEMINI, ProviderType.GEMMA_3 -> "Usando chave padrão do Google AI Studio"
                                else -> "Cole sua chave gratuita..."
                            }
                        )
                    },
                    visualTransformation = if (isKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { isKeyVisible = !isKeyVisible }) {
                            Icon(
                                imageVector = if (isKeyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Alternar visibilidade",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "✓ Sem necessidade de chave API ou cadastro. Sempre disponível como garantia final!",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF10B981),
                    fontSize = 11.sp
                )
            }
        }
    }
}
