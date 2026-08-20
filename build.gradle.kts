plugins {
    java
    kotlin("jvm") version "2.1.0"
    id("org.jetbrains.intellij.platform") version "2.2.1"
}

group = "com.github.azmiao.gitmoji"
version = "1.0.0"

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
        create("IC", "2024.2")
        bundledPlugin("Git4Idea")
    }
}

intellijPlatform {
    pluginConfiguration {
        id = "com.github.azmiao.gitmoji"
        name = "Git Emoji Lint"
        version = "1.0.0"
        ideaVersion {
            sinceBuild = "242"
            untilBuild = provider { null }
        }
        description = "A plugin that adds emoji prefix templates to the Git commit message editor. Provides a toolbar button in the Commit tool window with 18 default commit type templates. Supports custom format templates and template management through a visual settings page."
        vendor {
            name = "GitEmojiLint"
        }
    }

    publishing {
        token = providers.environmentVariable("PUBLISH_TOKEN")
        channels.set(providers.gradleProperty("publishChannels").map { listOf(it) })
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
