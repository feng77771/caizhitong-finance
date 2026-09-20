---
title: CaiZhiTong Finance Backend
emoji: 📊
colorFrom: blue
colorTo: purple
sdk: docker
app_port: 7860
pinned: false
---

# 多模态财报深度解析平台 - 后端服务

单容器运行 MySQL 8 + Spring Boot 后端（前端部署于 GitHub Pages）。

- 启动时自动初始化数据库并导入演示数据
- 首次访问或休眠唤醒后需要约 1-3 分钟冷启动
- AI 对话功能依赖本地 Ollama，云端不可用
