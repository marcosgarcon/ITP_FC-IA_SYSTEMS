package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.AiModality
import com.example.data.model.ExecutionMode
import com.example.data.model.TaskCategory
import com.example.ui.components.AiStatusBar
import com.example.ui.components.ConversationsDrawerContent
import com.example.ui.components.MessageBubble
import com.example.ui.components.ModelAndCategorySelectorBar
import com.example.ui.components.ModeSelectorBar
import com.example.ui.components.ProviderSettingsDialog
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: ChatViewModel,
    modifier: Modifier = Modifier
) {
    val conversations by viewModel.conversationsState.collectAsStateWithLifecycle()
    val currentConvId by viewModel.currentConversationId.collectAsStateWithLifecycle()
    val messages by viewModel.currentMessagesState.collectAsStateWithLifecycle()
    val providers by viewModel.providersState.collectAsStateWithLifecycle()
    val modality by viewModel.selectedModality.collectAsStateWithLifecycle()
    val executionMode by viewModel.executionMode.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedTaskCategory.collectAsStateWithLifecycle()
    val imageStyle by viewModel.selectedImageStyle.collectAsStateWithLifecycle()
    val inputText by viewModel.inputText.collectAsStateWithLifecycle()
    val isGenerating by viewModel.isGenerating.collectAsStateWithLifecycle()
    val fallbackAlert by viewModel.fallbackAlert.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val focusManager = LocalFocusManager.current

    var showSettingsDialog by remember { mutableStateOf(false) }

    // Auto-scroll to latest message when messages list changes
    LaunchedEffect(messages.size, isGenerating) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    if (showSettingsDialog) {
        ProviderSettingsDialog(
            providers = providers,
            onSaveKey = { type, key -> viewModel.saveProviderKey(type, key) },
            onToggleEnabled = { type, enabled -> viewModel.toggleProvider(type, enabled) },
            onReorder = { from, to -> viewModel.reorderPriority(from, to) },
            onResetQuotas = { viewModel.resetQuotas() },
            onDismiss = { showSettingsDialog = false }
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                ConversationsDrawerContent(
                    conversations = conversations,
                    activeConversationId = currentConvId,
                    onSelectConversation = { id -> viewModel.selectConversation(id) },
                    onNewConversation = { mode -> viewModel.startNewConversation(mode) },
                    onDeleteConversation = { id -> viewModel.deleteConversation(id) },
                    onCloseDrawer = {
                        coroutineScope.launch { drawerState.close() }
                    }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "AI Multi-Hub",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(MaterialTheme.colorScheme.primaryContainer)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "Fallback Ativo",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 9.sp,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            val currentTitle = conversations.find { it.id == currentConvId }?.title ?: modality.label
                            Text(
                                text = currentTitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontSize = 11.sp
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { coroutineScope.launch { drawerState.open() } }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Abrir Histórico"
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { viewModel.startNewConversation() }) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Nova Conversa"
                            )
                        }
                        IconButton(onClick = { showSettingsDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = "Configurar IAs"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
            modifier = modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .imePadding()
            ) {
                // Real-time Provider status and Fallback Chain
                AiStatusBar(
                    providers = providers,
                    onOpenSettings = { showSettingsDialog = true }
                )

                // Model Selection & Task Categories Switcher Bar
                ModelAndCategorySelectorBar(
                    executionMode = executionMode,
                    onExecutionModeSelected = { viewModel.setExecutionMode(it) },
                    selectedCategory = selectedCategory,
                    onCategorySelected = { viewModel.setTaskCategory(it) },
                    selectedModality = modality,
                    onModalitySelected = { viewModel.setModality(it) },
                    onOpenProviderSettings = { showSettingsDialog = true }
                )

                // Modality Selection Pills (Chat, Code, Sheet, Image)
                ModeSelectorBar(
                    selectedModality = modality,
                    onModalitySelected = { viewModel.setModality(it) },
                    selectedImageStyle = imageStyle,
                    onImageStyleSelected = { viewModel.setImageStyle(it) }
                )

                // Fallback Notification Banner
                AnimatedVisibility(visible = fallbackAlert != null) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFF59E0B).copy(alpha = 0.18f)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Fallback",
                                tint = Color(0xFFF59E0B),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = fallbackAlert.orEmpty(),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f),
                                fontSize = 11.sp
                            )
                            IconButton(
                                onClick = { viewModel.dismissFallbackAlert() },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Fechar",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                // Error Message Banner
                AnimatedVisibility(visible = errorMessage != null) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Erro",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = errorMessage.orEmpty(),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.weight(1f),
                                fontSize = 11.sp
                            )
                            IconButton(
                                onClick = { viewModel.dismissErrorMessage() },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Fechar",
                                    tint = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                // Messages list or Empty State
                Box(modifier = Modifier.weight(1f)) {
                    if (messages.isEmpty()) {
                        EmptyStateView(
                            modality = modality,
                            selectedCategory = selectedCategory,
                            onSuggestionClick = { prompt ->
                                viewModel.applyPromptSuggestion(prompt)
                            }
                        )
                    } else {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(messages) { msg ->
                                MessageBubble(message = msg)
                            }

                            if (isGenerating) {
                                item {
                                    GeneratingIndicator()
                                }
                            }
                        }
                    }
                }

                // Bottom Input Row
                BottomInputBar(
                    text = inputText,
                    onTextChanged = { viewModel.setInputText(it) },
                    onSend = {
                        focusManager.clearFocus()
                        viewModel.sendMessage()
                    },
                    isGenerating = isGenerating,
                    modality = modality,
                    selectedCategory = selectedCategory
                )
            }
        }
    }
}

