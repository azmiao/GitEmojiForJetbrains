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
    <p><b>Git Emoji Lint</b> adds emoji prefix templates to the Git commit message editor. Pick a template from the Commit tool window toolbar to generate a <code>feat✨: </code> style prefix, making your commit history more readable and consistent.</p>

    <h3>✨ Features</h3>
    <ul>
        <li><b>Toolbar button in Commit tool window</b>: Click the emoji button in the commit message editor toolbar to pop up the template list.</li>
        <li><b>Template picker popup</b>: Displays <code>emoji + type + name</code> (e.g. <code>✨ feat - New feature</code>), navigate with arrow keys and confirm with Enter.</li>
        <li><b>One-click overwrite</b>: Selecting a template generates the prefix and overwrites the commit message input, cursor moves to the end so you can keep typing immediately.</li>
        <li><b>Visual settings page</b>: Settings → Tools → Git Emoji Lint. Customize the format template with <code>${'$'}{emoji}</code>, <code>${'$'}{type}</code>, <code>${'$'}{name}</code>, <code>${'$'}{description}</code> placeholders, and manage the template list.</li>
    </ul>

    <h3>📋 Default Templates</h3>
    <p>Built-in 18 common commit type templates: feat, fix, perf, refactor, format, patch, style, docs, chore, deps, revert, test, file, tag, config, ci, git, init. Default format: <code>${'$'}{type}${'$'}{emoji}: </code>, producing prefixes like <code>feat✨: </code>.</p>

    <h3>🛠 Tech Stack</h3>
    <ul>
        <li>Kotlin + Gradle Kotlin DSL</li>
        <li>IntelliJ Platform Gradle Plugin 2.x</li>
        <li>JDK 21</li>
        <li>Compatible with IntelliJ IDEA 2024.2+</li>
    </ul>
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
