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
 * 插件描述：与 plugin.xml / README 保持一致的完整中文说明。
 * 通过 build.gradle.kts 注入，确保 Marketplace 页面展示的是这段而不是 plugin.xml 里的 CDATA。
 */
fun pluginDescription(): String = """
    <h2>Git Emoji Lint 插件</h2>
    <p>在 Commit 工具窗口提供工具栏按钮，选择模板后自动生成 <code>feat✨: </code> 格式的 commit 前缀，让提交记录更直观、更规范。</p>

    <h3>✨ 功能特性</h3>
    <ul>
        <li><b>Commit 工具窗口工具栏按钮</b>：在提交信息编辑区工具栏中显示 emoji 按钮，点击即弹出模板列表。</li>
        <li><b>模板选择弹窗</b>：列表展示 <code>emoji + type + name</code>（如 <code>✨ feat - 引入新功能</code>），上下键选择、回车确认，操作流畅。</li>
        <li><b>一键覆盖输入框</b>：选中模板后自动生成前缀并覆盖 commit message 输入框，光标自动移到末尾，可直接继续输入正文。</li>
        <li><b>可视化设置页面</b>：Settings → Tools → Git Emoji Lint，支持自定义格式模板（<code>${'$'}{emoji}</code>、<code>${'$'}{type}</code>、<code>${'$'}{name}</code>、<code>${'$'}{description}</code> 四种占位符）和模板列表增删改。</li>
    </ul>

    <h3>📋 默认模板</h3>
    <p>内置 18 种常用提交类型模板：feat、fix、perf、refactor、format、patch、style、docs、chore、deps、revert、test、file、tag、config、ci、git、init。默认格式为 <code>${'$'}{type}${'$'}{emoji}: </code>，即生成 <code>feat✨: </code> 样式的前缀。</p>

    <h3>🛠 技术栈</h3>
    <ul>
        <li>Kotlin + Gradle Kotlin DSL</li>
        <li>IntelliJ Platform Gradle Plugin 2.x</li>
        <li>JDK 21</li>
        <li>兼容 IntelliJ IDEA 2024.2 及以上</li>
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
