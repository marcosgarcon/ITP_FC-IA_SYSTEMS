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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
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

data class CodeSnippet(
    val language: String,
    val code: String
)

object CodeParser {
    fun extractCodeBlocks(text: String): List<CodeSnippet> {
        val snippets = mutableListOf<CodeSnippet>()
        val regex = Regex("```(\\w*)\\n?([\\s\\S]*?)```")
        val matches = regex.findAll(text)
        for (match in matches) {
            val lang = match.groupValues[1].ifBlank { "Código" }
            val code = match.groupValues[2].trim()
            snippets.add(CodeSnippet(lang, code))
        }
        return snippets
    }
}

@Composable
fun CodeBlockView(
    language: String,
    code: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, Color(0xFF334155), RoundedCornerShape(12.dp)),
        color = Color(0xFF0F172A) // Dark developer terminal canvas
    ) {
        Column {
            // Language bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1E293B))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Code,
                    contentDescription = "Code",
                    tint = Color(0xFF38BDF8),
                    modifier = Modifier.padding(end = 6.dp)
                )
                Text(
                    text = language.uppercase(),
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.weight(1f))
                OutlinedButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Código", code))
                        Toast.makeText(context, "Código copiado!", Toast.LENGTH_SHORT).show()
                    },
                    contentPadding = ButtonDefaults.TextButtonContentPadding,
                    modifier = Modifier.height(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copiar Código",
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Text("Copiar", color = Color(0xFF38BDF8), fontSize = 11.sp)
                }
            }

            // Code content
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState)
                    .padding(12.dp)
            ) {
                Text(
                    text = code,
                    color = Color(0xFFE2E8F0),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }
        }
    }
}
