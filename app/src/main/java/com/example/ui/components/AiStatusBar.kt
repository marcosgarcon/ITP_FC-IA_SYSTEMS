package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ProviderConfig
import com.example.data.model.ProviderHealthStatus
import com.example.data.model.ProviderType

@Composable
fun AiStatusBar(
    providers: List<ProviderConfig>,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .border(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                RoundedCornerShape(0.dp)
            ),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
    ) {
        Column(modifier = Modifier.padding(vertical = 6.dp, horizontal = 12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onOpenSettings() }
                ) {
                    Icon(
                        imageVector = Icons.Default.Hub,
                        contentDescription = "Router Status",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Ordem de Fallback Automático:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Settings button
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                        .clickable { onOpenSettings() }
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Configurações",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Ajustar",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Chain horizontal list
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
                    .horizontalScroll(scrollState),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val enabledProviders = providers.filter { it.isEnabled }
                enabledProviders.forEachIndexed { index, config ->
                    val statusColor = when (config.currentStatus) {
                        ProviderHealthStatus.HEALTHY -> Color(0xFF10B981) // Green
                        ProviderHealthStatus.QUOTA_EXHAUSTED -> Color(0xFFF59E0B) // Amber / 429
                        ProviderHealthStatus.KEY_MISSING -> Color(0xFF94A3B8) // Gray
                        ProviderHealthStatus.ERROR -> Color(0xFFEF4444) // Red
                    }

                    val shortName = when (config.type) {
                        ProviderType.GEMINI -> "${index + 1}. Gemini"
                        ProviderType.GEMMA_3 -> "${index + 1}. Gemma 3"
                        ProviderType.GROQ -> "${index + 1}. Groq"
                        ProviderType.OPEN_ROUTER -> "${index + 1}. OpenRouter"
                        ProviderType.POLLINATIONS -> "${index + 1}. Pollinations"
                    }

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (config.currentStatus == ProviderHealthStatus.QUOTA_EXHAUSTED) Color(0xFFF59E0B) else MaterialTheme.colorScheme.outlineVariant
                        ),
                        modifier = Modifier
                            .clickable { onOpenSettings() }
                            .padding(end = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(statusColor)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = shortName,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (config.currentStatus == ProviderHealthStatus.QUOTA_EXHAUSTED) {
                                Text(
                                    text = " (429)",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFF59E0B)
                                )
                            }
                        }
                    }

                    if (index < enabledProviders.lastIndex) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Próxima IA",
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier
                                .size(12.dp)
                                .padding(horizontal = 2.dp)
                        )
                    }
                }
            }
        }
    }
}