@Composable
private fun EmptyStateView(
    modality: AiModality,
    selectedCategory: TaskCategory?,
    onSuggestionClick: (String) -> Unit
) {
    val suggestions = if (selectedCategory != null) {
        selectedCategory.samplePrompts
    } else {
        when (modality) {
            AiModality.CHAT -> listOf(
                "Explique computação quântica de forma simples e intuitiva",
                "Dicas comprovadas para gerenciar melhor meu tempo de estudos",
                "Resuma as principais vantagens de usar Kotlin com Jetpack Compose"
            )
            AiModality.CODE -> listOf(
                "Escreva uma função em Kotlin para validar CPF com cálculo de dígitos",
                "Crie um componente Compose de botão animado com efeito shimmer",
                "Algoritmo de busca binária em Python com testes unitários"
            )
            AiModality.SHEET -> listOf(
                "Tabela de controle financeiro pessoal mensal com colunas e fórmulas",
                "Planilha de metas de vendas com cálculo de comissão e PROCV",
                "Tabela comparativa de custos de servidores em nuvem com soma total"
            )
            AiModality.IMAGE -> listOf(
                "Robô futurista assistente trabalhando em um jardim zen à noite",
                "Cidade cyberpunk iluminada com hologramas e reflexos de chuva",
                "Pintura em aquarela de um farol clássico na costa rochosa ao entardecer"
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = selectedCategory?.title ?: "Hub de IAs com Fallback Inteligente",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = selectedCategory?.subtitle
                ?: "Gemini 2.5, Gemma 3, Groq, OpenRouter e Pollinations integrados. Quando a cota esgota, a próxima IA assume automaticamente.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp, bottom = 18.dp)
        )

        Text(
            text = if (selectedCategory != null) "Prompts prontos para ${selectedCategory.title}:" else "Sugestões para ${modality.label}:",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        suggestions.forEach { suggestion ->
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { onSuggestionClick(suggestion) }
            ) {
                Text(
                    text = "💡 \"$suggestion\"",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    }
}

@Composable
private fun GeneratingIndicator() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = "Processando requisição...",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Roteador ativo: Se a IA atingir limite de cota, alternará automaticamente.",
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
private fun BottomInputBar(
    text: String,
    onTextChanged: (String) -> Unit,
    onSend: () -> Unit,
    isGenerating: Boolean,
    modality: AiModality,
    selectedCategory: TaskCategory? = null
) {
    val placeholder = if (selectedCategory != null) {
        when (selectedCategory) {
            TaskCategory.PROGRAMMING -> "Descreva o código, função ou bug para resolver..."
            TaskCategory.TEXT_CREATION -> "Descreva o tema, tom e objetivo do texto..."
            TaskCategory.SHEET_CREATOR -> "Descreva as colunas e dados da planilha a criar..."
            TaskCategory.SHEET_ANALYSIS -> "Cole a tabela ou dados e peça as análises e métricas..."
            TaskCategory.EXECUTIVE_DASHBOARD -> "Descreva os indicadores para o Dashboard executivo (PDF e slides)..."
            TaskCategory.DOC_TO_EXCEL -> "Cole o texto do documento para converter em colunas Excel..."
            TaskCategory.PDF_TO_EXCEL -> "Cole o texto extraído do PDF para converter em planilha Excel..."
            TaskCategory.EXCEL_TO_DOC -> "Cole os dados da planilha para redigir relatório formal..."
            TaskCategory.EXCEL_TO_PDF -> "Cole os dados da planilha para estruturar relatório PDF..."
            TaskCategory.IMAGE_CREATOR -> "Descreva a cena visual que deseja criar..."
            TaskCategory.TRANSLATOR -> "Digite ou cole o texto e indique o idioma de destino..."
            TaskCategory.VIDEO_CREATOR -> "Descreva o tema, duração e plataforma do vídeo..."
        }
    } else {
        when (modality) {
            AiModality.CHAT -> "Digite sua mensagem..."
            AiModality.CODE -> "Descreva o código ou problema a resolver..."
            AiModality.SHEET -> "Descreva a planilha, dados ou fórmula..."
            AiModality.IMAGE -> "Descreva a cena visual que deseja criar..."
        }
    }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = onTextChanged,
                placeholder = { Text(placeholder, fontSize = 13.sp) },
                maxLines = 4,
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { onSend() })
            )

            Surface(
                shape = CircleShape,
                color = if (text.isNotBlank() && !isGenerating) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .size(48.dp)
                    .clickable(enabled = text.isNotBlank() && !isGenerating) { onSend() }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (isGenerating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Enviar",
                            tint = if (text.isNotBlank()) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
