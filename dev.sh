#!/bin/bash

# --- 颜色定义 ---
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[0;33m'
NC='\033[0m'

echo -e "${BLUE}>>> 正在启动 FlowStock 全栈环境...${NC}"

# 1. 清理可能残留的端口（防止端口占用报错）
echo -e "${YELLOW}检查端口占用...${NC}"
lsof -i:8080 -t | xargs kill -9 2>/dev/null
lsof -i:5173 -t | xargs kill -9 2>/dev/null

# 2. 启动后端
# 将后端日志重定向到文件，避免跟前端日志混在一起打架
echo -e "${GREEN}正在启动 Spring Boot (日志记录在 backend.log)...${NC}"
./mvnw spring-boot:run > backend.log 2>&1 &
BACKEND_PID=$!

# 3. 启动前端
echo -e "${GREEN}正在启动 Vue 3 前端...${NC}"
# 使用 --silent 减少 npm 自身的废话
cd frontend && npm run dev -- &
FRONTEND_PID=$!

# --- 核心改进：彻底的退出机制 ---
# 定义一个清理函数
cleanup() {
    echo -e "\n${BLUE}>>> 正在关闭所有服务 (PID: $BACKEND_PID, $FRONTEND_PID)...${NC}"
    # 杀死进程组，确保子进程（比如 mvnw 启动的 java 进程）也被关掉
    kill -TERM $BACKEND_PID $FRONTEND_PID 2>/dev/null
    exit
}

# 捕捉 Ctrl+C (SIGINT) 和 终止信号 (SIGTERM)
trap cleanup SIGINT SIGTERM

echo -e "${YELLOW}提示: 使用 Ctrl+C 退出。后端日志请查看 tail -f backend.log${NC}"

# 保持脚本运行
wait