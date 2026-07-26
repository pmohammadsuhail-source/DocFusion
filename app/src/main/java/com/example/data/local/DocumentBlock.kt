package com.example.data.local

import androidx.room.TypeConverter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

enum class BlockType {
    HEADING1,
    HEADING2,
    PARAGRAPH,
    IMAGE,
    BULLET_LIST,
    NUMBERED_LIST,
    QUOTE
}

data class DocumentBlock(
    val id: String = java.util.UUID.randomUUID().toString(),
    var type: BlockType = BlockType.PARAGRAPH,
    var text: String = "",
    var imageUrl: String? = null,
    var imageCaption: String? = null,
    var isBold: Boolean = false,
    var isItalic: Boolean = false,
    var isUnderline: Boolean = false,
    var fontSizeSp: Int = 16,
    var fontColorHex: String = "#0F172A",
    var alignment: String = "LEFT" // "LEFT", "CENTER", "RIGHT", "JUSTIFY"
)

class BlockListConverter {
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val type = Types.newParameterizedType(List::class.java, DocumentBlock::class.java)
    private val adapter = moshi.adapter<List<DocumentBlock>>(type)

    @TypeConverter
    fun fromBlockList(blocks: List<DocumentBlock>?): String {
        if (blocks == null) return "[]"
        return adapter.toJson(blocks)
    }

    @TypeConverter
    fun toBlockList(json: String?): List<DocumentBlock> {
        if (json.isNullOrEmpty()) return emptyList()
        return try {
            adapter.fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}
