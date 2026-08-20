package com.gitmoji.plugin

import com.intellij.util.xmlb.annotations.Tag

@Tag("template")
data class EmojiTemplate(
    val emoji: String = "",
    val type: String = "",
    val name: String = "",
    val description: String = ""
) {
    companion object {
        val DEFAULTS = listOf(
            EmojiTemplate("✨", "feat", "引入新功能", "新功能"),
            EmojiTemplate("🐛", "fix", "修复bug", "BUG"),
            EmojiTemplate("🚀", "perf", "提高性能/优化", "优化"),
            EmojiTemplate("🎨", "refactor", "改进/重构代码", "优化"),
            EmojiTemplate("🥚", "format", "格式化代码", "格式化"),
            EmojiTemplate("🚑", "patch", "添加重要补丁", "补丁"),
            EmojiTemplate("💄", "style", "更新样式文件", "样式"),
            EmojiTemplate("📚", "docs", "添加/更新文档", "文档"),
            EmojiTemplate("🔧", "chore", "日常维护", "杂项"),
            EmojiTemplate("🧩", "deps", "修改依赖版本", "依赖"),
            EmojiTemplate("🔁", "revert", "还原之前的提交", "回滚"),
            EmojiTemplate("🧪", "test", "增加测试代码", "测试"),
            EmojiTemplate("📦", "file", "添加新文件", "新文件"),
            EmojiTemplate("📌", "tag", "发布版本/添加标签", "书签"),
            EmojiTemplate("🔧", "config", "修改配置文件", "配置"),
            EmojiTemplate("⚙️", "ci", "Action持续集成相关修改", "持续集成"),
            EmojiTemplate("🙈", "git", "添加或修改.gitignore文件", "不可见"),
            EmojiTemplate("🎉", "init", "初次提交/初始化项目", "初始化")
        )
    }
}
