plugins {
    java
    kotlin("jvm") version "2.1.0"
    id("org.jetbrains.intellij.platform") version "2.2.1"
}

group = providers.gradleProperty("pluginGroup").get()
version = providers.gradleProperty("pluginVersion").get()

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

// 配置 JDK 21 路径
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

dependencies {
    intellijPlatform {
        create(
            providers.gradleProperty("platformType"),
            providers.gradleProperty("platformVersion")
        )
        bundledPlugin("Git4Idea")
    }
}

intellijPlatform {
    pluginConfiguration {
        id = providers.gradleProperty("pluginGroup")
        name = "Git Emoji Lint"
        version = providers.gradleProperty("pluginVersion")
        ideaVersion {
            sinceBuild = providers.gradleProperty("pluginSinceBuild")
            untilBuild = providers.gradleProperty("pluginUntilBuild")
                .map { it.trim() }
                .filter { it.isNotEmpty() }
        }
        description = provider { pluginDescription() }
        changeNotes = provider { latestChangeNotes() }
        vendor {
            name = "GitEmojiLint"
        }
    }

    publishing {
        token = providers.environmentVariable("PUBLISH_TOKEN")
        channels = providers.gradleProperty("publishChannels")
            .orElse("default")
            .map { listOf(it) }
    }
}

kotlin {
    jvmToolchain(21)
}

tasks {
    compileKotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
        }
    }
    compileJava {
        sourceCompatibility = "21"
        targetCompatibility = "21"
    }
}

/**
 * 插件描述（英文）：Marketplace 校验要求描述以拉丁字符开头，且部分版本对非拉丁内容审核更严格，
 * 因此统一使用英文。通过 build.gradle.kts 注入，覆盖 plugin.xml 中的 CDATA。
 */
fun pluginDescription(): String = """
Git Emoji Lint adds emoji prefix templates to the Git commit message editor. Pick a template from the Commit tool window toolbar to generate a `feat✨:` style prefix, making your commit history more readable and consistent.

## 📚 中文说明

在 Commit 工具窗口提供工具栏按钮，选择模板后自动生成 `feat✨: ` 格式的 commit 前缀，让提交记录更直观、更规范。

## ✨ 功能特性

- **Commit 工具窗口工具栏按钮**：在提交信息编辑区工具栏中显示 emoji 按钮，点击即弹出模板列表。
- **模板选择弹窗**：列表展示 `emoji + type + name`（如 `✨ feat - 引入新功能`），上下键选择、回车确认，操作流畅。
- **一键覆盖输入框**：选中模板后自动按格式模板生成前缀并覆盖 commit message 输入框内容，光标自动移到末尾，可直接继续输入正文。
- **可视化设置页面**：`Settings → Tools → Git Emoji Lint`
  - **自定义格式模板**：支持 `${'$'}{emoji}`、`${'$'}{type}`、`${'$'}{name}`、`${'$'}{description}` 四种占位符，自由组合出你想要的任何前缀格式。
  - **模板列表增删改**：表格中 emoji、type、name、description 四列均可直接编辑，支持添加新模板、删除不用的模板。

## 📋 默认模板

<div align="center">

| emoji | type | name | description |
|:-----:|:----:|------|-------------|
| ✨ | feat | 引入新功能 | 新功能 |
| 🐛 | fix | 修复bug | BUG |
| 🚀 | perf | 提高性能/优化 | 优化 |
| 🎨 | refactor | 改进/重构代码 | 优化 |
| 🥚 | format | 格式化代码 | 格式化 |
| 🚑 | patch | 添加重要补丁 | 补丁 |
| 💄 | style | 更新样式文件 | 样式 |
| 📚 | docs | 添加/更新文档 | 文档 |
| 🔧 | chore | 日常维护 | 杂项 |
| 🧩 | deps | 修改依赖版本 | 依赖 |
| 🔁 | revert | 还原之前的提交 | 回滚 |
| 🧪 | test | 增加测试代码 | 测试 |
| 📦 | file | 添加新文件 | 新文件 |
| 📌 | tag | 发布版本/添加标签 | 书签 |
| 🔧 | config | 修改配置文件 | 配置 |
| ⚙️ | ci | Action持续集成相关修改 | 持续集成 |
| 🙈 | git | 添加或修改.gitignore文件 | 不可见 |
| 🎉 | init | 初次提交/初始化项目 | 初始化 |

</div>

默认格式模板为 `${'$'}{type}${'$'}{emoji}: `，即生成 `feat✨: ` 样式的前缀。所有模板及格式均可在设置页面自由修改。
""".trimIndent()

/**
 * 从 CHANGELOG.md 中提取当前 pluginVersion 对应的小节，转换为 plugin.xml 所需的 HTML。
 * 找不到对应小节时回退为版本号提示，避免发布流程因缺少 change-notes 而中断。
 */
fun latestChangeNotes(): String {
    // 发布流程（publish.yml）会通过 -PreleaseNotesFile 传入由 generate_changelog.py 生成的
    // 当版本发布说明（markdown），优先使用它；否则回退到仓库里已有的 CHANGELOG.md。
    providers.gradleProperty("releaseNotesFile")
        .orNull
        ?.takeIf { it.isNotBlank() }
        ?.let { layout.projectDirectory.file(it).asFile }
        ?.takeIf { it.isFile }
        ?.let { return markdownSectionToHtml(it.readLines()) }

    val pluginVersion = providers.gradleProperty("pluginVersion").get().trim()
    val changelog = layout.projectDirectory.file("CHANGELOG.md").asFile
    val fallback = "<ul><li>$pluginVersion</li></ul>"
    if (!changelog.isFile) return fallback

    // CHANGELOG 小节形如：## 1.0.0 (2025-01-01)
    val sectionHeader = Regex("""^##\s+\Q$pluginVersion\E(\s|$)""")
    val lines = changelog.readLines()
    val start = lines.indexOfFirst { sectionHeader.containsMatchIn(it) }
    if (start < 0) return fallback
    val rest = lines.drop(start + 1)
    val end = rest.indexOfFirst { it.startsWith("## ") }
    val body = if (end < 0) rest else rest.take(end)

    return markdownSectionToHtml(body).ifEmpty { fallback }
}

/** 把 changelog 小节（### 分类 + - 列表 + 段落）转成 plugin.xml 允许的 HTML 子集。 */
fun markdownSectionToHtml(body: List<String>): String {
    val html = StringBuilder()
    var listOpen = false
    fun closeList() {
        if (listOpen) {
            html.append("</ul>")
            listOpen = false
        }
    }
    for (raw in body) {
        val line = raw.trim()
        when {
            line.isEmpty() -> Unit
            line.startsWith("## ") -> {
                closeList()
                html.append("<h3>").append(escapeHtml(line.removePrefix("## ").trim())).append("</h3>")
            }
            line.startsWith("### ") -> {
                closeList()
                html.append("<h4>").append(escapeHtml(line.removePrefix("### ").trim())).append("</h4>")
            }
            line.startsWith("- ") -> {
                if (!listOpen) {
                    html.append("<ul>")
                    listOpen = true
                }
                html.append("<li>").append(escapeHtml(line.removePrefix("- ").trim())).append("</li>")
            }
            else -> {
                closeList()
                html.append("<p>").append(escapeHtml(line)).append("</p>")
            }
        }
    }
    closeList()
    return html.toString()
}

fun escapeHtml(text: String): String = text
    .replace("&", "&amp;")
    .replace("<", "&lt;")
    .replace(">", "&gt;")
