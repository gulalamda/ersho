#!/usr/bin/env bash
# ============================================================
# 一键部署（在 192.168.136.131 上执行）
#
#   bash deploy/deploy.sh
#
# 做四件事：检查环境 → 初始化数据库 → 构建镜像 → 启动
# 已经初始化过数据库的话，第 2 步会跳过（幂等，不会清数据）。
# ============================================================
set -euo pipefail

cd "$(dirname "${BASH_SOURCE[0]}")/.."
PROJECT_DIR="$(pwd)"
VM_IP="192.168.136.131"
APP_PORT="$(grep -E '^APP_PORT=' .env 2>/dev/null | cut -d= -f2 || echo 8081)"
APP_PORT="${APP_PORT:-8081}"

echo "=========================================="
echo " ersho 一键部署"
echo " 项目目录: ${PROJECT_DIR}"
echo " 访问地址: http://${VM_IP}:${APP_PORT}/"
echo "=========================================="

# ---------- 1. 环境检查 ----------
echo ""
echo "[1/4] 环境检查"
for cmd in docker mysql; do
    if ! command -v "$cmd" >/dev/null 2>&1; then
        echo "  [错误] 缺少 $cmd 命令"
        exit 1
    fi
done
if ! docker info >/dev/null 2>&1; then
    echo "  [错误] Docker 引擎没启动：sudo systemctl start docker"
    exit 1
fi
if ! docker compose version >/dev/null 2>&1; then
    echo "  [错误] 缺少 compose 插件：sudo yum install -y docker-compose-plugin"
    exit 1
fi
echo "  docker / compose / mysql 都在"

# ---------- 2. 数据库 ----------
echo ""
echo "[2/4] 初始化数据库"
if mysql -uroot -p"${DB_ROOT_PASSWORD:-root}" -N -B \
        -e "SELECT 1 FROM tlias.goods LIMIT 1;" >/dev/null 2>&1; then
    echo "  tlias 库已存在，跳过初始化（想重建请手动删库后重跑）"
else
    echo "  未检测到 tlias 库，执行 deploy/db-init.sh"
    bash deploy/db-init.sh
fi

# ---------- 3. 构建 ----------
echo ""
echo "[3/4] 构建镜像（第一次要下 Maven 依赖，可能 5~15 分钟）"
docker compose build

# ---------- 4. 启动 ----------
echo ""
echo "[4/4] 启动容器"
docker compose up -d

echo ""
echo "等待应用就绪..."
for i in $(seq 1 40); do
    sleep 3
    STATUS="$(docker inspect --format='{{.State.Health.Status}}' ersho-app 2>/dev/null || echo unknown)"
    if [ "$STATUS" = "healthy" ]; then
        echo "  应用已就绪（等待 $((i * 3)) 秒）"
        break
    fi
    if [ "$STATUS" = "unhealthy" ]; then
        echo "  [警告] 健康检查失败，看日志：docker compose logs --tail=100 app"
        break
    fi
done

echo ""
echo "=========================================="
echo " 部署完成"
echo " 访问: http://${VM_IP}:${APP_PORT}/"
echo " 账号: 13900139000 / 123456"
echo ""
echo " 常用命令："
echo "   docker compose ps              # 状态"
echo "   docker compose logs -f app     # 日志"
echo "   docker compose restart app     # 重启"
echo "   docker compose down            # 停止"
echo "=========================================="
docker compose ps
