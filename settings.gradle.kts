plugins {
    // 允许 Gradle 在本机 JDK 不满足 toolchain 要求时自动下载对应版本，
    // 避免各环境依赖机器相关的 org.gradle.java.home 配置。
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "GitEmojiLint"
