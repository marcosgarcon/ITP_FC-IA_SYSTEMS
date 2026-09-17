package com.example

import com.example.data.model.AiModality
import com.example.data.model.ExecutionMode
import com.example.data.model.ProviderType
import com.example.data.model.TaskCategory
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testProviderTypeIncludesGemma3() {
    val providers = ProviderType.values()
    assertTrue(providers.contains(ProviderType.GEMMA_3))
    val gemma = ProviderType.GEMMA_3
    assertEquals("Google Gemma 3", gemma.displayName)
    assertEquals("gemma-3-27b-it", gemma.defaultModel)
    assertFalse(gemma.requiresKey)
  }

  @Test
  fun testExecutionModesCoverAutoRotationAndPinnedModels() {
    val modes = ExecutionMode.values()
    assertTrue(modes.contains(ExecutionMode.AUTO_ROTATION))
    assertTrue(modes.contains(ExecutionMode.PINNED_GEMINI))
    assertTrue(modes.contains(ExecutionMode.PINNED_GEMMA_3))
    assertTrue(modes.contains(ExecutionMode.PINNED_GROQ))
    assertTrue(modes.contains(ExecutionMode.PINNED_OPEN_ROUTER))
    assertTrue(modes.contains(ExecutionMode.PINNED_POLLINATIONS))
  }

  @Test
  fun testTaskCategoriesRequestedByCustomer() {
    val categories = TaskCategory.values()
    val ids = categories.map { it.id }

    // Programming & Dev
    assertTrue(ids.contains("programming"))
    // Text creation
    assertTrue(ids.contains("text_creation"))
    // Spreadsheets and Analysis
    assertTrue(ids.contains("sheet_creator"))
    assertTrue(ids.contains("sheet_analysis"))
    // Executive Dashboard with PDF and Slides
    assertTrue(ids.contains("dashboard_exec"))
    // Document Conversions
    assertTrue(ids.contains("doc_to_excel"))
    assertTrue(ids.contains("pdf_to_excel"))
    assertTrue(ids.contains("excel_to_doc"))
    assertTrue(ids.contains("excel_to_pdf"))
    // Multimedia and translation
    assertTrue(ids.contains("image_creator"))
    assertTrue(ids.contains("translator"))
    assertTrue(ids.contains("video_creator"))

    // Verify sample prompts and prefixes are configured
    categories.forEach { category ->
      assertNotNull(category.title)
      assertNotNull(category.subtitle)
      assertNotNull(category.group)
      assertTrue(category.samplePrompts.isNotEmpty())
    }
  }
}

