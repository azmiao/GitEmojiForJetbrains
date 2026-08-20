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
        val item = items[rowIndex]
        val value = aValue as? String ?: ""
        when (columnIndex) {
            0 -> item.emoji = value
            1 -> item.type = value
            2 -> item.name = value
            3 -> item.description = value
        }
        fireTableCellUpdated(rowIndex, columnIndex)
    }
}
