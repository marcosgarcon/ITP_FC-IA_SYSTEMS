package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.SubcomposeAsyncImage

@Composable
fun ImageViewerDialog(
    imageUrl: String,
    prompt: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.92f)),
            color = Color.Transparent
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Top bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Imagem Gerada por IA",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Fechar",
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Image container
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF1E293B)),
                    contentAlignment = Alignment.Center
                ) {
                    SubcomposeAsyncImage(
                        model = imageUrl,
                        contentDescription = prompt,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize(),
                        loading = {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Prompt description
                Text(
                    text = prompt,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFCBD5E1),
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(imageUrl))
                            context.startActivity(intent)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.OpenInBrowser,
                            contentDescription = "Abrir no navegador",
                            modifier = Modifier.padding(end = 6.dp)
                        )
                        Text("Abrir Alta Resolução")
                    }

                    Spacer(modifier = Modifier.padding(horizontal = 6.dp))

                    Button(
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, "Veja a imagem gerada por IA: $imageUrl")
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Compartilhar imagem"))
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Compartilhar",
                            modifier = Modifier.padding(end = 6.dp)
                        )
                        Text("Compartilhar")
                    }
                }
            }
        }
    }
}
