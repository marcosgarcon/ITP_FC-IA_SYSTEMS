package com.example.data.model

enum class AiModality(val label: String, val iconName: String) {
    CHAT("Geral", "Chat"),
    CODE("Código", "Terminal"),
    SHEET("Planilha", "Table"),
    IMAGE("Imagem", "Image")
}

/**
 * Execution mode: either Auto-Rotation (Fallback chain across all healthy providers)
 * or Pinning a specific AI model (Gemini, Gemma 3, Groq, OpenRouter, Pollinations).
 */
enum class ExecutionMode(val label: String, val description: String) {
    AUTO_ROTATION("Rotação Automática", "Alterna entre IAs automaticamente em caso de limite de cota 429"),
    PINNED_GEMINI("Google Gemini 2.5", "Força o uso prioritário do Google Gemini"),
    PINNED_GEMMA_3("Google Gemma 3 27B", "Força o uso do modelo aberto de última geração Google Gemma 3"),
    PINNED_GROQ("Groq LLaMA 3.3", "Inferência ultrarrápida LPU Groq"),
    PINNED_OPEN_ROUTER("OpenRouter Free", "Modelos abertos sem custo"),
    PINNED_POLLINATIONS("Pollinations AI", "Livre, gratuito e ilimitado sem necessidade de chave")
}

/**
 * Detailed Task Categories requested by user:
 * Programação, Criação de textos, Planilhas, Dashboard Executivo com gerador de PDF e slides,
 * Análises de planilhas, Criador de planilhas, Conversão de documentos (Doc -> Excel, PDF -> Excel,
 * Excel -> Doc, Excel -> PDF), Criador de imagens, Tradutor, Criação de roteiros de vídeos.
 */
