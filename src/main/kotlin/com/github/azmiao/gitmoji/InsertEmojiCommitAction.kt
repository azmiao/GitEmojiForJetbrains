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
        ui.setText(prefix)

        // 不调用 ui.focus()，因为它内部会同步调用 selectAll()。
        // 直接请求焦点到编辑器内部组件，然后手动设置光标位置。
        val editorField = (ui as? com.intellij.openapi.vcs.ui.CommitMessage)?.editorField
            ?: (ui as? EditorTextField)
        editorField?.let { field ->
            IdeFocusManager.getGlobalInstance().requestFocus(field.focusTarget, true)
            field.editor?.let { editor ->
                editor.selectionModel.removeSelection()
                editor.caretModel.moveToOffset(prefix.length)
            }
        }
    }
}
