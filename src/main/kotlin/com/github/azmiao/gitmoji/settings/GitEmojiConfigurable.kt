package com.github.azmiao.gitmoji.settings

import com.github.azmiao.gitmoji.EmojiTemplate
import com.github.azmiao.gitmoji.GitEmojiSettingsService
import com.intellij.openapi.options.Configurable
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.table.TableView
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import javax.swing.*

class GitEmojiConfigurable : Configurable {

    private val settings get() = GitEmojiSettingsService.getInstance()
    private var panel: JPanel? = null
    private var formatField: JTextField? = null
    private var tableModel: EmojiTemplateTableModel? = null
    private var table: TableView<EmojiTemplate>? = null

    override fun getDisplayName() = "Git Emoji Lint"

    override fun createComponent(): JComponent {
        val p = JPanel(BorderLayout(0, JBUI.scale(10)))
        p.border = JBUI.Borders.empty(10)

        // 格式模板区域
        val formatPanel = JPanel(BorderLayout(JBUI.scale(5), 0))
        formatPanel.add(JLabel("格式模板:"), BorderLayout.WEST)
        formatField = JTextField(settings.formatTemplate).apply {
            toolTipText = "支持占位符: \${emoji} \${type} \${name} \${description}"
        }
        formatPanel.add(formatField, BorderLayout.CENTER)
        p.add(formatPanel, BorderLayout.NORTH)

        // 模板列表表格：编辑副本，避免直接改动已保存的模板对象
        val model = EmojiTemplateTableModel()
        tableModel = model
        table = TableView(model)
        model.items = copyOf(settings.templates)

        val decorator = ToolbarDecorator.createDecorator(table!!)
            .setAddAction {
                model.addRow(EmojiTemplate())
                // 让新增的空行可见并高亮，方便立即编辑
                val newIndex = model.rowCount - 1
                table!!.setRowSelectionInterval(newIndex, newIndex)
                table!!.scrollRectToVisible(table!!.getCellRect(newIndex, 0, true))
            }
            .setRemoveAction {
                table!!.selectedRows.sortedDescending().forEach { model.removeRow(it) }
            }
            .disableUpDownActions()

        p.add(decorator.createPanel(), BorderLayout.CENTER)

        // 恢复默认按钮
        val resetButton = JButton("恢复默认").apply {
            addActionListener {
                model.items = copyOf(EmojiTemplate.DEFAULTS)
                formatField!!.text = GitEmojiSettingsService.DEFAULT_FORMAT
            }
        }
        val southPanel = JPanel(java.awt.FlowLayout(java.awt.FlowLayout.LEFT))
        southPanel.add(resetButton)
        p.add(southPanel, BorderLayout.SOUTH)

        panel = p
        return p
    }

    override fun isModified(): Boolean {
        val currentFormat = formatField?.text ?: return false
        val currentTemplates = tableModel?.items ?: return false
        return currentFormat != settings.formatTemplate ||
               currentTemplates != settings.templates
    }

    override fun apply() {
        settings.formatTemplate = formatField?.text ?: GitEmojiSettingsService.DEFAULT_FORMAT
        settings.templates = copyOf(tableModel?.items ?: emptyList())
    }

    override fun reset() {
        formatField?.text = settings.formatTemplate
        tableModel?.items = copyOf(settings.templates)
    }

    override fun disposeUIResources() {
        panel = null
        formatField = null
        tableModel = null
        table = null
    }

    private fun copyOf(templates: List<EmojiTemplate>): MutableList<EmojiTemplate> =
        templates.mapTo(mutableListOf()) { it.copy() }
}
