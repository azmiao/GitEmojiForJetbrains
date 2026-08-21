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
        description = "A plugin that adds emoji prefix templates to the Git commit message editor. Provides a toolbar button in the Commit tool window with 18 default commit type templates. Supports custom format templates and template management through a visual settings page."
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
 * 从 CHANGELOG.md 中提取当前 pluginVersion 对应的小节，转换为 plugin.xml 所需的 HTML。
 * 找不到对应小节时回退为版本号提示，避免发布流程因缺少 change-notes 而中断。
 */
fun latestChangeNotes(): String {
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
    return html.toString().ifEmpty { fallback }
}

fun escapeHtml(text: String): String = text
    .replace("&", "&amp;")
    .replace("<", "&lt;")
    .replace(">", "&gt;")
