package com.example.util

import android.content.Context
import android.content.Intent
import com.example.data.model.CodeItem
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CodeExportHelper {

    fun exportToCsv(items: List<CodeItem>): String {
        val sb = StringBuilder()
        sb.append("ID,Kind,Type,Format,Title,Category,Preview,Content,Notes,Favorite,Created,Updated\n")
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)

        for (item in items) {
            val created = sdf.format(Date(item.createdAt))
            val updated = sdf.format(Date(item.updatedAt))
            sb.append(escapeCsv(item.id.toString())).append(",")
            sb.append(escapeCsv(item.kind.name)).append(",")
            sb.append(escapeCsv(item.codeType.name)).append(",")
            sb.append(escapeCsv(item.format)).append(",")
            sb.append(escapeCsv(item.title)).append(",")
            sb.append(escapeCsv(item.category)).append(",")
            sb.append(escapeCsv(item.preview)).append(",")
            sb.append(escapeCsv(item.content)).append(",")
            sb.append(escapeCsv(item.notes ?: "")).append(",")
            sb.append(if (item.isFavorite) "TRUE" else "FALSE").append(",")
            sb.append(escapeCsv(created)).append(",")
            sb.append(escapeCsv(updated)).append("\n")
        }
        return sb.toString()
    }

    fun exportToJson(items: List<CodeItem>): String {
        val array = JSONArray()
        for (item in items) {
            val obj = JSONObject()
            obj.put("id", item.id)
            obj.put("kind", item.kind.name.lowercase(Locale.US))
            obj.put("type", item.codeType.name)
            obj.put("format", item.format)
            obj.put("title", item.title)
            obj.put("category", item.category)
            obj.put("preview", item.preview)
            obj.put("rawValue", item.content)
            obj.put("notes", item.notes ?: "")
            obj.put("favorite", item.isFavorite)
            obj.put("createdAt", item.createdAt)
            obj.put("updatedAt", item.updatedAt)
            array.put(obj)
        }
        return array.toString(2)
    }

    fun exportToFormattedText(items: List<CodeItem>): String {
        val sb = StringBuilder()
        val sdf = SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault())
        sb.append("Qobley Codes Export (${items.size} items)\n")
        sb.append("Generated on ${sdf.format(Date())}\n\n")

        items.forEachIndexed { index, item ->
            sb.append("${index + 1}. [${item.displayTypeLabel}] ${item.title}\n")
            sb.append("   Preview: ${item.preview}\n")
            sb.append("   Format: ${item.format.replace("_", " ")}\n")
            sb.append("   Category: ${item.category}\n")
            sb.append("   Payload: ${item.content}\n")
            if (!item.notes.isNullOrBlank()) {
                sb.append("   Notes: ${item.notes}\n")
            }
            sb.append("   Date: ${sdf.format(Date(item.createdAt))}\n")
            if (item.isFavorite) {
                sb.append("   Starred: Yes ★\n")
            } else {
                sb.append("   Starred: No\n")
            }
            sb.append("\n")
        }
        return sb.toString()
    }

    fun shareExport(context: Context, content: String, subject: String, mimeType: String = "text/plain") {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, content)
        }
        val chooser = Intent.createChooser(sendIntent, subject)
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }

    private fun escapeCsv(str: String): String {
        val escaped = str.replace("\"", "\"\"")
        return if (escaped.contains(",") || escaped.contains("\"") || escaped.contains("\n") || escaped.contains("\r")) {
            "\"$escaped\""
        } else {
            escaped
        }
    }
}
