package com.github.azmiao.gitmoji

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.ui.popup.PopupChooserBuilder
import com.intellij.openapi.vcs.VcsDataKeys
import com.intellij.openapi.wm.IdeFocusManager
import com.intellij.ui.EditorTextField
import com.intellij.ui.ColoredListCellRenderer
import com.intellij.ui.SimpleTextAttributes
import com.intellij.ui.awt.RelativePoint
import com.intellij.ui.components.JBList
import javax.swing.JComponent
import javax.swing.JList

class InsertEmojiCommitAction : AnAction(), DumbAware {

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent) {
        val workflow = e.getData(VcsDataKeys.COMMIT_WORKFLOW_UI)
        e.presentation.isEnabledAndVisible = workflow != null
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val settings = GitEmojiSettingsService.getInstance()
        val templates = settings.templates

        if (templates.isEmpty()) {
            @Suppress("DialogTitleCapitalization")
            NotificationGroupManager.getInstance()
                .getNotificationGroup("GitEmoji")
                .createNotification(
                    "Git Emoji Lint",
                    "暂无模板，请在 设置 → Tools → Git Emoji Lint 中添加",
                    NotificationType.INFORMATION
                )
                .notify(project)
            return
        }

        val list = JBList(templates).apply {
            cellRenderer = object : ColoredListCellRenderer<EmojiTemplate>() {
                override fun customizeCellRenderer(
                    list: JList<out EmojiTemplate>,
                    value: EmojiTemplate,
                    index: Int,
                    selected: Boolean,
                    hasFocus: Boolean
                ) {
                    append("${value.emoji} ${value.type}", SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES)
                    append(" - ${value.name}", SimpleTextAttributes.REGULAR_ATTRIBUTES)
                }
            }
        }

        val popup = PopupChooserBuilder(list)
            .setTitle("选择 Commit 模板")
            .setItemChosenCallback { selected ->
                applyTemplate(selected, e)
            }
            .createPopup()

        // 工具栏点击时贴着按钮弹出；快捷键触发时没有 inputEvent，回退到焦点区域居中显示
        val component = e.inputEvent?.component as? JComponent
        if (component != null && component.isShowing) {
            popup.show(RelativePoint.getSouthWestOf(component))
        } else {
            popup.showInFocusCenter()
        }
    }

    private fun applyTemplate(template: EmojiTemplate, e: AnActionEvent) {
        val workflow = e.getData(VcsDataKeys.COMMIT_WORKFLOW_UI) ?: return
        val settings = GitEmojiSettingsService.getInstance()
        val format = settings.formatTemplate.ifBlank { GitEmojiSettingsService.DEFAULT_FORMAT }
        val prefix = FormatEngine.format(format, template)
        val ui = workflow.commitMessageUi

        val editorField = (ui as? com.intellij.openapi.vcs.ui.CommitMessage)?.editorField
            ?: (ui as? EditorTextField)
        val editor = editorField?.editor

        val oldText = ui.text.orEmpty()

        // 记录改动前的光标与选区，用于按偏移量平移，避免硬跳到前缀末尾打断输入。
        val caretOffset = editor?.caretModel?.offset ?: oldText.length
        val selectionStart = editor?.selectionModel?.selectionStart ?: caretOffset
        val selectionEnd = editor?.selectionModel?.selectionEnd ?: caretOffset
        val hadSelection = selectionStart != selectionEnd

        // 关闭开关时整体覆盖，光标固定落在前缀末尾，与旧行为保持一致。
        // 开启时剥离识别得出的旧前缀，只保留正文，并把光标按偏移量平移。
        val newText: String
        val caretTarget: Int
        val selectionTarget: Pair<Int, Int>?
        if (settings.preserveExistingText) {
            val body = FormatEngine.stripKnownPrefix(oldText, format, settings.templates)
            val strippedLength = oldText.length - body.length
            newText = prefix + body

            // 旧前缀区间内的偏移统一落到新前缀末尾，避免光标停在半个前缀中间；
            // 正文内的偏移按长度差平移。offset 最大为 oldText.length，结果天然落在 [prefix.length, newText.length]。
            fun translate(offset: Int): Int =
                if (offset <= strippedLength) prefix.length
                else prefix.length + offset - strippedLength

            val start = translate(selectionStart)
            val end = translate(selectionEnd)
            caretTarget = translate(caretOffset)
            selectionTarget = if (hadSelection && start < end) start to end else null
        } else {
            newText = prefix
            caretTarget = prefix.length
            selectionTarget = null
        }

        ui.setText(newText)

        // 不调用 ui.focus()，因为它内部会同步调用 selectAll()。
        // 直接请求焦点到编辑器内部组件，然后手动设置光标位置。
        editorField?.let { field ->
            IdeFocusManager.getGlobalInstance().requestFocus(field.focusTarget, true)
            field.editor?.let { target ->
                val selection = selectionTarget
                if (selection != null) {
                    target.selectionModel.setSelection(selection.first, selection.second)
                } else {
                    target.selectionModel.removeSelection()
                    target.caretModel.moveToOffset(caretTarget)
                }
            }
        }
    }
}
