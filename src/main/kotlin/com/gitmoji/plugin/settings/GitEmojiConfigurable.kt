package com.gitmoji.plugin.settings

import com.gitmoji.plugin.EmojiTemplate
import com.gitmoji.plugin.GitEmojiSettingsService
import com.intellij.openapi.options.Configurable
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.table.TableView
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import javax.swing.*

class GitEmojiConfigurable : Configurable {

    private val settings = GitEmojiSettingsService.getInstance()
    private var panel: JPanel? = null
    private var formatField: JTextField? = null
    private var tableModel: EmojiTemplateTableModel? = null
    private var table: TableView<EmojiTemplate>? = null

    override fun getDisplayName() = "Git Emoji Commit"

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

        // 模板列表表格
        tableModel = EmojiTemplateTableModel()
        table = TableView(tableModel)
        tableModel!!.items = settings.templates.toMutableList()

        val decorator = ToolbarDecorator.createDecorator(table!!)
            .setAddAction {
                tableModel!!.addRow(EmojiTemplate())
            }
            .setRemoveAction {
                val selected = table!!.selectedRows
                selected.sortedDescending().forEach { tableModel!!.removeRow(it) }
            }
            .disableUpDownActions()

        p.add(decorator.createPanel(), BorderLayout.CENTER)

        // 恢复默认按钮
        val resetButton = JButton("恢复默认").apply {
            addActionListener {
                tableModel!!.items = EmojiTemplate.DEFAULTS.toMutableList()
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
        settings.templates = tableModel?.items?.toMutableList() ?: mutableListOf()
    }

    override fun reset() {
        formatField?.text = settings.formatTemplate
        tableModel?.items = settings.templates.toMutableList()
    }

    override fun disposeUIResources() {
        panel = null
        formatField = null
        tableModel = null
        table = null
    }
}
