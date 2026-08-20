package com.gitmoji.plugin.settings

import com.gitmoji.plugin.EmojiTemplate
import com.intellij.util.ui.ColumnInfo
import com.intellij.util.ui.ListTableModel

class EmojiTemplateTableModel : ListTableModel<EmojiTemplate>(
    arrayOf(
        object : ColumnInfo<EmojiTemplate, String>("Emoji") {
            override fun valueOf(item: EmojiTemplate) = item.emoji
            override fun isCellEditable(item: EmojiTemplate) = true
        },
        object : ColumnInfo<EmojiTemplate, String>("Type") {
            override fun valueOf(item: EmojiTemplate) = item.type
            override fun isCellEditable(item: EmojiTemplate) = true
        },
        object : ColumnInfo<EmojiTemplate, String>("Name") {
            override fun valueOf(item: EmojiTemplate) = item.name
            override fun isCellEditable(item: EmojiTemplate) = true
        },
        object : ColumnInfo<EmojiTemplate, String>("Description") {
            override fun valueOf(item: EmojiTemplate) = item.description
            override fun isCellEditable(item: EmojiTemplate) = true
        }
    ),
    mutableListOf()
) {
    override fun setValueAt(aValue: Any?, rowIndex: Int, columnIndex: Int) {
        val old = items[rowIndex]
        val new = when (columnIndex) {
            0 -> old.copy(emoji = aValue as? String ?: "")
            1 -> old.copy(type = aValue as? String ?: "")
            2 -> old.copy(name = aValue as? String ?: "")
            3 -> old.copy(description = aValue as? String ?: "")
            else -> old
        }
        items[rowIndex] = new
        fireTableCellUpdated(rowIndex, columnIndex)
    }
}
