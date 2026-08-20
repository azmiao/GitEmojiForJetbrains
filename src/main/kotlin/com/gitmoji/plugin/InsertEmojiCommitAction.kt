package com.gitmoji.plugin

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.vcs.VcsDataKeys
import com.intellij.ui.ColoredListCellRenderer
import com.intellij.ui.SimpleTextAttributes
import com.intellij.ui.awt.RelativePoint
import com.intellij.ui.components.JBList
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
            NotificationGroupManager.getInstance()
                .getNotificationGroup("GitEmoji")
                .createNotification(
                    "暂无模板，请在 设置 → Tools → Git Emoji Commit 中添加",
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

        val popup = JBPopupFactory.getInstance()
            .createListPopupBuilder(list)
            .setTitle("选择 Commit 模板")
            .setItemChoosenCallback {
                val selected = list.selectedValue ?: return@setItemChoosenCallback
                applyTemplate(selected, e)
            }
            .createPopup()

        val component = e.inputEvent?.component ?: return
        popup.show(RelativePoint.getSouthWestOf(component as javax.swing.JComponent))
    }

    private fun applyTemplate(template: EmojiTemplate, e: AnActionEvent) {
        val workflow = e.getData(VcsDataKeys.COMMIT_WORKFLOW_UI) ?: return
        val settings = GitEmojiSettingsService.getInstance()
        val format = settings.formatTemplate.ifBlank { GitEmojiSettingsService.DEFAULT_FORMAT }
        val prefix = FormatEngine.format(format, template)
        workflow.commitMessageUi.setText(prefix)
        workflow.commitMessageUi.focus()
    }
}
