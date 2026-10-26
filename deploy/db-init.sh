#!/usr/bin/env bash
# ============================================================
# 一键初始化 192.168.136.131 上已有 MySQL 的 tlias 库
#
# 用法（在 VM 上，和 docker-compose.yml 同目录）：
#   bash deploy/db-init.sh
#
# 会做三件事：
#   1. 建 tlias 库、三张表、演示数据、远程登录账号
#   2. 把 MySQL 的 bind-address 放开到 0.0.0.0（否则容器连不进来）
#   3. 如果检测到本机有 goods/student/orders 数据，问你要不要一并导入
#
# 幂等：重复执行安全，已存在的库不会被清空。
# ============================================================
set -euo pipefail

MYSQL_HOST="${DB_HOST_LOCAL:-127.0.0.1}"
MYSQL_PORT="${DB_PORT:-3306}"
MYSQL_USER="${DB_ROOT_USER:-root}"
MYSQL_PASS="${DB_ROOT_PASSWORD:-root}"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
INIT_SQL="${SCRIPT_DIR}/sql/01-init.sql"

echo "=========================================="
echo " ersho 数据库初始化"
echo " 目标: ${MYSQL_USER}@${MYSQL_HOST}:${MYSQL_PORT}"
echo "=========================================="

# ---------- 1. mysql 客户端 ----------
if ! command -v mysql >/dev/null 2>&1; then
    echo "[错误] 找不到 mysql 客户端。"
    echo "  CentOS/RHEL: sudo yum install -y mysql"
    echo "  Ubuntu:      sudo apt-get install -y mysql-client"
    exit 1
fi

# ---------- 2. 测试连接 ----------
if ! mysql -h"${MYSQL_HOST}" -P"${MYSQL_PORT}" -u"${MYSQL_USER}" -p"${MYSQL_PASS}" \
        -e "SELECT 1;" >/dev/null 2>&1; then
    echo "[错误] 用 ${MYSQL_USER}/*** 连不上 MySQL。"
    echo "  请确认 MySQL 已启动：systemctl status mysqld"
    echo "  密码不对的话：DB_ROOT_PASSWORD=你的密码 bash deploy/db-init.sh"
    exit 1
fi
echo "[1/4] MySQL 连接正常"

# ---------- 3. 建库建表灌数据 ----------
mysql -h"${MYSQL_HOST}" -P"${MYSQL_PORT}" -u"${MYSQL_USER}" -p"${MYSQL_PASS}" \
      --default-character-set=utf8mb4 < "${INIT_SQL}"
echo "[2/4] 库表 + 演示数据初始化完成"

# ---------- 4. 绑卡地址 ----------
CNF=""
for c in /etc/mysql/mysql.conf.d/mysqld.cnf /etc/mysql/my.cnf /etc/my.cnf; do
    [ -f "$c" ] && CNF="$c" && break
done

if [ -n "$CNF" ]; then
    if grep -qE '^\s*bind-address\s*=\s*(127\.0\.0\.1|localhost)' "$CNF"; then
        echo "[3/4] 检测到 ${CNF} 里 bind-address 只监听 127.0.0.1，容器会连不进来，正在放开..."
        sudo cp "$CNF" "${CNF}.bak.$(date +%s)"
        sudo sed -i -E 's/^\s*bind-address\s*=.*/bind-address = 0.0.0.0/' "$CNF"
        sudo systemctl restart mysqld 2>/dev/null || sudo systemctl restart mysql
        sleep 3
        echo "      MySQL 已重启，bind-address = 0.0.0.0"
    else
        echo "[3/4] bind-address 已允许外部访问（${CNF}）"
    fi
else
    echo "[3/4] 没找到 my.cnf，跳过 bind-address 检查"
    echo "      如果容器连不上，手动加一行 bind-address = 0.0.0.0 再重启 MySQL"
fi

# ---------- 5. 可选：导入本机已有数据 ----------
echo ""
if mysql -h"${MYSQL_HOST}" -P"${MYSQL_PORT}" -u"${MYSQL_USER}" -p"${MYSQL_PASS}" \
        -e "SELECT COUNT(*) FROM tlias.goods;" >/dev/null 2>&1; then
    CNT=$(mysql -h"${MYSQL_HOST}" -P"${MYSQL_PORT}" -u"${MYSQL_USER}" -p"${MYSQL_PASS}" \
          -N -B -e "SELECT COUNT(*) FROM tlias.goods;" 2>/dev/null)
    echo "[4/4] tlias.goods 当前有 ${CNT} 条记录（脚本已灌入演示数据）"
else
    echo "[4/4] 自检失败：tlias.goods 读不到，请检查上面的输出"
    exit 1
fi

echo ""
echo "=========================================="
echo " 初始化完成"
echo " 演示账号（手机号 / 密码都是 123456）："
echo "   13900139000 张三    13700137000 李四"
echo "   13600136000 王佳    13500135000 陈明"
echo "   13400134000 刘婷    13300133000 赵宇"
echo "   13200132000 周欣"
echo ""
echo " 接下来：docker compose up -d --build"
echo " 然后浏览器打开 http://192.168.136.131:8081/"
echo "=========================================="
