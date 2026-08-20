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
        var templates: MutableList<EmojiTemplate> = EmojiTemplate.DEFAULTS.toMutableList()
        var formatTemplate: String = DEFAULT_FORMAT
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

    fun resetToDefaults() {
        state.templates = EmojiTemplate.DEFAULTS.toMutableList()
        state.formatTemplate = DEFAULT_FORMAT
    }

    companion object {
        const val DEFAULT_FORMAT = "\${type} \${emoji}: "

        fun getInstance(): GitEmojiSettingsService {
            return ApplicationManager.getApplication().getService(GitEmojiSettingsService::class.java)
        }
    }
}
