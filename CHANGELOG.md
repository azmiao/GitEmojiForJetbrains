# 更新日志

## 1.0.2 (2026-08-25)

### 🐛 Bug 修复

- 修复生成changelog时归类错误 @azmiao

### 🎨 代码重构

- (breaking change) 默认和原VSC插件行为保持一致，type和emoji之间去除空格 @azmiao

### 🧹 日常维护

- 修正插件描述和更新日志逻辑 @azmiao
- 优化BOT提交格式 @azmiao
- 更新 CHANGELOG @github-actions[bot]

### 其他变更

- v1.0.2 @azmiao

## 1.0.1 (2026-08-24)

### ✨ 新功能

- 新增changelog生成脚本 @azmiao
- 设置页面（模板表格编辑 + 格式模板） @azmiao
- Commit 工具窗口 Action 与模板选择弹窗 @azmiao
- 持久化设置服务 @azmiao
- 格式化引擎与占位符替换 @azmiao
- 数据模型 EmojiTemplate 与 18 条默认模板 @azmiao
- 项目骨架与 Gradle 配置 @azmiao

### 🐛 Bug 修复

- 修复新增配置高亮缺失，修复生成前缀自动全选文字 @azmiao
- 尝试更换为直接拉取Gradle版本 @azmiao
- 修改固定CI的gradle版本 @azmiao
- 修复gradle配置问题 @azmiao
- 用 provider { null } 移除 untilBuild 限制 @azmiao
- EmojiTemplate 改为 JavaBean 风格，修复持久化序列化 @azmiao
- 移除 untilBuild 限制，兼容 2024.2+ 所有版本 @azmiao

### 🚀 性能优化

- 修复通知组未注册，优化部分代码逻辑 @azmiao

### 🎨 代码重构

- 更换按钮图标和预览图 @azmiao
- 修改包名，修改包路径 @azmiao

### 📚 文档

- 更新文档样式 @azmiao
- 更新文档 @azmiao
- 更新 README 使用说明 + 修复 EmojiTemplate 序列化 @azmiao

### 🧹 日常维护

- 优化代码逻辑和引用 @azmiao
- 修改svg为40x40图标 @azmiao
- 新增插件发布配置 @azmiao
- 移除 superpowers 计划/设计文档并更新 .gitignore @azmiao

### 其他变更

- v1.0.1 @azmiao
- Initial commit @azmiao
