<div align="center">

# Git Emoji Lint 插件

在 Commit 工具窗口提供工具栏按钮，选择模板后自动生成 `feat ✨: ` 格式的 commit 前缀，让提交记录更直观、更规范。

<img src="preview.gif" alt="插件使用演示" width="720"/>

<br/>
<br/>

![Platform](https://img.shields.io/badge/platform-IntelliJ%20IDEA%202024.2%2B-blue)
![Language](https://img.shields.io/badge/language-Kotlin-purple)
![JDK](https://img.shields.io/badge/JDK-21-orange)
![License](https://img.shields.io/badge/license-MIT-green)

</div>

---

## ✨ 功能特性

- **Commit 工具窗口工具栏按钮**：在提交信息编辑区工具栏中显示 emoji 按钮，点击即弹出模板列表。
- **模板选择弹窗**：列表展示 `emoji + type + name`（如 `✨ feat - 引入新功能`），上下键选择、回车确认，操作流畅。
- **一键覆盖输入框**：选中模板后自动按格式模板生成前缀并覆盖 commit message 输入框内容，光标自动移到末尾，可直接继续输入正文。
- **可视化设置页面**：`Settings → Tools → Git Emoji Lint`
  - **自定义格式模板**：支持 `${emoji}`、`${type}`、`${name}`、`${description}` 四种占位符，自由组合出你想要的任何前缀格式。
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

默认格式模板为 `${type} ${emoji}: `，即生成 `feat ✨: ` 样式的前缀。所有模板及格式均可在设置页面自由修改。

## 🛠 构建

```bash
./gradlew buildPlugin
```

构建成功后，插件 zip 包输出在 `build/distributions/` 目录下。

## 🔧 开发调试

```bash
./gradlew runIde
```

该命令会启动一个带插件的沙箱 IDE 实例，在其中打开任意 Git 项目，即可在 Commit 工具窗口中测试按钮与设置页面的功能。

## 🧱 技术栈

- **语言 / 构建**：Kotlin + Gradle Kotlin DSL
- **平台 SDK**：IntelliJ Platform Gradle Plugin 2.x
- **运行环境**：JDK 21（编译目标 21）
- **兼容版本**：IntelliJ IDEA 2024.2 及以上（since-build 242，未设置 until-build 上限）

## 📖 参考

灵感来源于 VS Code 插件 [git-commit-lint-vscode](https://github.com/UvDream/git-commit-lint-vscode)，在此基础上针对 JetBrains 平台重新实现。

## 📄 许可证

本项目基于 [MIT License](LICENSE) 开源。
