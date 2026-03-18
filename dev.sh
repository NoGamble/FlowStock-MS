#!/bin/bash

# --- 颜色定义 ---
GREEN='\033[0;32m'
BLUE='\033[0;34m'
NC='\033[0m'

echo -e "${BLUE}>>> 正在启动 FlowStock-MS 全栈开发环境...${NC}"

# 1. 启动后端 (后台运行)
echo -e "${GREEN}正在启动 Spring Boot 后端 (Port: 8080)...${NC}"
./mvnw spring-boot:run & 
BACKEND_PID=$!

# 2. 等待后端初步启动
sleep 3

# 3. 启动前端
echo -e "${GREEN}正在启动 Vue 3 前端 (Port: 5173)...${NC}"
cd frontend && npm run dev &
FRONTEND_PID=$!

# 捕捉退出信号 (Ctrl+C)，同时关闭前后端
trap "kill $BACKEND_PID $FRONTEND_PID; echo -e '\n${BLUE}已停止所有服务${NC}'; exit" SIGINT

wait
