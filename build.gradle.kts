plugins {
    java
    kotlin("jvm") version "2.1.0"
    id("org.jetbrains.intellij.platform") version "2.2.1"
}

group = "com.gitmoji.plugin"
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
        id = "com.gitmoji.plugin"
        name = "GitEmojiForJetbrains"
        version = "1.0.0"
        ideaVersion {
            sinceBuild = "242"
            untilBuild = provider { null }
        }
        description = "Git Commit Emoji Prefix Plugin"
        vendor {
            name = "GitEmoji"
        }
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