enum class TaskCategory(
    val id: String,
    val title: String,
    val subtitle: String,
    val group: String,
    val associatedModality: AiModality,
    val defaultPromptPrefix: String,
    val samplePrompts: List<String>
) {
    PROGRAMMING(
        id = "programming",
        title = "Programação & Dev",
        subtitle = "Código limpo, depuração, APIs e arquitetura",
        group = "Desenvolvimento",
        associatedModality = AiModality.CODE,
        defaultPromptPrefix = "[MODO: PROGRAMAÇÃO SÊNIOR]\nAtue como Engenheiro de Software Sênior. Forneça código limpo, arquitetura escalável e testes.",
        samplePrompts = listOf(
            "Crie um componente Jetpack Compose com animação suave de loading e shimmer",
            "Escreva uma API REST completa em Kotlin Ktor com autenticação JWT e rotas CRUD",
            "Refatore este algoritmo para complexidade O(n) e adicione testes unitários"
        )
    ),
    TEXT_CREATION(
        id = "text_creation",
        title = "Criação de Textos",
        subtitle = "Artigos, emails, redações e storytelling",
        group = "Conteúdo",
        associatedModality = AiModality.CHAT,
        defaultPromptPrefix = "[MODO: REDAÇÃO PROFISSIONAL]\nAtue como Redator e Copywriter Especialista. Escreva textos claros, envolventes e bem estruturados.",
        samplePrompts = listOf(
            "Escreva um artigo persuasivo sobre como a IA está transformando a produtividade diária",
            "Crie um comunicado executivo formal para toda a empresa sobre lançamento de novo produto",
            "Escreva um roteiro de apresentação de vendas de 5 minutos com técnica de storytelling"
        )
    ),
    SHEET_CREATOR(
        id = "sheet_creator",
        title = "Criador de Planilhas",
        subtitle = "Geração de tabelas, fórmulas PROCV, SOMA e macros",
        group = "Planilhas & Dados",
        associatedModality = AiModality.SHEET,
        defaultPromptPrefix = "[MODO: CRIADOR DE PLANILHAS EXCEL/SHEETS]\nAtue como Especialista em Modelagem de Dados e Planilhas. Gere tabelas estruturadas em Markdown e liste fórmulas prontas.",
        samplePrompts = listOf(
            "Crie uma planilha de fluxo de caixa mensal com receitas, despesas, saldo e projeção anual",
            "Monte uma tabela de controle de estoque com estoque mínimo, giro e cálculo de reposição",
            "Tabela de metas de vendas com cálculo dinâmico de comissão e bônus progressivo"
        )
    ),
    SHEET_ANALYSIS(
        id = "sheet_analysis",
        title = "Análises de Planilhas",
        subtitle = "Diagnósticos, KPIs, insights estatísticos e tendências",
        group = "Planilhas & Dados",
        associatedModality = AiModality.SHEET,
        defaultPromptPrefix = "[MODO: ANÁLISE AVANÇADA DE DADOS E PLANILHAS]\nAtue como Cientista e Analista de Dados. Analise a planilha abaixo, identifique anomalias, calcule métricas chave e extraia insights acionáveis.",
        samplePrompts = listOf(
            "Analise os dados financeiros da tabela e aponte os 3 maiores centros de custo e oportunidades de economia",
            "Avalie o histórico de vendas trimestrais e projete a taxa de crescimento para o próximo semestre",
            "Faça uma análise de correlação entre investimento em marketing e conversão de novos clientes"
        )
    ),
    EXECUTIVE_DASHBOARD(
        id = "dashboard_exec",
        title = "Dashboard Executivo (PDF & Slides)",
        subtitle = "Relatórios de diretoria, estrutura de slides e gerador para PDF",
        group = "Executivo & Gestão",
        associatedModality = AiModality.SHEET,
        defaultPromptPrefix = "[MODO: DASHBOARD EXECUTIVO COM ESTRUTURA PARA PDF E SLIDES]\nAtue como Consultor Estratégico Executivo (McKinsey/Bain). Crie um Dashboard Executivo completo com seções: 1. Sumário Executivo; 2. Tabela de KPIs Principais; 3. Roteiro Slide a Slide pronto para apresentação; 4. Relatório Formatado pronto para exportação em PDF.",
        samplePrompts = listOf(
            "Crie um Dashboard Executivo de Performance Financeira com KPIs de EBITDA, Margem Líquida e estrutura de 5 slides",
            "Monte um Dashboard de Diretoria de Operações com taxa de entrega, SLA e plano de ação executivo em PDF",
            "Estruture uma apresentação de Pitch Deck executivo de 7 slides com métricas de tração e mercado"
        )
    ),
    DOC_TO_EXCEL(
        id = "doc_to_excel",
        title = "Doc ➔ Excel",
        subtitle = "Extrair e tabular dados de texto/documentos em planilhas",
        group = "Conversão de Documentos",
        associatedModality = AiModality.SHEET,
        defaultPromptPrefix = "[MODO: CONVERSÃO DE DOC PARA EXCEL/CSV]\nConverta o documento/texto a seguir em uma planilha Excel perfeitamente estruturada com colunas, linhas padronizadas e tipos de dados consistentes. Inclua a tabela formatada e o bloco CSV para download.",
        samplePrompts = listOf(
            "Converta este relatório em texto de despesas de viagem em uma planilha com Data, Categoria, Valor e Comprovante",
            "Extraia a lista de funcionários, cargos e salários deste memorando para uma tabela estruturada para Excel",
            "Transforme este contrato de prestação de serviços em uma matriz de obrigações e prazos em tabela"
        )
    ),
    PDF_TO_EXCEL(
        id = "pdf_to_excel",
        title = "PDF ➔ Excel",
        subtitle = "Converter tabelas e extratos de PDF em planilha Excel",
        group = "Conversão de Documentos",
        associatedModality = AiModality.SHEET,
        defaultPromptPrefix = "[MODO: CONVERSÃO DE PDF PARA EXCEL]\nProcesse o conteúdo textual extraído de PDF e converta-o em uma planilha estruturada com colunas limpas, numéricos formatados e fórmulas adequadas.",
        samplePrompts = listOf(
            "Converta este extrato bancário de PDF em colunas: Data, Descrição, Documento, Débito, Crédito e Saldo",
            "Transforme a fatura em PDF colada em uma tabela com Itens, Quantidade, Valor Unitário e Total",
            "Organize a listagem de inventário de patrimônio de PDF em planilha com Código, Descrição e Depreciação"
        )
    ),
    EXCEL_TO_DOC(
        id = "excel_to_doc",
        title = "Excel ➔ Doc",
        subtitle = "Transformar dados de planilhas em relatórios executivos escritos",
        group = "Conversão de Documentos",
        associatedModality = AiModality.CHAT,
        defaultPromptPrefix = "[MODO: CONVERSÃO DE EXCEL PARA DOCUMENTO EXECUTIVO/DOC]\nTransforme a tabela/planilha de dados a seguir em um relatório escrito formal, narrativo, detalhado e profissional (estilo Microsoft Word/Google Docs).",
        samplePrompts = listOf(
            "Transforme a planilha de resultados do 3º trimestre em um relatório executivo formal com introdução, destaques e conclusões",
            "Converta a tabela de auditoria de conformidade em um parecer técnico por extenso",
            "Escreva uma carta de prestação de contas detalhada a partir desta tabela financeira"
        )
    ),
    EXCEL_TO_PDF(
        id = "excel_to_pdf",
        title = "Excel ➔ PDF",
        subtitle = "Formatação de dados para emissão de relatório PDF final",
        group = "Conversão de Documentos",
        associatedModality = AiModality.SHEET,
        defaultPromptPrefix = "[MODO: PREPARAÇÃO DE EXCEL PARA RELATÓRIO PDF FORMAL]\nEstruture e formate os dados desta planilha em um layout de relatório final pronto para emissão e impressão em PDF, com cabeçalhos formais, sumário executivo, tabela paginada e notas de rodapé.",
        samplePrompts = listOf(
            "Formate esta planilha de balanço patrimonial para um relatório oficial pronto para PDF com notas explicativas",
            "Crie o layout de um relatório de despesas de projeto pronto para impressão formal em PDF",
            "Prepare uma demonstração de resultados consolidada formatada para envio executivo em PDF"
        )
    ),
    IMAGE_CREATOR(
        id = "image_creator",
        title = "Criador de Imagens",
        subtitle = "Geração de artes conceituais, fotos realistas e ilustrações",
        group = "Multimídia",
        associatedModality = AiModality.IMAGE,
        defaultPromptPrefix = "",
        samplePrompts = listOf(
            "Dashboard executivo moderno em holograma 3D com gráficos financeiros luminosos",
            "Ilustração minimalista de arquitetura moderna em meio a floresta ao pôr do sol",
            "Foto realista de estúdio de produto tecnológico futurista com iluminação cinematográfica"
        )
    ),
    TRANSLATOR(
        id = "translator",
        title = "Tradutor Poliglota",
        subtitle = "Tradução contextual, técnica e idiomática avançada",
        group = "Idiomas",
        associatedModality = AiModality.CHAT,
        defaultPromptPrefix = "[MODO: TRADUTOR ESPECIALISTA CONTEXTUAL]\nAtue como Tradutor Profissional e Intérprete Diplomático. Traduza o texto mantendo nuances culturais, termos técnicos e naturalidade fluente. Apresente a tradução principal e comentários sobre termos específicos.",
        samplePrompts = listOf(
            "Traduza este contrato jurídico do inglês para português mantendo rigor terminológico",
            "Traduza este manual técnico para espanhol e francês com terminologia industrial",
            "Localize este texto de marketing para o mercado norte-americano com expressões idiomáticas naturais"
        )
    ),
    VIDEO_CREATOR(
        id = "video_creator",
        title = "Criação de Roteiros de Vídeos",
        subtitle = "Roteiros para YouTube, Reels, TikTok, áudio, cenas e prompts",
        group = "Multimídia",
        associatedModality = AiModality.CHAT,
        defaultPromptPrefix = "[MODO: DIRETOR E ROTEIRISTA DE VÍDEOS]\nAtue como Roteirista e Diretor Audiovisual. Crie um roteiro completo de vídeo estruturado em: [Tempo] | [Cena/Visual] | [Áudio/Locução] | [Prompts para IA de Vídeo como Sora/Runway] | [Texto na Tela (CTA)].",
        samplePrompts = listOf(
            "Roteiro de 60 segundos para Reels/TikTok sobre curiosidades de inteligência artificial com gancho inicial magnético",
            "Roteiro completo de vídeo de 10 minutos para YouTube sobre finanças para iniciantes",
            "Roteiro para comercial de 30 segundos com prompts de geração de cena para Runway/Sora"
        )
    )
}

