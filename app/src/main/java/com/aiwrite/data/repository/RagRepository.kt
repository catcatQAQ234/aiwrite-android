package com.aiwrite.data.repository

import com.aiwrite.data.local.dao.KnowledgeItemDao
import com.aiwrite.data.local.entity.KnowledgeItemEntity
import com.aiwrite.data.remote.EmbeddingService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RagRepository @Inject constructor(
    private val dao: KnowledgeItemDao,
    private val embeddingService: EmbeddingService
) {
    fun getByNovel(novelId: String): Flow<List<KnowledgeItemEntity>> = dao.getByNovel(novelId)

    fun getBySource(novelId: String, source: String): Flow<List<KnowledgeItemEntity>> =
        dao.getBySource(novelId, source)

    fun search(novelId: String, query: String): Flow<List<KnowledgeItemEntity>> =
        dao.search(novelId, query)

    suspend fun create(novelId: String, title: String, content: String, source: String = "manual"): String {
        val id = UUID.randomUUID().toString()
        dao.insert(KnowledgeItemEntity(id = id, novelId = novelId, title = title, content = content, source = source))
        return id
    }

    suspend fun delete(item: KnowledgeItemEntity) {
        dao.delete(item)
    }

    suspend fun clearNovel(novelId: String) {
        dao.deleteByNovel(novelId)
    }

    /**
     * Import a reference text, splitting into chunks for better retrieval.
     */
    suspend fun importText(novelId: String, title: String, text: String): Int {
        val chunks = chunkText(text, 500)
        var count = 0
        for ((i, chunk) in chunks.withIndex()) {
            val id = UUID.randomUUID().toString()
            dao.insert(
                KnowledgeItemEntity(
                    id = id, novelId = novelId,
                    title = "$title (片段${i + 1})",
                    content = chunk, source = "imported"
                )
            )
            count++
        }
        return count
    }

    /**
     * Semantic search using embeddings.
     */
    suspend fun semanticSearch(novelId: String, query: String, topK: Int = 5): List<KnowledgeItemEntity> {
        val queryEmbedding = embeddingService.embed(query) ?: return emptyList()
        val items = dao.getByNovel(novelId).first()

        // Score items by cosine similarity
        val scored = items.mapNotNull { item ->
            val itemEmbedding = embeddingService.embed(item.content)
            if (itemEmbedding != null) {
                val score = embeddingService.cosineSimilarity(queryEmbedding, itemEmbedding)
                item to score
            } else null
        }

        return scored
            .sortedByDescending { it.second }
            .take(topK)
            .map { it.first }
    }

    /**
     * Build a context string from retrieved knowledge items.
     */
    suspend fun buildContext(novelId: String, query: String, maxItems: Int = 3): String {
        val items = semanticSearch(novelId, query, maxItems)
        if (items.isEmpty()) return ""

        return buildString {
            appendLine("【参考知识】")
            items.forEach { item ->
                appendLine("--- ${item.title} ---")
                appendLine(item.content.take(300))
                appendLine()
            }
        }
    }

    private fun chunkText(text: String, maxChars: Int): List<String> {
        if (text.length <= maxChars) return listOf(text)

        val chunks = mutableListOf<String>()
        var start = 0
        while (start < text.length) {
            var end = (start + maxChars).coerceAtMost(text.length)
            // Try to break at a sentence boundary
            if (end < text.length) {
                val paragraphBreak = text.lastIndexOf("\n\n", end)
                if (paragraphBreak > start + maxChars / 2) {
                    end = paragraphBreak
                } else {
                    val sentenceBreak = text.lastIndexOf('。', end)
                    if (sentenceBreak > start + maxChars / 2) {
                        end = sentenceBreak + 1
                    }
                }
            }
            chunks.add(text.substring(start, end).trim())
            start = end
        }
        return chunks
    }
}
