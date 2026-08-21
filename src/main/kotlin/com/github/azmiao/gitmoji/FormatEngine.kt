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
}