enum class ProviderType(val displayName: String, val defaultModel: String, val requiresKey: Boolean, val freeTierInfo: String) {
    GEMINI("Google Gemini", "gemini-2.5-flash", false, "15 req/min grátis"),
    GEMMA_3("Google Gemma 3", "gemma-3-27b-it", false, "Open Model Google (Grátis)"),
    GROQ("Groq Cloud", "llama-3.3-70b-versatile", true, "Ultra-rápido, tier grátis"),
    OPEN_ROUTER("OpenRouter Free", "meta-llama/llama-3.3-70b-instruct:free", true, "Modelos abertos sem custo"),
    POLLINATIONS("Pollinations AI", "openai", false, "100% Grátis e Ilimitado (Sem Chave!)")
}

enum class ProviderHealthStatus {
    HEALTHY,
    QUOTA_EXHAUSTED, // 429 or rate-limit
    KEY_MISSING,
    ERROR
}

data class ProviderConfig(
    val type: ProviderType,
    var apiKey: String = "",
    var isEnabled: Boolean = true,
    var currentStatus: ProviderHealthStatus = ProviderHealthStatus.HEALTHY,
    var statusMessage: String = "Disponível",
    var priorityOrder: Int = 0,
    var successfulRequests: Int = 0,
    var failedRequests: Int = 0
)

data class FallbackAttempt(
    val provider: ProviderType,
    val timestamp: Long = System.currentTimeMillis(),
    val errorReason: String
)

data class AiGenerationResult(
    val textContent: String,
    val imageUrl: String? = null,
    val providerUsed: ProviderType,
    val modelUsed: String,
    val fallbackOccurred: Boolean = false,
    val fallbackHistory: List<FallbackAttempt> = emptyList(),
    val latencyMs: Long = 0
)
