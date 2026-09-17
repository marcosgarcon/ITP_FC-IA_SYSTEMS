package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class ParsedTable(
    val headers: List<String>,
    val rows: List<List<String>>
)

object TableParser {
    fun parseMarkdownTable(text: String): ParsedTable? {
        val lines = text.lines().map { it.trim() }.filter { it.startsWith("|") && it.endsWith("|") }
        if (lines.size < 2) return null

        val rawHeader = lines[0]
        val headers = rawHeader.split("|")
            .map { it.trim() }
            .filterIndexed { index, _ -> index > 0 && index < rawHeader.split("|").lastIndex }

        if (headers.isEmpty()) return null

        // Check delimiter row (e.g. |---|---|)
        val dataLines = lines.drop(1).filter { !it.contains("---") }
        val rows = dataLines.map { line ->
            val cols = line.split("|")
                .map { it.trim() }
                .filterIndexed { index, _ -> index > 0 && index < line.split("|").lastIndex }
            cols
        }

        return ParsedTable(headers, rows)
    }

    fun toCsv(table: ParsedTable): String {
        val sb = StringBuilder()
        sb.append(table.headers.joinToString(",") { "\"$it\"" }).append("\n")
        table.rows.forEach { row ->
            sb.append(row.joinToString(",") { "\"$it\"" }).append("\n")
        }
        return sb.toString()
    }
}

@Composable
fun DataTableView(
    table: ParsedTable,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp)),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            // Header actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.TableChart,
                    contentDescription = "Tabela de dados",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(end = 6.dp)
                )
                Text(
                    text = "Dados Tabulares (${table.rows.size} linhas)",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.weight(1f))
                OutlinedButton(
                    onClick = {
                        val csv = TableParser.toCsv(table)
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Tabela CSV", csv))
                        Toast.makeText(context, "CSV copiado para a área de transferência!", Toast.LENGTH_SHORT).show()
                    },
                    contentPadding = ButtonDefaults.TextButtonContentPadding,
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copiar CSV",
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Text("Copiar CSV", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Scrollable table
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState)
            ) {
                Column {
                    // Table Header Row
                    Row(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        table.headers.forEach { header ->
                            Box(
                                modifier = Modifier
                                    .width(140.dp)
                                    .padding(horizontal = 10.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = header,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    // Table Data Rows
                    table.rows.forEachIndexed { rowIndex, row ->
                        val bg = if (rowIndex % 2 == 0) {
                            Color.Transparent
                        } else {
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                        }
                        Row(
                            modifier = Modifier
                                .background(bg)
                                .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        ) {
                            table.headers.indices.forEach { colIndex ->
                                val cellText = row.getOrNull(colIndex).orEmpty()
                                Box(
                                    modifier = Modifier
                                        .width(140.dp)
                                        .padding(horizontal = 10.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = cellText,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontFamily = FontFamily.Default,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
