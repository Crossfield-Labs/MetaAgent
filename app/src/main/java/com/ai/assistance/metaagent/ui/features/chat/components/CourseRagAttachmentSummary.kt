package com.ai.assistance.metaagent.ui.features.chat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ai.assistance.metaagent.data.model.AttachmentInfo

private const val COURSE_RAG_CONTEXT_FILE_NAME = "course_rag_context.txt"
private val COURSE_RAG_TITLE_PATTERN = Regex("^\\[(\\d+)]\\s+(.+)$")

private data class CourseRagHitEntry(
    val title: String,
    val snippet: String
)

private data class CourseRagPreview(
    val question: String?,
    val entries: List<CourseRagHitEntry>
)

private fun parseCourseRagPreview(content: String): CourseRagPreview? {
    if (content.isBlank()) return null
    val lines = content
        .lineSequence()
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .toList()
    if (lines.isEmpty()) return null

    val question = lines
        .firstOrNull { it.startsWith("User question:", ignoreCase = true) }
        ?.substringAfter(':')
        ?.trim()
        ?.takeIf { it.isNotBlank() }

    val entries = mutableListOf<CourseRagHitEntry>()
    var currentTitle: String? = null
    val snippetBuffer = mutableListOf<String>()

    fun flushCurrent() {
        val title = currentTitle?.takeIf { it.isNotBlank() } ?: return
        val snippet = snippetBuffer
            .joinToString(" ")
            .replace(Regex("\\s+"), " ")
            .trim()
            .take(140)
        entries += CourseRagHitEntry(title = title, snippet = snippet)
        snippetBuffer.clear()
    }

    lines.forEach { line ->
        val match = COURSE_RAG_TITLE_PATTERN.matchEntire(line)
        if (match != null) {
            flushCurrent()
            currentTitle = match.groupValues.getOrNull(2)?.trim()
        } else if (currentTitle != null) {
            snippetBuffer += line
        }
    }
    flushCurrent()

    val dedupEntries = entries
        .groupBy { it.title }
        .map { (_, value) -> value.first() }

    if (question == null && dedupEntries.isEmpty()) return null
    return CourseRagPreview(question = question, entries = dedupEntries)
}

@Composable
fun CourseRagAttachmentSummary(
    attachments: List<AttachmentInfo>,
    modifier: Modifier = Modifier
) {
    val ragAttachment = remember(attachments) {
        attachments.lastOrNull { it.fileName.equals(COURSE_RAG_CONTEXT_FILE_NAME, ignoreCase = true) }
    } ?: return

    val preview = remember(ragAttachment.filePath, ragAttachment.content) {
        parseCourseRagPreview(ragAttachment.content)
    } ?: return

    var expanded by remember(ragAttachment.filePath) { mutableStateOf(false) }

    val hitCount = preview.entries.size
    val titlePreview = when {
        preview.entries.isEmpty() -> "No source"
        preview.entries.size <= 2 -> preview.entries.joinToString(" / ") { it.title }
        else -> "${preview.entries.take(2).joinToString(" / ") { it.title }} +${preview.entries.size - 2}"
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .clickable { expanded = !expanded }
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                shape = MaterialTheme.shapes.medium
            )
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Description,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = "Course KB hits: $hitCount",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 6.dp)
            )
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
        }

        preview.question?.let { question ->
            Text(
                text = "Question: $question",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Text(
            text = "Sources: $titlePreview",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 2.dp)
        )

        if (expanded && preview.entries.isNotEmpty()) {
            Spacer(modifier = Modifier.height(6.dp))
            preview.entries.take(3).forEachIndexed { index, entry ->
                Text(
                    text = "${index + 1}. ${entry.title}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (entry.snippet.isNotBlank()) {
                    Text(
                        text = entry.snippet,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 1.dp, bottom = 3.dp)
                    )
                }
            }
        }
    }
}