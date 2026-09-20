#!/bin/bash
set -e

APP_DIR=/app
DATA_DIR=/app/mysql-data
SOCK_DIR=/run/mysqld

mkdir -p "$DATA_DIR" "$SOCK_DIR"

# 根据运行身份决定是否需要切换 MySQL 运行用户
if [ "$(id -u)" = "0" ]; then
  chown -R mysql:mysql "$DATA_DIR" "$SOCK_DIR"
  USER_FLAG="--user=mysql"
else
  USER_FLAG=""
fi

# 首次启动初始化数据目录（lower_case_table_names=1 与原 Windows 环境一致）
if [ ! -d "$DATA_DIR/mysql" ]; then
  mysqld --initialize-insecure $USER_FLAG --datadir="$DATA_DIR" --lower_case_table_names=1
fi

# 后台启动 MySQL
mysqld $USER_FLAG --datadir="$DATA_DIR" --socket="$SOCK_DIR/mysqld.sock" \
  --bind-address=127.0.0.1 --port=3306 --skip-name-resolve \
  --performance-schema=OFF --innodb-buffer-pool-size=256M \
  --pid-file="$SOCK_DIR/mysqld.pid" &

# 等待 MySQL 就绪
for i in $(seq 1 60); do
  if mysqladmin --socket="$SOCK_DIR/mysqld.sock" ping --silent 2>/dev/null; then
    break
  fi
  sleep 1
done

# 首次启动：设置 root 密码（与应用配置一致）并导入演示数据
if [ ! -f "$DATA_DIR/.imported" ]; then
  mysql --socket="$SOCK_DIR/mysqld.sock" -uroot -e "ALTER USER 'root'@'localhost' IDENTIFIED BY 'Fjy204716@'; CREATE USER IF NOT EXISTS 'root'@'%' IDENTIFIED BY 'Fjy204716@'; GRANT ALL PRIVILEGES ON *.* TO 'root'@'%'; FLUSH PRIVILEGES;"
  mysql --socket="$SOCK_DIR/mysqld.sock" -uroot -p'Fjy204716@' < /app/finance_dump.sql
  touch "$DATA_DIR/.imported"
fi

# HF Spaces 要求应用监听 7860 端口
export SERVER_PORT=7860
exec java -Xmx1200m -Dfile.encoding=UTF-8 -jar /app/app.jar
