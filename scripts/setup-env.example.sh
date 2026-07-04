#!/usr/bin/env bash
# =====================================================================
# API Key 环境变量模板
#
# 使用方式：
#   cp scripts/setup-env.example.sh scripts/setup-env.local.sh
#   编辑 setup-env.local.sh 填入真实 Key（该文件已被 .gitignore 排除）
#   source scripts/setup-env.local.sh
#
# 严禁将真实 Key 写入任何被 Git 追踪的文件或 application.yml。
# =====================================================================

# 阿里云百炼 DashScope（https://bailian.console.aliyun.com 获取）
export AI_DASHSCOPE_API_KEY="sk-your-dashscope-key"

# DeepSeek 开放平台（https://platform.deepseek.com 获取）
export DEEPSEEK_API_KEY="sk-your-deepseek-key"

echo "已加载模型 API Key 环境变量（DashScope / DeepSeek）"
