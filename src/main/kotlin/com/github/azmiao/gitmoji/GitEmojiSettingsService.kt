package com.github.azmiao.gitmoji

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.*

@State(
    name = "GitEmojiSettings",
    storages = [Storage("git-emoji-settings.xml")]
)
@Service
class GitEmojiSettingsService : PersistentStateComponent<GitEmojiSettingsService.State> {

    class State {
        var templates: MutableList<EmojiTemplate> = defaultTemplates()
        var formatTemplate: String = DEFAULT_FORMAT

        /** 切换模板时是否保留已输入的正文，只替换前缀。旧配置文件缺该字段时按默认值 true 加载。 */
        var preserveExistingText: Boolean = true
    }

    private var state = State()

    override fun getState(): State = state

    override fun loadState(state: State) {
        this.state = state
    }

    var templates: MutableList<EmojiTemplate>
        get() = state.templates
        set(value) { state.templates = value }

    var formatTemplate: String
        get() = state.formatTemplate
        set(value) { state.formatTemplate = value }

    var preserveExistingText: Boolean
        get() = state.preserveExistingText
        set(value) { state.preserveExistingText = value }

    companion object {
        const val DEFAULT_FORMAT = "\${type}\${emoji}: "

        /** DEFAULTS 是全局常量，必须深拷贝后再交给可变状态，否则会被就地修改。 */
        private fun defaultTemplates(): MutableList<EmojiTemplate> =
            EmojiTemplate.DEFAULTS.mapTo(mutableListOf()) { it.copy() }

        fun getInstance(): GitEmojiSettingsService {
            return ApplicationManager.getApplication().getService(GitEmojiSettingsService::class.java)
        }
    }
}
