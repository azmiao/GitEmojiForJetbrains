package com.github.azmiao.gitmoji

object FormatEngine {

    private val PLACEHOLDER_PATTERN = Regex("""\$\{(\w+)}""")

    fun format(formatTemplate: String, emojiTemplate: EmojiTemplate): String {
        return PLACEHOLDER_PATTERN.replace(formatTemplate) { match ->
            when (match.groupValues[1]) {
                "emoji" -> emojiTemplate.emoji
                "type" -> emojiTemplate.type
                "name" -> emojiTemplate.name
                "description" -> emojiTemplate.description
                else -> match.value
            }
        }
    }

    /**
     * 剥离文本开头的 commit 前缀，返回剩余正文。
     *
     * 候选前缀由当前格式模板实时渲染所有模板得到，因此模板增删改、格式调整都能立即生效。
     * 按长度降序匹配，避免 `feat` 把 `feature` 的前缀截掉一半。
     * 一个都匹配不上时原样返回，由调用方决定如何处理（当前策略是直接前置拼接）。
     */
    fun stripKnownPrefix(
        text: String,
        formatTemplate: String,
        templates: List<EmojiTemplate>
    ): String {
        if (text.isEmpty()) return text
        val matched = templates
            .map { format(formatTemplate, it) }
            .filter { it.isNotEmpty() }
            .sortedByDescending { it.length }
            .firstOrNull { text.startsWith(it) }
            ?: return text
        return text.removePrefix(matched)
    }
}
